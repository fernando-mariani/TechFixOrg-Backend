package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.LoginDTO;
import io.github.fernando_mariani.TechFixOrg.exceptions.CredenciaisInvalidas;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoVerificado;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import io.github.fernando_mariani.TechFixOrg.response.LoginResponse;
import io.github.fernando_mariani.TechFixOrg.validations.AuthValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UsuariosRepository repository;
    private final AuthValidation validation;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginResponse login(LoginDTO dto) {
        validation.validar(dto);
        var usuario = repository.findByEmailIgnoreCase(dto.email())
                .orElseThrow(CredenciaisInvalidas::new);

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidas();
        }

        if (!Boolean.TRUE.equals(usuario.getVerificado())) {
            throw new NaoVerificado();
        }

        var token = tokenService.gerar(usuario);
        return new LoginResponse(token.getTokenValue());
    }
}
