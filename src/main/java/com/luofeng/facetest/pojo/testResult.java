package com.luofeng.facetest.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class testResult {
    private Integer id;
    private Integer userId;
    private String imagePath;
    private Boolean isGlandFace;
    private String level;
    private Double confidence;
    private String visualizationDescription;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    // 兼容数据库字段名
    private LocalDateTime testTime;
    
    // 用于序列化时返回createTime字段
    public LocalDateTime getCreateTime() {
        return testTime;
    }
}