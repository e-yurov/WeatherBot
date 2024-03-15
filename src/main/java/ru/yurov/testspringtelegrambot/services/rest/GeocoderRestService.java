package ru.yurov.testspringtelegrambot.services.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.yurov.testspringtelegrambot.dto.GeocoderResponseDTO;
import ru.yurov.testspringtelegrambot.exceptions.NoSuchCityException;
import ru.yurov.testspringtelegrambot.exceptions.ServerException;
import ru.yurov.testspringtelegrambot.utils.UrlBuilder;

import java.util.Arrays;

@Component
@Slf4j
public class GeocoderRestService {
    private final UrlBuilder urlBuilder;

    public GeocoderRestService(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    public GeocoderResponseDTO getCoordinates(String city) throws NoSuchCityException, ServerException {
        String url = urlBuilder.buildGeocodingUrl(city);
        RestTemplate restTemplate = new RestTemplate();

        GeocoderResponseDTO[] responses;
        try {
            responses = restTemplate.getForObject(url, GeocoderResponseDTO[].class);
        } catch (RestClientException e) {
            log.error("GeocoderAPI exception: " + e.getMessage());
            throw new ServerException();
        }
        if (responses == null || responses.length == 0) {
            throw new NoSuchCityException("Извините, по вашему запросу не нашлось ни одного подходящего города.");
        }
        return responses[0];
    }
}
