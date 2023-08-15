package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import ru.practicum.statsclient.StatsClient;

@SpringBootApplication
public class EwmMainApp {
    public static void main(String[] args) {
        SpringApplication.run(EwmMainApp.class, args);
    }

    @Bean
    public StatsClient getStatsClient(@Value("${ewm-stat_server.url}") String serverUrl,
                                      RestTemplateBuilder restTemplateBuilder) {
        return new StatsClient(serverUrl, restTemplateBuilder);
    }
}