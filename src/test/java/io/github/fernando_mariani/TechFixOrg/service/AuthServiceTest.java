package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.LoginDTO;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.CredenciaisInvalidas;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoVerificado;

import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import io.github.fernando_mariani.TechFixOrg.validations.AuthValidation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

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
    private final AuthService service = new AuthService(repository,
            new AuthValidation(FACTORY.getValidator()), encoder,
            new TokenService(jwtConfiguration.jwtEncoder(key)), limiter());

    @AfterAll
    static void fecharValidator() {
        FACTORY.close();
    }

    @Test
    void deveAutenticarComSenhaBCryptERetornarApenasTokenComClaims() {
        var usuario = usuario();
        when(repository.findByEmailIgnoreCase("MARIA@example.com")).thenReturn(Optional.of(usuario));

        var response = service.login(new LoginDTO("MARIA@example.com", "senha-segura"), "192.0.2.1");
        var jwt = jwtConfiguration.jwtDecoder(key).decode(response.token());
        assertEquals(usuario.getId().toString(), jwt.getSubject());
        assertEquals(usuario.getId().toString(), jwt.getClaimAsString("id"));
        assertEquals(usuario.getNome(), jwt.getClaimAsString("nome"));
        assertEquals(usuario.getEmail(), jwt.getClaimAsString("email"));
        assertEquals(usuario.getRole().name(), jwt.getClaimAsString("role"));
        assertEquals(1, response.getClass().getRecordComponents().length);
        assertEquals(Duration.ofDays(3), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
        assertTrue(jwt.getExpiresAt().isAfter(Instant.now()));
        verify(repository).findByEmailIgnoreCase("MARIA@example.com");
    }

    @Test
    void deveRejeitarEmailInexistenteESenhaIncorretaComMesmaMensagem() {
        when(repository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        when(repository.findByEmailIgnoreCase("maria@example.com")).thenReturn(Optional.of(usuario()));

        var ausente = assertThrows(CredenciaisInvalidas.class,
                () -> service.login(new LoginDTO("ausente@example.com", "senha-segura"), "192.0.2.1"));
        var incorreta = assertThrows(CredenciaisInvalidas.class,
                () -> service.login(new LoginDTO("maria@example.com", "senha-incorreta"), "192.0.2.1"));

        assertEquals("E-mail ou senha inválidos.", ausente.getMessage());
        assertEquals(ausente.getMessage(), incorreta.getMessage());
    }

    @Test
    void deveNegarLoginSemGerarTokenQuandoUsuarioNaoVerificado() {
        var usuario = usuario();
        usuario.setVerificado(false);
        when(repository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        var tokens = mock(TokenService.class);
        var auth = new AuthService(repository,
                new AuthValidation(FACTORY.getValidator()), encoder, tokens, limiter());

        assertThrows(NaoVerificado.class,
                () -> auth.login(new LoginDTO(usuario.getEmail(), "senha-segura"), "192.0.2.1"));
        verifyNoInteractions(tokens);
    }

    @Test
    void deveValidarSenhaAntesDeInformarQueUsuarioNaoEstaVerificado() {
        var usuario = usuario();
        usuario.setVerificado(false);
        when(repository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(CredenciaisInvalidas.class,
                () -> service.login(new LoginDTO(usuario.getEmail(), "senha-incorreta"), "192.0.2.1"));
    }

    @Test
    void deveRejeitarDadosInvalidosAntesDeBuscarUsuario() {
        assertThrows(AcaoInvalida.class, () -> service.login(null, "192.0.2.1"));
        assertThrows(AcaoInvalida.class, () -> service.login(new LoginDTO("invalido", " "), "192.0.2.1"));
        assertThrows(AcaoInvalida.class,
                () -> service.login(new LoginDTO("maria@example.com", "á".repeat(37)), "192.0.2.1"));
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

    private RateLimitService limiter() {
        return new RateLimitService(600, Duration.ofMinutes(1), 5, Duration.ofMinutes(15), 100);
    }

    @Test
    void deveBloquearSextaTentativaAntesDeConsultarBancoOuGerarToken() {
        var tokens = mock(TokenService.class);
        var auth = new AuthService(repository, new AuthValidation(FACTORY.getValidator()),
                encoder, tokens, limiter());
        when(repository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        for (int i = 0; i < 5; i++) {
            assertThrows(CredenciaisInvalidas.class,
                    () -> auth.login(new LoginDTO("ausente@example.com", "senha-segura"), "192.0.2.1"));
        }
        assertThrows(io.github.fernando_mariani.TechFixOrg.exceptions.LimiteRequisicoes.class,
                () -> auth.login(new LoginDTO("AUSENTE@example.com", "senha-segura"), "192.0.2.1"));
        verify(repository, times(5)).findByEmailIgnoreCase(anyString());
        assertThrows(CredenciaisInvalidas.class,
                () -> auth.login(new LoginDTO("ausente@example.com", "senha-segura"), "192.0.2.2"));
        verify(repository, times(6)).findByEmailIgnoreCase(anyString());
        verifyNoInteractions(tokens);
    }
}
