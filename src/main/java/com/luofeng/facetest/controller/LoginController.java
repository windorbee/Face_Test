package com.luofeng.facetest.controller;

import com.luofeng.facetest.pojo.Result;
import com.luofeng.facetest.pojo.User;
import com.luofeng.facetest.service.UserService;
import com.luofeng.facetest.utils.JwtUtils;
import com.luofeng.facetest.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        // 根据用户名查询用户
        User dbUser = userService.getByUsername(user.getUsername());
        
        // 判断用户是否存在以及密码是否正确
        if (dbUser == null) {
            return Result.error("用户名不存在");
        }
        
        if (!dbUser.getPassword().equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        
        // 登录成功，生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", dbUser.getId());
        claims.put("username", dbUser.getUsername());
        
        String jwtToken = JwtUtils.genToken(claims);
        
        // 将用户ID存入ThreadLocal
        UserThreadLocal.setUserId(dbUser.getId());
        
        // 返回令牌
        return Result.success(jwtToken);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result logout() {
        // 清除ThreadLocal中的用户ID
        UserThreadLocal.clear();
        return Result.success("登出成功");
    }
}