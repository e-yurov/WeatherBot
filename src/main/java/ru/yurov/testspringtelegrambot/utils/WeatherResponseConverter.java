package ru.yurov.testspringtelegrambot.utils;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yurov.testspringtelegrambot.dto.ForecastResponseDTO;
import ru.yurov.testspringtelegrambot.dto.GeocoderResponseDTO;
import ru.yurov.testspringtelegrambot.dto.WeatherResponseDTO;
import ru.yurov.testspringtelegrambot.models.weather.WeatherResponse;

import java.util.List;

@Component
public class WeatherResponseConverter {
    private final ModelMapper modelMapper;

    @Autowired
    public WeatherResponseConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public WeatherResponse convertSingle(WeatherResponseDTO weatherResponseDTO, GeocoderResponseDTO geocoderResponseDTO) {
        WeatherResponse result = new WeatherResponse();

        modelMapper.map(weatherResponseDTO, result);
        result.setWeather(modelMapper.map(weatherResponseDTO.getWeatherDTO().get(0), WeatherResponse.Weather.class));
        result.setMain(modelMapper.map(weatherResponseDTO.getMainDTO(), WeatherResponse.Main.class));

        var localNamesDTO = geocoderResponseDTO.getLocalNamesDTO();
        if (localNamesDTO == null || localNamesDTO.getRussian() == null) {
            result.setCityName(weatherResponseDTO.getDefaultName());
        } else {
            result.setCityName(localNamesDTO.getRussian());
        }
        result.setCountryCode(geocoderResponseDTO.getCountryCode());

        return result;
    }

    public List<WeatherResponse> convertForecast(ForecastResponseDTO forecast, GeocoderResponseDTO geocoderResponseDTO) {
        return forecast.getWeatherResponseDTOs().stream()
                .map(elem -> convertSingle(elem, geocoderResponseDTO))
                .toList();
    }
}
