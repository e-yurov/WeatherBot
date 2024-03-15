package ru.yurov.testspringtelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestSpringTelegramBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestSpringTelegramBotApplication.class, args);
	}
}
