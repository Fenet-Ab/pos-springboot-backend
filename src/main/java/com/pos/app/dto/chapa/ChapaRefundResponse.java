package com.pos.app.dto.chapa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChapaRefundResponse {

    private String status;
    private String message;
    private ChapaRefundData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChapaRefundData {
        private String tx_ref;
        private String amount;
        private String ref_id;
    }
}
