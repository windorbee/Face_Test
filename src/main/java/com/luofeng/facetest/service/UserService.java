package com.luofeng.facetest.service;

import com.luofeng.facetest.pojo.User;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserService {
    /**
     * 查询所有用户
     */
    List<User> list();

    /**
     * 根据ID查询用户
     */
    User getById(Integer id);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 添加用户
     */
    void add(User user);

    /**
     * 更新用户信息
     */
    void update(User user);

    /**
     * 根据ID删除用户
     */
    void deleteById(Integer id);
    
    /**
     * 分页条件查询用户
     */
    PageInfo<User> getUsersByPage(Integer page, Integer pageSize, String username);
}