package ru.yurov.testspringtelegrambot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ForecastResponseDTO {
    @JsonProperty("list")
    private List<WeatherResponseDTO> weatherResponseDTOs;
}
