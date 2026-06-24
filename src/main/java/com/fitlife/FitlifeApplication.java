package com.fitlife;

import org.springframework.boot.SpringApplication; // <-- Dòng này được thêm vào để sửa lỗi
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class FitlifeApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitlifeApplication.class, args);
    }
}