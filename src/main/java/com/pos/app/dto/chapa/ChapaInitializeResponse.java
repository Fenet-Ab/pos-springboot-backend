package com.pos.app.dto.chapa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChapaInitializeResponse {

    private String status;
    private String message;
    private ChapaData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChapaData {
        private String checkout_url;
    }
}
