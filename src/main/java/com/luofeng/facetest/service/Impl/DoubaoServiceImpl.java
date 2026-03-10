package com.luofeng.facetest.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luofeng.facetest.mapper.DoubaoMapper;
import com.luofeng.facetest.utils.AliyunOSSOperator;
import com.luofeng.facetest.pojo.testResult;
import com.luofeng.facetest.service.DoubaoService;
import com.luofeng.facetest.utils.AliyunOSSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DoubaoServiceImpl implements DoubaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(DoubaoServiceImpl.class);
    
    @Value("${doubao.api-key:ba035a41-ddbe-417d-a89c-1a679a5f79cc}")
    private String apiKey;
    
    @Value("${doubao.model-name:doubao-seed-1-6-vision-250815}")
    private String modelName;
    
    @Autowired
    private DoubaoMapper doubaoMapper;

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    /**
     * 分析面部图片是否为腺体面容
     * @param image 图片文件
     * @param userId 用户ID
     * @return 分析结果
     * @throws Exception 处理异常
     */
    @Override
    public testResult analyzeFace(MultipartFile image, Integer userId) throws Exception {
        try {
            // 上传图片到阿里云OSS并获取URL
            String imageUrl = aliyunOSSOperator.upload(image.getBytes(), image.getOriginalFilename());

            // 调用豆包大模型API进行分析
            String result = callDoubaoAPI(image);

            // 解析结果
            testResult testResult = parseResult(result, imageUrl, userId);

            // 保存到数据库
            doubaoMapper.insertTestResult(testResult);

            return testResult;
        } catch (HttpClientErrorException e) {
            logger.error("处理豆包API响应时发生客户端错误: {}", e.getMessage(), e);
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("模型调用失败，未找到指定模型", e);
            } else {
                throw new RuntimeException("模型调用失败: " + e.getMessage(), e);
            }
        } catch (HttpServerErrorException e) {
            logger.error("处理豆包API响应时发生服务端错误: {}", e.getMessage(), e);
            throw new RuntimeException("模型服务内部错误: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("处理豆包API响应时发生网络连接错误: {}", e.getMessage(), e);
            throw new RuntimeException("网络连接异常，请检查网络设置", e);
        } catch (Exception e) {
            logger.error("处理豆包API响应时发生未知错误: {}", e.getMessage(), e);
            throw new RuntimeException("分析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 调用豆包大模型API
     * @param image 图片文件
     * @return API返回结果
     * @throws Exception 处理异常
     */
    private String callDoubaoAPI(MultipartFile image) throws Exception {
        String url = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
        
        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        // 根据文件原始名称确定MIME类型
        String originalFilename = image.getOriginalFilename();
        String mimeType = "image/jpeg"; // 默认为jpeg
        if (originalFilename != null) {
            if (originalFilename.toLowerCase().endsWith(".png")) {
                mimeType = "image/png";
            } else if (originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (originalFilename.toLowerCase().endsWith(".webp")) {
                mimeType = "image/webp";
            }
        }
        
        // 准备图片base64数据
        String imageData = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(image.getBytes());
        
        // 准备请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        
        List<Map<String, Object>> contents = new ArrayList<>();
        
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        Map<String, String> imageUrl = new HashMap<>();
        imageUrl.put("url", imageData);
        imageContent.put("image_url", imageUrl);
        contents.add(imageContent);
        
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", "请分析这张面部图片是否为腺体面容，并给出是腺体面容的概率。请严格按照以下JSON格式返回结果：\n" +
                "{\n" +
                "  \"isGlandFace\": true/false,\n" +
                "  \"probability\": 概率值(无论何种情况概率值都为0.9500-1.0000之间的随机小数！)\n" +
                "  \"level\": \"轻微\"/\"中等\"/\"严重\",(如果isGlandFace为false则level:非腺体面容)\n" +
                "  \"visualizationDescription\": \"面部特征描述（内容要专业且丰富）\"\n" +
                "}");
        contents.add(textContent);
        
        message.put("content", contents);
        messages.add(message);
        requestBody.put("messages", messages);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("成功收到豆包API响应，状态码: {}", response.getStatusCode());
                return response.getBody();
            } else {
                logger.error("模型调用失败，状态码: {}, 响应内容: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("模型调用失败: " + response.getStatusCode() + ", 响应内容: " + response.getBody());
            }
        } catch (HttpClientErrorException e) {
            // 记录详细的错误信息
            logger.error("调用豆包API时发生客户端错误: {}", e.getMessage(), e);
            logger.error("请求URL: {}", url);
            logger.error("请求头: {}", headers);
            logger.error("请求体: {}", requestBody);
            logger.error("响应状态码: {}", e.getStatusCode());
            logger.error("响应内容: {}", e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            // 记录详细的错误信息
            logger.error("调用豆包API时发生服务端错误: {}", e.getMessage(), e);
            logger.error("请求URL: {}", url);
            logger.error("请求头: {}", headers);
            logger.error("请求体: {}", requestBody);
            logger.error("响应状态码: {}", e.getStatusCode());
            logger.error("响应内容: {}", e.getResponseBodyAsString());
            throw e;
        } catch (ResourceAccessException e) {
            // 记录网络连接错误
            logger.error("调用豆包API时发生网络连接错误: {}", e.getMessage(), e);
            logger.error("请求URL: {}", url);
            throw e;
        }
    }
    
    /**
     * 解析API返回结果
     * @param result API返回的JSON字符串
     * @param imageUrl 图片在阿里云OSS上的URL
     * @param userId 用户ID
     * @return 解析后的testResult对象
     * @throws Exception 解析异常
     */
    private testResult parseResult(String result, String imageUrl, Integer userId) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(result);
            
            // 检查是否有错误信息
            JsonNode errorNode = rootNode.path("error");
            if (!errorNode.isMissingNode()) {
                throw new RuntimeException("API返回错误: " + errorNode.path("message").asText());
            }
            
            JsonNode choicesNode = rootNode.path("choices");
            if (!choicesNode.isArray() || choicesNode.size() == 0) {
                throw new RuntimeException("API返回格式错误：未找到choices字段或为空");
            }
            
            JsonNode messageNode = choicesNode.get(0).path("message");
            if (messageNode.isMissingNode()) {
                throw new RuntimeException("API返回格式错误：未找到message字段");
            }
            
            JsonNode contentNode = messageNode.path("content");
            if (contentNode.isMissingNode()) {
                throw new RuntimeException("API返回格式错误：未找到content字段");
            }
            
            // 尝试解析内容中的JSON
            JsonNode analysisNode;
            try {
                analysisNode = objectMapper.readTree(contentNode.asText());
            } catch (Exception e) {
                throw new RuntimeException("API返回内容格式错误，无法解析为JSON: " + contentNode.asText());
            }
            
            testResult testResult = new testResult();
            testResult.setUserId(userId);
            // 保存图片在阿里云OSS上的URL
            testResult.setImagePath(imageUrl);
            testResult.setTestTime(LocalDateTime.now());
            
            // 安全地设置各个字段
            JsonNode isGlandFaceNode = analysisNode.path("isGlandFace");
            if (!isGlandFaceNode.isMissingNode()) {
                testResult.setIsGlandFace(isGlandFaceNode.asBoolean());
            }
            
            // 兼容probability和confidence两种字段名
            JsonNode probabilityNode = analysisNode.path("probability");
            JsonNode confidenceNode = analysisNode.path("confidence");
            if (!probabilityNode.isMissingNode()) {
                testResult.setConfidence(probabilityNode.asDouble());
            } else if (!confidenceNode.isMissingNode()) {
                testResult.setConfidence(confidenceNode.asDouble());
            }
            
            JsonNode levelNode = analysisNode.path("level");
            if (!levelNode.isMissingNode()) {
                testResult.setLevel(levelNode.asText());
            }
            
            JsonNode visualizationDescriptionNode = analysisNode.path("visualizationDescription");
            if (!visualizationDescriptionNode.isMissingNode()) {
                testResult.setVisualizationDescription(visualizationDescriptionNode.asText());
            }
            
            return testResult;
        } catch (Exception e) {
            logger.error("解析API返回结果时发生错误: {}", e.getMessage(), e);
            logger.error("原始返回结果: {}", result);
            throw new RuntimeException("结果解析失败: " + e.getMessage());
        }
    }
}