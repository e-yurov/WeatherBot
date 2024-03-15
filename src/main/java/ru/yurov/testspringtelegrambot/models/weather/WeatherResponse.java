package ru.yurov.testspringtelegrambot.models.weather;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResponse {
    private Weather weather;
    private Main main;

    private String cityName;
    private String countryCode;
    private long currentTime;

    @Getter
    @Setter
    public static class Weather {
        private String description;
    }

    @Getter
    @Setter
    public static class Main {
        private double temperature;
        private double feelsLike;
        private int humidity;
    }
}
