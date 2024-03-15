package ru.yurov.testspringtelegrambot.services.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.yurov.testspringtelegrambot.dto.ForecastResponseDTO;
import ru.yurov.testspringtelegrambot.dto.GeocoderResponseDTO;
import ru.yurov.testspringtelegrambot.dto.WeatherResponseDTO;
import ru.yurov.testspringtelegrambot.exceptions.NoSuchCityException;
import ru.yurov.testspringtelegrambot.exceptions.ServerException;
import ru.yurov.testspringtelegrambot.models.weather.WeatherResponse;
import ru.yurov.testspringtelegrambot.utils.UrlBuilder;
import ru.yurov.testspringtelegrambot.utils.WeatherResponseConverter;

import java.util.List;

@Component
@Slf4j
public class ForecastRestService {
    private final GeocoderRestService geocoderService;
    private final UrlBuilder urlBuilder;
    private final WeatherResponseConverter weatherResponseConverter;

    @Autowired
    public ForecastRestService(GeocoderRestService geocoderService, UrlBuilder urlBuilder, WeatherResponseConverter weatherResponseConverter) {
        this.geocoderService = geocoderService;
        this.urlBuilder = urlBuilder;
        this.weatherResponseConverter = weatherResponseConverter;
    }

    public List<WeatherResponse> getForecast(String city, int days) throws NoSuchCityException, ServerException {
        GeocoderResponseDTO geocoderResponseDTO = geocoderService.getCoordinates(city);
        String url = urlBuilder.buildForecastUrl(geocoderResponseDTO.getLatitude(),
                geocoderResponseDTO.getLongitude(), days * 8);
        RestTemplate restTemplate = new RestTemplate();

        ForecastResponseDTO forecastResponseDTO;
        try {
            forecastResponseDTO = restTemplate.getForObject(url, ForecastResponseDTO.class);
            return weatherResponseConverter.convertForecast(forecastResponseDTO, geocoderResponseDTO);
        } catch (RestClientException e) {
            log.error("OpenWeatherAPI forecast exception: " + e.getMessage());
            throw new ServerException();
        }
    }
}
