package com.qiujie.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.qiujie.entity.User;
import com.qiujie.enums.RoleEnum;
import com.qiujie.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限加载实现 —— 基于数据库用户角色
 *
 * @author qiujie
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    public StpInterfaceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Integer userId = Integer.valueOf(loginId.toString());
        User user = userMapper.selectById(userId);
        if (user != null && user.getRole() == RoleEnum.ADMIN) {
            return Collections.singletonList("admin");
        }
        return Collections.singletonList("user");
    }
}
