package ru.kitaev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("ru.kitaev.*")
@EnableFeignClients
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}