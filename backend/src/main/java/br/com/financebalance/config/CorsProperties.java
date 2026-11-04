package br.com.financebalance.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
    public CorsProperties {
        if (allowedOrigins == null) {
            allowedOrigins = List.of();
        }
    }
}
