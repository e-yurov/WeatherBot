package ru.yurov.testspringtelegrambot.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.yurov.testspringtelegrambot.services.bot.WeatherBot;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotInitializer {
    private final WeatherBot bot;

    public BotInitializer(WeatherBot bot) {
        this.bot = bot;
    }


    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
            bot.execute(SetMyCommands.builder()
                    .commands(createBotCommands())
                    .scope(new BotCommandScopeDefault())
                    .build());
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private List<BotCommand> createBotCommands() {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "Приветствие"));
        botCommands.add(new BotCommand("/help", "Помощь"));
        botCommands.add(new BotCommand("/select", "Выбрать текущий город"));
        botCommands.add(new BotCommand("/forecast", "Прогноз погоды"));
        botCommands.add(new BotCommand("/subscribe", "Подписаться на рассылку"));
        botCommands.add(new BotCommand("/unsubscribe", "Отписаться от рассылки"));
        botCommands.add(new BotCommand("/get", "Узнать погоду сейчас"));
        botCommands.add(new BotCommand("/logout", "Очистить свои данные"));

        return botCommands;
    }
}
