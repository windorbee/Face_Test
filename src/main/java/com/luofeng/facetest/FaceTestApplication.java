package com.luofeng.facetest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.luofeng.facetest.mapper")
public class FaceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaceTestApplication.class, args);
    }

}