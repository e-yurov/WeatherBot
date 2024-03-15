package ru.yurov.testspringtelegrambot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeocoderResponseDTO {
    @JsonProperty("lat")
    private String latitude;
    @JsonProperty("lon")
    private String longitude;
    @JsonProperty("local_names")
    private LocalNamesDTO localNamesDTO;
    @JsonProperty("country")
    private String countryCode;

    @Getter
    @Setter
    public static class LocalNamesDTO {
        @JsonProperty("ru")
        private String russian;
    }
}
