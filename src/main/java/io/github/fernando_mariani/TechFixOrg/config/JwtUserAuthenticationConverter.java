package io.github.fernando_mariani.TechFixOrg.config;

import io.github.fernando_mariani.TechFixOrg.service.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UsuarioDetailsService detailsService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            var details = detailsService.carregarPorId(UUID.fromString(jwt.getSubject()));
            return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
        } catch (IllegalArgumentException | UsernameNotFoundException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token", "Usuário do token inválido.", null), exception);
        }
    }
}
