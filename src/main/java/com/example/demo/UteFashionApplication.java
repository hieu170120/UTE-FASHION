package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * UTE Fashion - Website Shop Thời Trang Online
 * Main Application Class
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class UteFashionApplication {

    public static void main(String[] args) {
    	System.setProperty("file.encoding", "UTF-8");
        SpringApplication.run(UteFashionApplication.class, args);
        System.out.println("===========================================");
        System.out.println("UTE Fashion Application Started!");
        System.out.println("Access: http://localhost:5055/UTE_Fashion/");
        System.out.println("===========================================");
    }
}
