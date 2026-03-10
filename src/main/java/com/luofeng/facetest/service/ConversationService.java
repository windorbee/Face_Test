package com.luofeng.facetest.service;

import com.luofeng.facetest.pojo.Conversation;
import com.luofeng.facetest.pojo.Message;

import java.io.OutputStream;
import java.util.List;

public interface ConversationService {
    
    /**
     * 创建新的对话
     * @param userId 用户ID
     * @param firstMessage 首条消息内容，用于生成标题
     * @return 对话对象
     */
    Conversation createConversation(Integer userId, String firstMessage);
    
    /**
     * 发送消息并获取AI回复
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param content 用户消息内容
     * @return AI回复内容
     * @throws Exception 处理异常
     */
    String sendMessage(Integer conversationId, Integer userId, String content) throws Exception;
    
    /**
     * 流式发送消息并获取AI回复
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param content 用户消息内容
     * @param outputStream 输出流
     * @throws Exception 处理异常
     */
    void streamMessage(Integer conversationId, Integer userId, String content, OutputStream outputStream) throws Exception;
    
    /**
     * 根据用户ID查询对话列表
     * @param userId 用户ID
     * @return 对话列表
     */
    List<Conversation> getConversationsByUserId(Integer userId);
    
    /**
     * 根据对话ID查询消息列表
     * @param conversationId 对话ID
     * @return 消息列表
     */
    List<Message> getMessagesByConversationId(Integer conversationId);
}