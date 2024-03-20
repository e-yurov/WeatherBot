package ru.yurov.testspringtelegrambot.services.bot;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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

import java.util.ArrayList;
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

    private boolean isForecast = false;
    private boolean isSelect = false;

    private int forecastDays = 0;

    private static final String FORECAST_CALLBACK_PREFIX = "FORECAST_";

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
        try {
            if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
                return;
            } else if (!update.hasMessage()) {
                return;
            }

            Message message = update.getMessage();
            if (!message.hasText()) {
                return;
            }

            handleMessage(message);
        } catch (TelegramApiException e) {
            log.error("TelegramApiException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) throws TelegramApiException {
        String data = callbackQuery.getData();
        if (data.startsWith(FORECAST_CALLBACK_PREFIX)) {
            data = data.substring(FORECAST_CALLBACK_PREFIX.length());
            forecastDays = ForecastDays.valueOf(data).getValue();

            sendMessage(callbackQuery.getFrom().getId(), "Введите город:");
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
            case "/select" -> handleSelectCommand(chatId);
            case "/forecast" -> handleForecast(chatId);
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

    private void handleSelectCommand(long chatId) throws TelegramApiException {
        sendMessage(chatId, "Введите город:");
        isSelect = true;
    }

    private String createForecastMessage(long chatId, String city) throws TelegramApiException {
        List<WeatherResponse> responses;
        try {
            responses = forecastService.getForecast(city, forecastDays);
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

    private void handleForecast(long chatId) throws TelegramApiException {
        var message = SendMessage.builder()
                .chatId(chatId)
                .text("На сколько дней вы хотите прогноз?")
                .replyMarkup(createForecastDaysKeyboard())
                .build();
        this.execute(message);
        isForecast = true;
    }

    private InlineKeyboardMarkup createForecastDaysKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        var button1 = new InlineKeyboardButton();
        button1.setCallbackData(ForecastDays.ONE.toString());
        button1.setText(EmojiParser.parseToUnicode(":one:"));
        var button2 = new InlineKeyboardButton();
        button2.setCallbackData(ForecastDays.TWO.toString());
        button2.setText(EmojiParser.parseToUnicode(":two:"));
        var button3 = new InlineKeyboardButton();
        button3.setCallbackData(ForecastDays.THREE.toString());
        button3.setText(EmojiParser.parseToUnicode(":three:"));
        var button4 = new InlineKeyboardButton();
        button4.setCallbackData(ForecastDays.FOUR.toString());
        button4.setText(EmojiParser.parseToUnicode(":four:"));
        var button5 = new InlineKeyboardButton();
        button5.setCallbackData(ForecastDays.FIVE.toString());
        button5.setText(EmojiParser.parseToUnicode(":five:"));

        firstRow.add(button1);
        firstRow.add(button2);

        secondRow.add(button3);
        secondRow.add(button4);
        secondRow.add(button5);

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        markup.setKeyboard(keyboard);

        return markup;
    }

    private void handleGetCommand(long chatId) throws TelegramApiException {
        Optional<User> userOptional = userService.getUserByChatId(chatId);
        if (userOptional.isPresent() && userOptional.get().getSelectedCity() != null) {
            WeatherResponse weatherResponse = requestWeather(chatId, userOptional.get().getSelectedCity());
            if (weatherResponse != null) {
                sendMessage(chatId, messageBuilder.buildCurrentWeatherMessage(weatherResponse));
            }
        } else {
            sendMessage(chatId, "Вы не выбрали город");
        }
    }

    private void parseCityName(Message message) throws TelegramApiException {
        if (isForecast) {
            String forecastMessage = createForecastMessage(message.getChatId(), message.getText());
            if (forecastMessage != null) {
                sendMessage(message.getChatId(), forecastMessage);
                isForecast = false;
            }
        } else if (isSelect) {
            updateUser(message, message.getText());
            isSelect = false;
            sendMessage(message.getChatId(), "Город установлен");
        } else {
            WeatherResponse weatherResponse = requestWeather(message.getChatId(), message.getText());
            if (weatherResponse != null) {
                String toSend = messageBuilder.buildSearchMessage(weatherResponse);
                execute(new SendMessage(String.valueOf(message.getChatId()), toSend));
            }
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

    @Scheduled(cron = "* 55 23 * * *")
    //@Scheduled(fixedDelay = 5_000L)
    private void sendScheduledForecast() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            if (user.isSubscribed()) {
                try {
                    String city = user.getSelectedCity();
                    if (city == null || city.isBlank()) {
                        sendMessage(user.getChatId(), "Вы не выбрали город!");
                        return;
                    }

                    String forecastMessage = createForecastMessage(user.getChatId(), city);
                    if (forecastMessage != null) {
                        sendMessage(user.getChatId(), forecastMessage);
                    }
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
