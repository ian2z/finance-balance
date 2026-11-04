package br.com.financebalance.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration) {
    public JwtProperties {
        if (secret == null || secret.getBytes().length < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter ao menos 32 caracteres");
        }
        if (expiration == null) {
            expiration = Duration.ofHours(1);
        }
    }
}
