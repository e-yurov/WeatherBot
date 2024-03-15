package ru.yurov.testspringtelegrambot.services.bots;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.yurov.testspringtelegrambot.config.BotConfig;
import ru.yurov.testspringtelegrambot.exceptions.NoSuchCityException;
import ru.yurov.testspringtelegrambot.exceptions.ServerException;
import ru.yurov.testspringtelegrambot.models.User;
import ru.yurov.testspringtelegrambot.models.weather.WeatherResponse;
import ru.yurov.testspringtelegrambot.services.database.UserService;
import ru.yurov.testspringtelegrambot.services.rest.ForecastRestService;
import ru.yurov.testspringtelegrambot.services.rest.WeatherRestService;
import ru.yurov.testspringtelegrambot.utils.MessageBuilder;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class WeatherBot extends TelegramLongPollingBot {
    private final String username;
    private final WeatherRestService weatherService;
    private final ForecastRestService forecastService;
    private final UserService userService;
    private final MessageBuilder messageBuilder;

    @Autowired
    public WeatherBot(BotConfig botConfig, WeatherRestService weatherService, ForecastRestService forecastService,
                      UserService userService, MessageBuilder messageBuilder) {
        super(botConfig.getToken());
        this.username = botConfig.getName();
        this.weatherService = weatherService;
        this.forecastService = forecastService;
        this.userService = userService;
        this.messageBuilder = messageBuilder;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        if (!message.hasText()) {
            return;
        }

        try {
            handleMessage(message);
        } catch (TelegramApiException e) {
            log.error("TelegramApiException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(Message message) throws TelegramApiException {
        if (message.isCommand()) {
            parseCommand(message);
        } else {
            parseCityName(message);
        }
    }

    private void parseCommand(Message message) throws TelegramApiException {
        String[] words = message.getText().split(" ");
        String command = words[0];
        long chatId = message.getChatId();

        switch (command) {
            case "/start" -> sendMessage(chatId, messageBuilder.buildStartMessage(message.getChat().getFirstName()));
            case "/help" -> sendMessage(chatId, messageBuilder.buildHelpMessage());
            case "/select" -> handleSelectCommand(message, words);
            case "/forecast" -> handleForecast(chatId, words);
            case "/subscribe" -> {
                updateSubscription(message.getChat(), true);
                sendMessage(chatId, "Вы успешно подписались на рассылку!");
            }
            case "/unsubscribe" -> {
                updateSubscription(message.getChat(), false);
                sendMessage(chatId, "Вы успешно отписались от рассылки!");
            }
            case "/get" -> handleGetCommand(chatId);
            default -> sendMessage(chatId, "Команда не поддерживается");
        }
    }

    private void handleSelectCommand(Message message, String[] words) throws TelegramApiException {
        long chatId = message.getChatId();
        if (words.length < 2) {
            sendMessage(chatId, "Вы не ввели название города");
        } else {
            updateUser(message, words[1]);
            sendMessage(chatId, "Город установлен");
        }
    }

    private void handleForecast(long chatId, String[] words) throws TelegramApiException {
        if (words.length < 2) {
            sendMessage(chatId, "Вы не ввели количество дней");
            return;
        }

        int days = 1;
        try {
            days = Integer.parseInt(words[1]);
            if (days > 5) {
                throw new Exception();
            }
        } catch (Exception e) {
            sendMessage(chatId, "Неправильное количество дней");
        }

        String message = createForecastMessage(chatId, words, days);
        if (message != null) {
            sendMessage(chatId, message);
        }
    }

    private String createForecastMessage(long chatId, String[] words, int days) throws TelegramApiException {
        String city = null;
        if (words.length > 2) {
            city = words[2];
        } else {
            Optional<User> userOptional = userService.getUserByChatId(chatId);
            if (userOptional.isPresent()) {
                city = userOptional.get().getSelectedCity();
            }
        }

        if (city == null) {
            sendMessage(chatId, "Вы не выбрали город!");
            return null;
        }

        List<WeatherResponse> responses;
        try {
            responses = forecastService.getForecast(city, days);
        } catch (NoSuchCityException | ServerException e) {
            sendMessage(chatId, e.getMessage());
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (!responses.isEmpty()) {
            sb.append(messageBuilder.buildWeatherWithCityMessage(responses.get(0)));
        }
        for (int i = 1; i < responses.size(); i++) {
            sb.append("\n--------------------\n\n").append(messageBuilder.buildWeatherMessage(responses.get(i)));
        }

        return sb.toString();
    }

    private void handleGetCommand(long chatId) throws TelegramApiException {
        Optional<User> userOptional = userService.getUserByChatId(chatId);
        if (userOptional.isPresent()) {
            WeatherResponse weatherResponse = requestWeather(chatId, userOptional.get().getSelectedCity());
            if (weatherResponse != null) {
                sendMessage(chatId, messageBuilder.buildCurrentWeatherMessage(weatherResponse));
            }
        } else {
            sendMessage(chatId, "Вы не выбрали город");
        }
    }

    private void parseCityName(Message message) throws TelegramApiException {
        WeatherResponse weatherResponse = requestWeather(message.getChatId(), message.getText());
        if (weatherResponse != null) {
            String toSend = messageBuilder.buildSearchMessage(weatherResponse);
            execute(new SendMessage(String.valueOf(message.getChatId()), toSend));
        }
    }

    private void sendMessage(long chatId, String message) throws TelegramApiException {
        this.execute(new SendMessage(String.valueOf(chatId), message));
    }

    private void updateUser(Message message, String cityName) {
        Optional<User> userOptional = userService.getUserByChatId(message.getChatId());
        if (userOptional.isPresent()) {
            User userInDatabase = userOptional.get();
            userInDatabase.setSelectedCity(cityName);
            userService.saveUser(userInDatabase);
            return;
        }

        Chat chat = message.getChat();
        User user = new User(
                message.getChatId(),
                cityName,
                false,
                chat.getFirstName(),
                chat.getLastName(),
                chat.getUserName()
        );
        userService.saveUser(user);
    }

    private void updateSubscription(Chat chat, boolean isSubscribed) {
        Optional<User> userOptional = userService.getUserByChatId(chat.getId());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setSubscribed(isSubscribed);
        } else {
            user = new User(
                    chat.getId(),
                    null,
                    isSubscribed,
                    chat.getFirstName(),
                    chat.getLastName(),
                    chat.getUserName()
            );
        }

        userService.saveUser(user);
    }

    private WeatherResponse requestWeather(long chatId, String city) throws TelegramApiException {
        WeatherResponse weatherResponse;
        try {
            weatherResponse = weatherService.getWeather(city);
        } catch (NoSuchCityException | ServerException exception) {
            execute(new SendMessage(String.valueOf(chatId), exception.getMessage()));
            return null;
        }

        return weatherResponse;
    }

    private void test() {
        var msg = new SendMessage();
    }

    @Scheduled(cron = "* 55 23 * * *")
    //@Scheduled(fixedDelay = 5_000L)
    private void sendScheduledForecast() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            if (user.isSubscribed()) {
                try {
                    handleForecast(user.getChatId(), new String[]{"/forecast", "1"});
                } catch (TelegramApiException e) {
                    log.error("TelegramApiException: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }
}
