package com.example.bardakv1bot;

import com.example.bardakv1bot.telegram.TelegramProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelegramProperties.class)
public class BardakV1BotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BardakV1BotApplication.class, args);
    }

}
