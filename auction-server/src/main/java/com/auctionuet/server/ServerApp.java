package com.auctionuet.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;//bật cơ chế chạy lịch(@Scheduled)

@SpringBootApplication//đánh dấu đây là ứng dụng Spring Boot(quét bean, cấu hình mặc định)
@EnableScheduling// cho phép AuctionTimerTask.tick() chạy định kỳ
public class ServerApp {
    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class,args);
    }
}
