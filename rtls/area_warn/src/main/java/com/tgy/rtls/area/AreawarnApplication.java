package com.tgy.rtls.area;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author 许强
 * @Package com.tgy.rtls.area
 * @date 2020/10/29
 */
@SpringBootApplication
@ComponentScan(value = "com.tgy.rtls")
public class AreawarnApplication {
    public static void main(String[] args) {
        SpringApplication.run(AreawarnApplication.class, args);
    }

}
