package com.tgy.rtls.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan({"com.tgy.rtls"})
public class LocationApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(LocationApplication.class, args);
       /* KafukaSender kafukaSender = SpringContextHolder.getBean("kafukasender");
        while (true) {
            kafukaSender.send("testDemo", "dasdasdasd"+new Date());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }


}
