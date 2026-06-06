package com.qiujie.service.impl;

import com.qiujie.entity.User;
import com.qiujie.entity.CustomUserDetails;
import com.qiujie.enums.RoleEnum;
import com.qiujie.enums.UserStatusEnum;
import com.qiujie.mapper.UserMapper;
import com.qiujie.service.CustomUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    private final UserMapper userMapper;

    public CustomUserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userMapper.selectOne(new QueryWrapper<User>().eq("code", username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (user.getStatus() == UserStatusEnum.DISABLED) {
            throw new UsernameNotFoundException("账号已被禁用");
        }
        List<GrantedAuthority> list = new ArrayList<>();
        String role = user.getRole() == RoleEnum.ADMIN ? ROLE_ADMIN : ROLE_USER;
        list.add(new SimpleGrantedAuthority(role));
        return new CustomUserDetails(user.getId(), username, user.getPassword(), list,
                true, true, true, true);
    }
}
