package com.example.arcadesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ArcadeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArcadeApplication.class, args);
    }
}
