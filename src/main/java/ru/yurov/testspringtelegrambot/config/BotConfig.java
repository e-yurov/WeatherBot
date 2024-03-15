package ru.yurov.testspringtelegrambot.config;

import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("classpath:application.yml")
public class BotConfig {
    @Value("${bot.username}")
    private String name;

    @Value("${bot.token}")
    private String token;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
