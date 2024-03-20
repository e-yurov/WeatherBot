package ru.yurov.testspringtelegrambot.utils;

import org.springframework.stereotype.Component;
import ru.yurov.testspringtelegrambot.models.weather.WeatherResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class MessageBuilder {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("H:mm\nd MMMM yyyy");

    public String buildStartMessage(String firstName) {
        return "Здравствуйте, " + firstName + ". Используйте /help для получения подробной справки";
    }

    public String buildHelpMessage() {
        return "Данный бот используется для того, чтобы узнать погоду в любом городе мира. " +
               "Чтобы использовать его, просто отправьте ему название города. " +
               "Кроме того, поддерживаются комманды:\n\n" +

               "/start - приветствие и начало работы\n" +
               "/help - вывод этого сообщения\n" +
               "/select - установить ваш город. Он будет использоваться для быстрого получения погоды и рассылки." +
               "Для установки нужно ввести название города в следующем сообщении\n" +
               "/forecast - получить прогноз погоды. Необходимо выбрать количество дней (не более 5) " +
               "и указать город в следующем сообщении." +
               "Прогноз выводится в виде измерений с периодичностью 3 часа.\n" +
               "/subscribe - подписаться на рассылку. Каждый день в 23:55 будет приходить прогноз погоды на следующий день.\n" +
               "/unsubscribe - отписаться от рассылки\n" +
               "/get - узнать погоду в вашем городе\n";
    }

    public String buildSearchMessage(WeatherResponse weatherResponse) {
        return "Вот что удалось найти по вашему запросу:\n\n" +
                buildWeatherWithCityMessage(weatherResponse);
    }

    public String buildCurrentWeatherMessage(WeatherResponse weatherResponse) {
        return "Погода на текущий момент:\n\n" +
               buildWeatherWithCityMessage(weatherResponse);
    }

    public String buildWeatherWithCityMessage(WeatherResponse weatherResponse) {
        return "Город: " + weatherResponse.getCityName() + "\n" +
                "Код страны: " + weatherResponse.getCountryCode() + "\n\n" +

                buildWeatherMessage(weatherResponse);
    }

    public String buildWeatherMessage(WeatherResponse weatherResponse) {
        WeatherResponse.Main main = weatherResponse.getMain();
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(weatherResponse.getCurrentTime() * 1000L),
                ZoneId.of("Europe/Moscow")
        );
        return
                dateTime.format(dateTimeFormatter) + "\n\n" +
                "Погода: " + weatherResponse.getWeather().getDescription() + "\n" +
                "Температура " + main.getTemperature() + "°C\n" +
                "Ощущается как " + main.getFeelsLike() + "°C\n" +
                "Влажность " + main.getHumidity() + "%\n";
    }
}
