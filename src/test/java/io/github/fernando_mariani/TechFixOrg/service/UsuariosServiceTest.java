package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.config.SecurityConfiguration;
import io.github.fernando_mariani.TechFixOrg.dto.UsuariosDTO;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.enums.Role;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.mapper.UsuariosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import io.github.fernando_mariani.TechFixOrg.validations.UsuariosValidation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuariosServiceTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final UsuariosRepository repository = mock(UsuariosRepository.class);
    private final UsuariosMapper mapper = Mappers.getMapper(UsuariosMapper.class);
    private final UsuariosValidation validation = new UsuariosValidation(repository, FACTORY.getValidator());
    private final org.springframework.security.crypto.password.PasswordEncoder encoder =
            new SecurityConfiguration().passwordEncoder();
    private final UsuariosService service = new UsuariosService(repository, mapper, validation, encoder);

    @AfterAll
    static void fecharValidator() {
        FACTORY.close();
    }

    @Test
    void deveSalvarSenhaComHashEPreservarValoresPadrao() {
        var dto = new UsuariosDTO("Maria", "maria@example.com", "senha-segura");
        when(repository.saveAndFlush(any(UsuariosEntity.class))).thenAnswer(invocation -> {
            UsuariosEntity usuario = invocation.getArgument(0);
            assertNotEquals(dto.senha(), usuario.getSenha());
            assertTrue(usuario.getSenha().startsWith("$2a$"));
            assertTrue(encoder.matches(dto.senha(), usuario.getSenha()));
            assertEquals(Role.USER, usuario.getRole());
            assertFalse(usuario.getVerificado());
            return usuario;
        });

        var response = service.criar(dto);

        assertEquals(dto.nome(), response.nome());
        assertEquals(dto.email(), response.email());
        assertEquals(Role.USER, response.role());
        assertFalse(response.verificado());
        verify(repository).saveAndFlush(any(UsuariosEntity.class));
    }

    @Test
    void deveRejeitarEmailDuplicadoSemSalvar() {
        var dto = new UsuariosDTO("Maria", "MARIA@example.com", "senha-segura");
        when(repository.existsByEmailIgnoreCase(dto.email())).thenReturn(true);

        assertThrows(RegistroDuplicado.class, () -> service.criar(dto));

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveRejeitarSenhaAcimaDe72BytesInclusiveComAcentos() {
        assertThrows(AcaoInvalida.class,
                () -> service.criar(new UsuariosDTO("Maria", "maria@example.com", "a".repeat(73))));
        assertThrows(AcaoInvalida.class,
                () -> service.criar(new UsuariosDTO("Maria", "maria@example.com", "á".repeat(37))));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarDadosInvalidosAntesDeConsultarRepositorio() {
        assertThrows(AcaoInvalida.class, () -> service.criar(null));
        assertThrows(AcaoInvalida.class,
                () -> service.criar(new UsuariosDTO(" ", "invalido", "curta")));
        assertThrows(AcaoInvalida.class,
                () -> service.criar(new UsuariosDTO("a".repeat(101), "a".repeat(256), "a".repeat(301))));

        verifyNoInteractions(repository);
    }
}
