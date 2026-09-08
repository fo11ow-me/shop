package com.qiujie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiujie.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
public interface UserService extends IService<User> {

    void add(User user);

    void delete(Integer id);

    void edit(User user);

    User query(Integer id);

    User queryByCode(String code);

    Map<String, Object> list(Integer current, Integer size, String name, String birthday, Integer status, String code, String phone, Integer gender, String startTime, String endTime);

    void deleteBatch(List<Integer> ids);

    void export(HttpServletResponse response, String filename) throws IOException;

    void imp(MultipartFile file) throws IOException;

    void validate(String pwd, Integer id);

    void reset(User user);

    User queryInfo(Integer id);

    User updateInfo(Integer userId, Map<String, String> params);

    Map<String, String> updateAvatar(Integer userId, MultipartFile file) throws IOException;
}
