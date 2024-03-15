package ru.yurov.testspringtelegrambot.services.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.yurov.testspringtelegrambot.dto.GeocoderResponseDTO;
import ru.yurov.testspringtelegrambot.dto.WeatherResponseDTO;
import ru.yurov.testspringtelegrambot.exceptions.NoSuchCityException;
import ru.yurov.testspringtelegrambot.exceptions.ServerException;
import ru.yurov.testspringtelegrambot.models.weather.WeatherResponse;
import ru.yurov.testspringtelegrambot.utils.UrlBuilder;
import ru.yurov.testspringtelegrambot.utils.WeatherResponseConverter;

@Component
@Slf4j
public class WeatherRestService {
    private final GeocoderRestService geocoderService;
    private final UrlBuilder urlBuilder;
    private final WeatherResponseConverter weatherResponseConverter;

    @Autowired
    public WeatherRestService(GeocoderRestService geocoderService, UrlBuilder urlBuilder,
                              WeatherResponseConverter weatherResponseConverter) {
        this.geocoderService = geocoderService;
        this.urlBuilder = urlBuilder;
        this.weatherResponseConverter = weatherResponseConverter;
    }

    public WeatherResponse getWeather(String city) throws NoSuchCityException, ServerException {
        GeocoderResponseDTO geocoderResponseDTO = geocoderService.getCoordinates(city);
        String url = urlBuilder.buildWeatherUrl(geocoderResponseDTO.getLatitude(), geocoderResponseDTO.getLongitude());
        RestTemplate restTemplate = new RestTemplate();

        WeatherResponseDTO weatherResponseDTO;
        try {
            weatherResponseDTO = restTemplate.getForObject(url, WeatherResponseDTO.class);
        } catch (RestClientException e) {
            log.error("OpenWeatherAPI exception: " + e.getMessage());
            throw new ServerException();
        }
        return weatherResponseConverter.convertSingle(weatherResponseDTO, geocoderResponseDTO);
    }
}
