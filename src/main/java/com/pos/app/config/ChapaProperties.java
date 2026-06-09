package com.pos.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "chapa")
public class ChapaProperties {

    private String secretKey;
    private String callbackUrl = "http://localhost:8080/api/sales/chapa/callback";
    private String returnUrl = "http://localhost:8080/api/sales/chapa/return";
}
