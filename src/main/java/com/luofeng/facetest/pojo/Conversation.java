package com.luofeng.facetest.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Conversation {
    private Integer id;
    private Integer userId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime lastUpdateTime;
    private Integer status; // 1=活跃，0=已结束
}