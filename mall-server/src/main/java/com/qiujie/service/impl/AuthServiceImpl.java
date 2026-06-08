package com.qiujie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.entity.CustomUserDetails;
import com.qiujie.entity.User;
import com.qiujie.entity.ValidateCode;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.UserStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.UserMapper;
import com.qiujie.service.AuthService;
import com.qiujie.util.JwtUtil;
import com.qiujie.util.RedisUtil;
import com.qiujie.util.ValidateCodeUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                           RedisUtil redisUtil, AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisUtil = redisUtil;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(String code, String password, String phone, String verificationCode, String uuid) {
        if (code == null || code.isBlank() || password == null || password.isBlank()) {
            throw new ServiceException(BusinessStatusEnum.AUTH_EMPTY_CREDENTIALS);
        }
        // 验证码校验（与 login 一致）
        if (uuid == null || uuid.isBlank()) {
            throw new ServiceException(BusinessStatusEnum.CAPTCHA_NOT_EXIST);
        }
        String codeInRedis = (String) redisUtil.get("validate:code:" + uuid);
        if (codeInRedis == null) {
            throw new ServiceException(BusinessStatusEnum.CAPTCHA_NOT_EXIST);
        }
        if (!codeInRedis.equals(verificationCode)) {
            throw new ServiceException(BusinessStatusEnum.CAPTCHA_ERROR);
        }
        redisUtil.del("validate:code:" + uuid);

        User exist = userMapper.selectOne(new QueryWrapper<User>().eq("code", code));
        if (exist != null) {
            throw new ServiceException(BusinessStatusEnum.USERNAME_EXISTS);
        }
        User user = new User();
        user.setCode(code);
        user.setName(code);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setStatus(UserStatusEnum.ENABLED);
        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(String code, String password, String verificationCode, String uuid, String pathPrefix) {
        if (uuid == null || uuid.isBlank()) {
            throw new ServiceException(BusinessStatusEnum.CAPTCHA_NOT_EXIST);
        }
        String codeInRedis = (String) redisUtil.get("validate:code:" + uuid);
        if (codeInRedis == null) {
            throw new ServiceException(BusinessStatusEnum.CAPTCHA_NOT_EXIST);
        }
        if (!codeInRedis.equals(verificationCode)) {
            throw new ServiceException(BusinessStatusEnum.CAPTCHA_ERROR);
        }
        redisUtil.del("validate:code:" + uuid);
        User existUser = userMapper.selectOne(new QueryWrapper<User>().eq("code", code));
        if (existUser == null) {
            throw new ServiceException(BusinessStatusEnum.USER_NOT_EXIST);
        }
        if (existUser.getStatus() == UserStatusEnum.DISABLED) {
            throw new ServiceException(BusinessStatusEnum.ACCOUNT_DISABLED);
        }
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(code, password);
        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(authToken);
        } catch (AuthenticationException e) {
            throw new ServiceException(BusinessStatusEnum.PASSWORD_ERROR);
        }
        CustomUserDetails staffDetails = (CustomUserDetails) authenticate.getPrincipal();
        String token = jwtUtil.generateToken(staffDetails, pathPrefix);
        User user = this.userMapper.queryByCode(staffDetails.getUsername());
        return Map.of("user", user, "token", token);
    }

    @Override
    public Map<String, Object> getVerificationCode() {
        ValidateCode validateCode = ValidateCodeUtil.generateValidateCode();
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        redisUtil.set("validate:code:" + uuid, validateCode.getCode(), 60);
        return Map.of("uuid", uuid, "image", validateCode.getImage());
    }

    @Override
    public void logout(String token, String requestUri) {
        String jti = jwtUtil.extractJti(token, requestUri);
        if (jti != null) {
            long remaining = jwtUtil.extractRemainingSeconds(token, requestUri);
            if (remaining > 0) {
                redisUtil.set("token:blacklist:" + jti, "1", remaining);
            }
        }
    }
}
