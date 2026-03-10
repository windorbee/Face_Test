package com.luofeng.facetest.controller;

import com.luofeng.facetest.pojo.Result;
import com.luofeng.facetest.pojo.testResult;
import com.luofeng.facetest.service.DoubaoService;
import com.luofeng.facetest.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/doubao")
public class DoubaoController {
    
    @Autowired
    private DoubaoService doubaoService;
    
    /**
     * 上传图片并分析是否为腺体面容
     * @param image 图片文件
     * @return 分析结果
     */
    @PostMapping("/analyzeFace")
    public Result analyzeFace(@RequestParam("image") MultipartFile image) {
        try {
            // 从ThreadLocal中获取当前用户ID
            Integer userId = UserThreadLocal.getUserId();
            testResult result = doubaoService.analyzeFace(image, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("分析失败: " + e.getMessage());
        }
    }
}