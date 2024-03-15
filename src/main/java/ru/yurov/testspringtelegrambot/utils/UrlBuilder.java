package ru.yurov.testspringtelegrambot.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlBuilder {
    private final String apiKey;

    public UrlBuilder(@Value("${openweather.api.token}")String apiKey) {
        this.apiKey = apiKey;
    }

    public String buildGeocodingUrl(String cityName) {
        return "https://api.openweathermap.org/geo/1.0/direct?" +
                "q=" + cityName +
                "&limit=1" +
                "&appid=" + apiKey;
    }

    public String buildWeatherUrl(String latitude, String longitude) {
        return "https://api.openweathermap.org/data/2.5/weather?" +
                buildWeatherParams(latitude, longitude);
    }

    public String buildForecastUrl(String latitude, String longitude, int count) {
        return "https://api.openweathermap.org/data/2.5/forecast?" +
                buildWeatherParams(latitude, longitude) +
                "&cnt=" + count;
    }

    private String buildWeatherParams(String latitude, String longitude) {
        return "lat=" + latitude +
                "&lon=" + longitude +
                "&appid=" + apiKey +
                "&units=metric" +
                "&lang=ru";
    }
}
