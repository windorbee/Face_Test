package com.luofeng.facetest.pojo;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private LocalDateTime createTime;
    private String avatar;
    private String name;
    private String email;
    private String phone;
    private Integer gender; // 0-未知，1-男，2-女
    private LocalDate birthday;
    private String address;
}