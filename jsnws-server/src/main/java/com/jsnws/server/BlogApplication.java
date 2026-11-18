package com.jsnws.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * jsnws 博客后台启动类，对接 jsnws-ui-v3 前端（baseURL: http://localhost:8081）
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.jsnws.server.mapper")
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
