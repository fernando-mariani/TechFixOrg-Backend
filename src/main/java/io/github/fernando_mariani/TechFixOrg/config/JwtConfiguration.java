package io.github.fernando_mariani.TechFixOrg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class JwtConfiguration {

    public static final String ISSUER = "TechFixOrg";

    @Bean
    public SecretKey jwtSecretKey(@Value("${security.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret deve conter pelo menos 32 bytes em UTF-8.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        var decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> claimsObrigatorios = jwt -> {
            if (jwt.getExpiresAt() == null || jwt.getIssuedAt() == null
                    || jwt.getSubject() == null || jwt.getSubject().isBlank()
                    || !jwt.getExpiresAt().isAfter(jwt.getIssuedAt())
                    || Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).compareTo(Duration.ofDays(3)) > 0) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Claims do token inválidos.", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO),
                new JwtIssuerValidator(ISSUER),
                claimsObrigatorios));
        return decoder;
    }
}
