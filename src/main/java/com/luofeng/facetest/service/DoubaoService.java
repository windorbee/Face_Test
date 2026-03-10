package com.luofeng.facetest.service;

import com.luofeng.facetest.pojo.testResult;
import org.springframework.web.multipart.MultipartFile;

public interface DoubaoService {
    /**
     * 分析面部图片是否为腺体面容
     * @param image 图片文件
     * @param userId 用户ID
     * @return 分析结果
     * @throws Exception 处理异常
     */
    testResult analyzeFace(MultipartFile image, Integer userId) throws Exception;
}