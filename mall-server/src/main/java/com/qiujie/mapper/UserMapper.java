package com.qiujie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiujie.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("select id, code, name, gender, role, pwd as password, avatar, birthday, phone, address, remark, status from sys_user where is_deleted = 0 and code = #{code}")
    User queryByCode(@Param("code") String code);

    @Select("select id, code, name, gender, role, avatar, birthday, phone, address, remark, status from sys_user where is_deleted = 0 and id = #{id}")
    User queryInfo(@Param("id") Integer id);

    @Select("select id, code, name, gender, role, pwd as password, avatar, birthday, phone, address, remark, status from sys_user where is_deleted = 0")
    List<User> queryUserDeptVO();
}
