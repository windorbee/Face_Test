package com.luofeng.facetest.controller;

import com.luofeng.facetest.pojo.Conversation;
import com.luofeng.facetest.pojo.Message;
import com.luofeng.facetest.pojo.Result;
import com.luofeng.facetest.service.ConversationService;
import com.luofeng.facetest.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.util.List;

@RestController
@RequestMapping("/conversation")
public class ConversationController {
    
    @Autowired
    private ConversationService conversationService;
    
    /**
     * 创建新的对话
     * @param firstMessage 首条消息内容
     * @return 对话对象
     */
    @PostMapping("/create")
    public Result createConversation(@RequestParam String firstMessage) {
        try {
            // 从UserThreadLocal获取用户ID
            Integer userId = UserThreadLocal.getUserId();
            Conversation conversation = conversationService.createConversation(userId, firstMessage);
            return Result.success(conversation);
        } catch (Exception e) {
            return Result.error("创建对话失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送消息并获取AI回复
     * @param conversationId 对话ID
     * @param content 消息内容
     * @return AI回复内容
     */
    @PostMapping("/send")
    public Result sendMessage(@RequestParam Integer conversationId,
                            @RequestParam String content) {
        try {
            // 从UserThreadLocal获取用户ID
            Integer userId = UserThreadLocal.getUserId();
            String response = conversationService.sendMessage(conversationId, userId, content);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送消息并流式获取AI回复
     * @param conversationId 对话ID
     * @param content 消息内容
     * @return 流式响应
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamMessage(@RequestParam Integer conversationId,
                                                               @RequestParam String content) {
        try {
            // 从UserThreadLocal获取用户ID
            Integer userId = UserThreadLocal.getUserId();
            
            StreamingResponseBody responseBody = outputStream -> {
                try {
                    conversationService.streamMessage(conversationId, userId, content, outputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .header("Cache-Control", "no-cache")
                    .header("X-Accel-Buffering", "no")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "text/event-stream;charset=UTF-8")
                    .body(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取用户对话列表
     * @return 对话列表
     */
    @GetMapping("/list")
    public Result getConversations() {
        try {
            // 从UserThreadLocal获取用户ID
            Integer userId = UserThreadLocal.getUserId();
            List<Conversation> conversations = conversationService.getConversationsByUserId(userId);
            return Result.success(conversations);
        } catch (Exception e) {
            return Result.error("获取对话列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取对话消息列表
     * @param conversationId 对话ID
     * @return 消息列表
     */
    @GetMapping("/messages")
    public Result getMessages(@RequestParam Integer conversationId) {
        try {
            List<Message> messages = conversationService.getMessagesByConversationId(conversationId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取消息列表失败: " + e.getMessage());
        }
    }
}