package ru.yurov.testspringtelegrambot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeatherResponseDTO {
    @JsonProperty("weather")
    private List<WeatherDTO> weatherDTO;
    @JsonProperty("main")
    private MainDTO mainDTO;
    @JsonProperty("name")
    private String defaultName;
    @JsonProperty("dt")
    private long currentTime;

    @Getter
    @Setter
    public static class WeatherDTO {
        @JsonProperty("description")
        private String description;
    }

    @Getter
    @Setter
    public static class MainDTO {
        @JsonProperty("temp")
        private double temperature;
        @JsonProperty("feels_like")
        private double feelsLike;
        @JsonProperty("humidity")
        private int humidity;
    }
}
