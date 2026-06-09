package com.pos.app.dto.chapa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChapaVerifyResponse {

    private String status;
    private String message;
    private ChapaVerifyData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChapaVerifyData {
        private String status;
    }
}
