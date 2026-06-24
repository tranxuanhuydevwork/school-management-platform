package com.golearn.myf3school_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class Myf3schoolBackendApplication {
    public static void main(String[] args) {
        // In ra hash rồi copy vào DB
        System.out.println(new BCryptPasswordEncoder(12).encode("123456"));
        SpringApplication.run(Myf3schoolBackendApplication.class, args);
    }
}
