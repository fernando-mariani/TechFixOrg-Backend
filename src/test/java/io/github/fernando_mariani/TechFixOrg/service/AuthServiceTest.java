package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.LoginDTO;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.CredenciaisInvalidas;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoVerificado;
import io.github.fernando_mariani.TechFixOrg.mapper.UsuariosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import io.github.fernando_mariani.TechFixOrg.validations.AuthValidation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.github.fernando_mariani.TechFixOrg.config.JwtConfiguration;
import java.time.Duration;
import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final UsuariosRepository repository = mock(UsuariosRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtConfiguration jwtConfiguration = new JwtConfiguration();
    private final javax.crypto.SecretKey key =
            jwtConfiguration.jwtSecretKey("segredo-de-testes-com-pelo-menos-32-bytes");
    private final AuthService service = new AuthService(repository, Mappers.getMapper(UsuariosMapper.class),
            new AuthValidation(FACTORY.getValidator()), encoder,
            new TokenService(jwtConfiguration.jwtEncoder(key)));

    @AfterAll
    static void fecharValidator() {
        FACTORY.close();
    }

    @Test
    void deveAutenticarComSenhaBCryptERetornarDadosDoUsuario() {
        var usuario = usuario();
        when(repository.findByEmailIgnoreCase("MARIA@example.com")).thenReturn(Optional.of(usuario));

        var response = service.login(new LoginDTO("MARIA@example.com", "senha-segura"));

        assertEquals(usuario.getId(), response.usuario().id());
        assertEquals(usuario.getEmail(), response.usuario().email());
        assertEquals(usuario.getNome(), response.usuario().nome());
        assertEquals("Bearer", response.tipo());
        var jwt = jwtConfiguration.jwtDecoder(key).decode(response.token());
        assertEquals(usuario.getId().toString(), jwt.getSubject());
        assertEquals(Duration.ofDays(3), Duration.between(jwt.getIssuedAt(), response.expiraEm()));
        assertTrue(response.expiraEm().isAfter(Instant.now()));
        verify(repository).findByEmailIgnoreCase("MARIA@example.com");
    }

    @Test
    void deveRejeitarEmailInexistenteESenhaIncorretaComMesmaMensagem() {
        when(repository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        when(repository.findByEmailIgnoreCase("maria@example.com")).thenReturn(Optional.of(usuario()));

        var ausente = assertThrows(CredenciaisInvalidas.class,
                () -> service.login(new LoginDTO("ausente@example.com", "senha-segura")));
        var incorreta = assertThrows(CredenciaisInvalidas.class,
                () -> service.login(new LoginDTO("maria@example.com", "senha-incorreta")));

        assertEquals("E-mail ou senha inválidos.", ausente.getMessage());
        assertEquals(ausente.getMessage(), incorreta.getMessage());
    }

    @Test
    void deveNegarLoginSemGerarTokenQuandoUsuarioNaoVerificado() {
        var usuario = usuario();
        usuario.setVerificado(false);
        when(repository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        var tokens = mock(TokenService.class);
        var auth = new AuthService(repository, Mappers.getMapper(UsuariosMapper.class),
                new AuthValidation(FACTORY.getValidator()), encoder, tokens);

        assertThrows(NaoVerificado.class,
                () -> auth.login(new LoginDTO(usuario.getEmail(), "senha-segura")));
        verifyNoInteractions(tokens);
    }

    @Test
    void deveValidarSenhaAntesDeInformarQueUsuarioNaoEstaVerificado() {
        var usuario = usuario();
        usuario.setVerificado(false);
        when(repository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(CredenciaisInvalidas.class,
                () -> service.login(new LoginDTO(usuario.getEmail(), "senha-incorreta")));
    }

    @Test
    void deveRejeitarDadosInvalidosAntesDeBuscarUsuario() {
        assertThrows(AcaoInvalida.class, () -> service.login(null));
        assertThrows(AcaoInvalida.class, () -> service.login(new LoginDTO("invalido", " ")));
        assertThrows(AcaoInvalida.class,
                () -> service.login(new LoginDTO("maria@example.com", "á".repeat(37))));
        verifyNoInteractions(repository);
    }

    private UsuariosEntity usuario() {
        var usuario = new UsuariosEntity();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Maria");
        usuario.setEmail("maria@example.com");
        usuario.setSenha(encoder.encode("senha-segura"));
        usuario.setVerificado(true);
        return usuario;
    }
}
