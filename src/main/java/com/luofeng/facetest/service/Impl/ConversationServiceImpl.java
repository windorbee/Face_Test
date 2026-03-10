package com.luofeng.facetest.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luofeng.facetest.mapper.ConversationMapper;
import com.luofeng.facetest.mapper.MessageMapper;
import com.luofeng.facetest.pojo.Conversation;
import com.luofeng.facetest.pojo.Message;
import com.luofeng.facetest.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationServiceImpl implements ConversationService {
    
    @Value("${doubao.api-key:ba035a41-ddbe-417d-a89c-1a679a5f79cc}")
    private String apiKey;
    
    @Value("${doubao.model-name:doubao-seed-1-6-vision-250815}")
    private String modelName;
    
    @Autowired
    private ConversationMapper conversationMapper;
    
    @Autowired
    private MessageMapper messageMapper;
    
    /**
     * 创建新的对话
     * @param userId 用户ID
     * @param firstMessage 首条消息内容，用于生成标题
     * @return 对话对象
     */
    @Override
    public Conversation createConversation(Integer userId, String firstMessage) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        // 从首条消息提取标题，如果消息太长则截取前20个字符
        String title = firstMessage.length() > 20 ? firstMessage.substring(0, 20) + "..." : firstMessage;
        conversation.setTitle(title);
        conversation.setStartTime(LocalDateTime.now());
        conversation.setLastUpdateTime(LocalDateTime.now());
        conversation.setStatus(1); // 1=活跃
        
        conversationMapper.insertConversation(conversation);
        return conversation;
    }
    
    /**
     * 发送消息并获取AI回复
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param content 用户消息内容
     * @return AI回复内容
     * @throws Exception 处理异常
     */
    @Override
    public String sendMessage(Integer conversationId, Integer userId, String content) throws Exception {
        // 保存用户消息
        Message userMessage = new Message();
        userMessage.setConversationId(conversationId);
        userMessage.setSender(0); // 0=用户
        userMessage.setContent(content);
        userMessage.setSendTime(LocalDateTime.now());
        messageMapper.insertMessage(userMessage);
        
        // 获取对话历史
        List<Message> historyMessages = messageMapper.selectMessagesByConversationId(conversationId);
        
        // 调用豆包API获取AI回复
        String aiResponse = callDoubaoAPI(historyMessages);
        
        // 保存AI回复消息
        Message aiMessage = new Message();
        aiMessage.setConversationId(conversationId);
        aiMessage.setSender(1); // 1=豆包助手
        aiMessage.setContent(aiResponse);
        aiMessage.setSendTime(LocalDateTime.now());
        messageMapper.insertMessage(aiMessage);
        
        // 更新对话的最后更新时间
        Conversation conversation = conversationMapper.selectConversationById(conversationId);
        if (conversation != null) {
            conversation.setLastUpdateTime(LocalDateTime.now());
            conversationMapper.updateConversation(conversation);
        }
        
        return aiResponse;
    }
    
    /**
     * 流式发送消息并获取AI回复
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param content 用户消息内容
     * @param outputStream 输出流
     * @throws Exception 处理异常
     */
    @Override
    public void streamMessage(Integer conversationId, Integer userId, String content, OutputStream outputStream) throws Exception {
        // 保存用户消息
        Message userMessage = new Message();
        userMessage.setConversationId(conversationId);
        userMessage.setSender(0); // 0=用户
        userMessage.setContent(content);
        userMessage.setSendTime(LocalDateTime.now());
        messageMapper.insertMessage(userMessage);
        
        // 获取对话历史
        List<Message> historyMessages = messageMapper.selectMessagesByConversationId(conversationId);
        
        // 调用豆包API获取AI回复（流式）
        String aiResponse = callDoubaoStreamAPI(historyMessages, outputStream);
        
        // 保存AI回复消息
        Message aiMessage = new Message();
        aiMessage.setConversationId(conversationId);
        aiMessage.setSender(1); // 1=豆包助手
        aiMessage.setContent(aiResponse);
        aiMessage.setSendTime(LocalDateTime.now());
        messageMapper.insertMessage(aiMessage);
        
        // 更新对话的最后更新时间
        Conversation conversation = conversationMapper.selectConversationById(conversationId);
        if (conversation != null) {
            conversation.setLastUpdateTime(LocalDateTime.now());
            conversationMapper.updateConversation(conversation);
        }
    }
    
    /**
     * 根据用户ID查询对话列表
     * @param userId 用户ID
     * @return 对话列表
     */
    @Override
    public List<Conversation> getConversationsByUserId(Integer userId) {
        return conversationMapper.selectConversationsByUserId(userId);
    }
    
    /**
     * 根据对话ID查询消息列表
     * @param conversationId 对话ID
     * @return 消息列表
     */
    @Override
    public List<Message> getMessagesByConversationId(Integer conversationId) {
        return messageMapper.selectMessagesByConversationId(conversationId);
    }
    
    /**
     * 调用豆包大模型API
     * @param historyMessages 历史消息
     * @return API返回结果
     * @throws Exception 处理异常
     */
    private String callDoubaoAPI(List<Message> historyMessages) throws Exception {
        String url = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
        
        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        // 准备请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        
        // 构建对话历史
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 添加系统角色提示
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个腺体面容专家医生，请以专业医生的身份回答用户关于腺体面容的问题。(不要回复除了腺体面容之外的问题,然后不要语句中不要有很多奇怪的符号,且语言通俗易懂)");
        messages.add(systemMessage);
        
        // 添加历史对话
        for (Message msg : historyMessages) {
            Map<String, Object> message = new HashMap<>();
            if (msg.getSender() == 0) { // 用户消息
                message.put("role", "user");
            } else { // AI助手消息
                message.put("role", "assistant");
            }
            message.put("content", msg.getContent());
            messages.add(message);
        }
        
        requestBody.put("messages", messages);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            // 解析API返回结果
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            JsonNode choicesNode = rootNode.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode messageNode = choicesNode.get(0).path("message");
                JsonNode contentNode = messageNode.path("content");
                return contentNode.asText();
            }
            throw new RuntimeException("无法解析API返回结果");
        } else {
            throw new RuntimeException("调用豆包API失败: " + response.getStatusCode());
        }
    }
    
    /**
     * 调用豆包大模型API（流式）
     * @param historyMessages 历史消息
     * @param outputStream 输出流
     * @return 完整的AI回复内容
     * @throws Exception 处理异常
     */
    private String callDoubaoStreamAPI(List<Message> historyMessages, OutputStream outputStream) throws Exception {
        String url = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
        
        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        // 准备请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("stream", true); // 启用流式输出
        
        // 构建对话历史
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 添加系统角色提示
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个腺体面容专家医生，请以专业医生的身份回答用户关于腺体面容的问题。(不要回复除了腺体面容之外的问题,然后不要语句中不要有很多奇怪的符号,且语言通俗易懂)");
        messages.add(systemMessage);
        
        // 添加历史对话
        for (Message msg : historyMessages) {
            Map<String, Object> message = new HashMap<>();
            if (msg.getSender() == 0) { // 用户消息
                message.put("role", "user");
            } else { // AI助手消息
                message.put("role", "assistant");
            }
            message.put("content", msg.getContent());
            messages.add(message);
        }
        
        requestBody.put("messages", messages);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        
        StringBuilder fullResponse = new StringBuilder();
        
        if (response.getStatusCode() == HttpStatus.OK) {
            // 按行处理响应
            String responseBody = response.getBody();
            if (responseBody != null) {
                // 按行分割响应内容
                String[] lines = responseBody.split("\n");
                for (String line : lines) {
                    // 处理流式响应行
                    if (line.startsWith("data:")) {
                        String data = line.substring(6); // 跳过 "data: " 前缀
                        if (!"[DONE]".equals(data)) {
                            try {
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = objectMapper.readTree(data);
                                JsonNode choicesNode = rootNode.path("choices");
                                if (choicesNode.isArray() && choicesNode.size() > 0) {
                                    JsonNode deltaNode = choicesNode.get(0).path("delta");
                                    JsonNode contentNode = deltaNode.path("content");
                                    if (contentNode != null && !contentNode.asText().isEmpty()) {
                                        String content = contentNode.asText();
                                        fullResponse.append(content);
                                        // 发送流式数据到前端，确保使用UTF-8编码
                                        String output = "data: " + objectMapper.writeValueAsString(content) + "\n\n";
                                        outputStream.write(output.getBytes(StandardCharsets.UTF_8));
                                        outputStream.flush();
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略解析错误，继续处理下一行
                                System.err.println("解析流式响应出错: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("调用豆包API失败: " + response.getStatusCode());
        }
        
        return fullResponse.toString();
    }
}