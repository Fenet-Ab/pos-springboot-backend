package com.pos.app.dto.chapa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChapaRefundVerifyResponse {

    private String status;
    private String message;
    private ChapaRefundVerifyData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChapaRefundVerifyData {
        private String ref_id;
        private String status;
        private String amount;
        private String currency;
    }
}
