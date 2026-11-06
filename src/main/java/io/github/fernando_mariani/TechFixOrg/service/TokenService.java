package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.config.JwtConfiguration;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;

    public Jwt gerar(UsuariosEntity usuario) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        var claims = JwtClaimsSet.builder()
                .issuer(JwtConfiguration.ISSUER)
                .subject(usuario.getId().toString())
                .claim("id", usuario.getId().toString())
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .claim("role", usuario.getRole().name())
                .issuedAt(agora)
                .notBefore(agora)
                .expiresAt(agora.plus(Duration.ofDays(3)))
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims));
    }
}
