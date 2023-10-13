package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class EwmMainApp {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        SpringApplication.run(EwmMainApp.class, args);
    }
}