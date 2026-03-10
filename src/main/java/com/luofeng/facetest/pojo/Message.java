package com.luofeng.facetest.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private Integer id;
    private Integer conversationId;
    private Integer sender; // 0=用户，1=豆包助手
    private String content;
    private LocalDateTime sendTime;
}