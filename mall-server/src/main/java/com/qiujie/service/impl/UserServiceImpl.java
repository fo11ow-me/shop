package com.qiujie.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.entity.User;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.UserMapper;
import com.qiujie.service.OssService;
import com.qiujie.service.UserService;
import com.qiujie.util.HutoolExcelUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author qiujie
 * @since 2022-01-27
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OssService ossService;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, OssService ossService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.ossService = ossService;
    }

    public void add(User user) {
        if (user.getCode() == null || user.getCode().isBlank()) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR);
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR);
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR);
        }
        save(user);
        user.setPassword(passwordEncoder.encode("123")).setCode("user_" + user.getId());
        updateById(user);
    }

    public void delete(Integer id) {
        removeById(id);
    }

    public void edit(User user) {
        updateById(user);
    }

    public User query(Integer id) {
        User user = getById(id);
        if (user == null) {
            throw new ServiceException(BusinessStatusEnum.USER_NOT_EXIST);
        }
        return user;
    }

    public User queryByCode(String code) {
        User user = userMapper.queryByCode(code);
        if (user == null) {
            throw new ServiceException(BusinessStatusEnum.USER_NOT_EXIST);
        }
        return user;
    }

    public Map<String, Object> list(Integer current, Integer size, String name, String birthday, Integer status, String code, String phone, Integer gender, String startTime, String endTime) {
        IPage<User> pageConfig = new Page<>(current, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (name != "" && name != null) {
            wrapper.like("name", name);
        }
        if (birthday != null) {
            wrapper.ge("birthday", birthday);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (code != null && !code.isEmpty()) {
            wrapper.like("code", code);
        }
        if (phone != null && !phone.isEmpty()) {
            wrapper.like("phone", phone);
        }
        if (gender != null) {
            wrapper.eq("gender", gender);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge("create_time", startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le("create_time", endTime);
        }
        IPage<User> page = page(pageConfig, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("pages", page.getPages());
        map.put("total", page.getTotal());
        map.put("list", page.getRecords());
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Integer> ids) {
        removeBatchByIds(ids);
    }

    public void export(HttpServletResponse response, String filename) throws IOException {
        List<User> list = this.userMapper.queryUserDeptVO();
        HutoolExcelUtil.writeExcel(response, list, filename, User.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void imp(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        List<User> list = HutoolExcelUtil.readExcel(inputStream, 1, User.class);
        for (User user : list) {
            save(user);
            user.setPassword(passwordEncoder.encode("123")).setCode("user_" + user.getId());
            updateById(user);
        }
    }

    public void validate(String pwd, Integer id) {
        User user = getById(id);
        if (!passwordEncoder.matches(pwd, user.getPassword())) {
            throw new ServiceException(BusinessStatusEnum.PASSWORD_ERROR);
        }
    }

    public void reset(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        updateById(user);
    }

    public User queryInfo(Integer id) {
        User staffInfo = this.userMapper.queryInfo(id);
        if (staffInfo == null) {
            throw new ServiceException(BusinessStatusEnum.USER_NOT_EXIST);
        }
        return staffInfo;
    }

    public User updateInfo(Integer userId, Map<String, String> params) {
        User user = getById(userId);
        if (user == null) {
            throw new ServiceException(BusinessStatusEnum.USER_NOT_EXIST);
        }
        if (params.containsKey("name")) user.setName(params.get("name"));
        if (params.containsKey("phone")) user.setPhone(params.get("phone"));
        if (params.containsKey("address")) user.setAddress(params.get("address"));
        updateById(user);
        user.setPassword(null);
        return user;
    }

    private static final String AVATAR_DIR = "avatar/";

    @Override
    public Map<String, String> updateAvatar(Integer userId, MultipartFile file) throws IOException {
        User user = getById(userId);
        if (user == null) {
            throw new ServiceException(BusinessStatusEnum.USER_NOT_EXIST);
        }

        byte[] fileBytes = file.getBytes();
        String newMd5;
        try {
            newMd5 = bytesToHex(MessageDigest.getInstance("MD5").digest(fileBytes));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        String oldAvatar = user.getAvatar();
        if (StrUtil.isNotBlank(oldAvatar)) {
            String oldOssKey = AVATAR_DIR + oldAvatar;
            String oldETag = ossService.getETag(oldOssKey);
            if (newMd5.equalsIgnoreCase(oldETag)) {
                String url = ossService.getUrl(oldOssKey);
                return Map.of("avatar", oldAvatar, "url", url);
            }
            ossService.delete(oldOssKey);
        }

        String ossKey = ossService.upload(file, "avatar");
        String fileName = ossKey.startsWith(AVATAR_DIR) ? ossKey.substring(AVATAR_DIR.length()) : ossKey;
        user.setAvatar(fileName);
        updateById(user);

        String url = ossService.getUrl(ossKey);
        return Map.of("avatar", fileName, "url", url);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
