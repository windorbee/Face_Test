package com.luofeng.facetest.controller;

import com.luofeng.facetest.pojo.Result;
import com.luofeng.facetest.pojo.User;
import com.luofeng.facetest.service.UserService;
import com.luofeng.facetest.utils.AliyunOSSOperator;
import com.luofeng.facetest.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;
    
    /**
     * 用户注册新用户
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        user.setCreateTime(LocalDateTime.now());
        user.setAvatar(""); // 默认头像为空
        // 设置默认值
        if (!StringUtils.hasText(user.getName())) {
            user.setName(user.getUsername()); // 默认姓名为用户名
        }
        userService.add(user);
        return Result.success("注册成功");
    }
    
    /**
     * 用户修改自己的信息
     */
    @PutMapping
    public Result<String> updateUser(@RequestBody User user) {
        // 从ThreadLocal中获取当前用户ID
        Integer userId = UserThreadLocal.getUserId();
        // 确保用户只能修改自己的信息
        user.setId(userId);
        // 如果密码为空，则不更新密码
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        }
        userService.update(user);
        return Result.success("更新成功");
    }
    
    /**
     * 用户获取自己的信息
     */
    @GetMapping
    public Result<User> getCurrentUser() {
        // 从ThreadLocal中获取当前用户ID
        Integer userId = UserThreadLocal.getUserId();
        User user = userService.getById(userId);
        return Result.success(user);
    }
    
    /**
     * 用户上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatar") MultipartFile avatarFile) {
        try {
            // 从ThreadLocal中获取当前用户ID
            Integer userId = UserThreadLocal.getUserId();
            
            // 检查文件是否为空
            if (avatarFile.isEmpty()) {
                return Result.error("上传的头像文件不能为空");
            }
            
            // 检查文件类型
            String contentType = avatarFile.getContentType();
            if (contentType == null || (!contentType.startsWith("image/jpeg") && 
                                        !contentType.startsWith("image/png") && 
                                        !contentType.startsWith("image/gif"))) {
                return Result.error("只支持JPG、PNG、GIF格式的图片");
            }
            
            // 检查文件大小（限制为2MB）
            if (avatarFile.getSize() > 2 * 1024 * 1024) {
                return Result.error("头像文件大小不能超过2MB");
            }
            
            // 上传到阿里云OSS
            String avatarUrl = aliyunOSSOperator.upload(avatarFile.getBytes(), avatarFile.getOriginalFilename());
            
            // 更新用户头像信息
            User user = userService.getById(userId);
            user.setAvatar(avatarUrl);
            userService.update(user);
            
            return Result.success(avatarUrl);
        } catch (Exception e) {
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}