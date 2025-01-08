package com.tgy.camerademo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = {"com.tgy.camerademo.mapper"})
public class CamerademoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamerademoApplication.class, args);
    }

}
