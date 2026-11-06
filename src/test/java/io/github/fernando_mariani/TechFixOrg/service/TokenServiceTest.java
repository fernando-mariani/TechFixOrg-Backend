package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.config.JwtConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private final JwtConfiguration configuration = new JwtConfiguration();
    private final javax.crypto.SecretKey key =
            configuration.jwtSecretKey("segredo-de-testes-com-pelo-menos-32-bytes");
    private final JwtEncoder encoder = configuration.jwtEncoder(key);
    private final JwtDecoder decoder = configuration.jwtDecoder(key);

    @Test
    void deveGerarTokenAssinadoComValidadeDeTresDias() {
        UUID id = UUID.randomUUID();
        var token = new TokenService(encoder).gerar(usuario(id));
        var validado = decoder.decode(token.getTokenValue());

        assertEquals(id.toString(), validado.getSubject());
        assertEquals(JwtConfiguration.ISSUER, validado.getClaimAsString("iss"));
        assertEquals(Duration.ofDays(3), Duration.between(validado.getIssuedAt(), validado.getExpiresAt()));
    }

    @Test
    void deveRejeitarTokenExpirado() {
        Instant agora = Instant.now();
        String token = assinar(JwtClaimsSet.builder()
                .issuer(JwtConfiguration.ISSUER).subject(UUID.randomUUID().toString())
                .issuedAt(agora.minus(Duration.ofDays(3))).expiresAt(agora.minusSeconds(1)).build());

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void deveRejeitarAssinaturaDiferente() {
        var outraChave = configuration.jwtSecretKey("outra-chave-de-testes-com-pelo-menos-32-bytes");
        var token = new TokenService(configuration.jwtEncoder(outraChave)).gerar(usuario(UUID.randomUUID()));

        assertThrows(JwtException.class, () -> decoder.decode(token.getTokenValue()));
    }

    @Test
    void deveRejeitarTokenSemExpiracaoOuMalformado() {
        String semExpiracao = assinar(JwtClaimsSet.builder()
                .issuer(JwtConfiguration.ISSUER).subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now()).build());

        assertThrows(JwtException.class, () -> decoder.decode(semExpiracao));
        assertThrows(JwtException.class, () -> decoder.decode("token-invalido"));
    }

    @Test
    void deveRejeitarEmissorIncorretoETokenAindaNaoValido() {
        Instant agora = Instant.now();
        String outroEmissor = assinar(JwtClaimsSet.builder()
                .issuer("outro-emissor").subject(UUID.randomUUID().toString())
                .issuedAt(agora).expiresAt(agora.plusSeconds(60)).build());
        String futuro = assinar(JwtClaimsSet.builder()
                .issuer(JwtConfiguration.ISSUER).subject(UUID.randomUUID().toString())
                .issuedAt(agora).notBefore(agora.plusSeconds(60))
                .expiresAt(agora.plusSeconds(120)).build());

        assertThrows(JwtException.class, () -> decoder.decode(outroEmissor));
        assertThrows(JwtException.class, () -> decoder.decode(futuro));
    }

    @Test
    void deveRejeitarChaveCurta() {
        assertThrows(IllegalArgumentException.class, () -> configuration.jwtSecretKey("curta"));
    }

    private String assinar(JwtClaimsSet claims) {
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private UsuariosEntity usuario(UUID id) {
        var usuario = new UsuariosEntity();
        usuario.setId(id);
        usuario.setNome("Maria");
        usuario.setEmail("maria@example.com");
        return usuario;
    }
}

