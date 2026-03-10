package com.luofeng.facetest.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.luofeng.facetest.mapper.UserMapper;
import com.luofeng.facetest.pojo.User;
import com.luofeng.facetest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 查询所有用户
     */
    @Override
    public List<User> list() {
        return userMapper.list();
    }
    
    /**
     * 根据ID查询用户
     */
    @Override
    public User getById(Integer id) {
        return userMapper.getById(id);
    }
    
    /**
     * 根据用户名查询用户
     */
    @Override
    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }
    
    /**
     * 添加用户
     */
    @Override
    public void add(User user) {
        userMapper.add(user);
    }
    
    /**
     * 更新用户信息
     */
    @Override
    public void update(User user) {
        // 如果密码不为空，则单独更新密码
        if (StringUtils.hasText(user.getPassword())) {
            userMapper.updatePassword(user.getId(), user.getPassword());
        }
        // 更新其他用户信息
        userMapper.update(user);
    }
    
    /**
     * 根据ID删除用户
     */
    @Override
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }
    
    /**
     * 分页条件查询用户
     */
    @Override
    public PageInfo<User> getUsersByPage(Integer page, Integer pageSize, String username) {
        PageHelper.startPage(page, pageSize);
        List<User> users = userMapper.getUsersByCondition(username);
        return new PageInfo<>(users);
    }
}