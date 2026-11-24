package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.entity.TokenTecnicoEntity;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.repository.TokenTecnicoRepository;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenTecnicoServiceTest {

    private final TokenTecnicoRepository repository = mock(TokenTecnicoRepository.class);
    private final UsuariosRepository usuariosRepository = mock(UsuariosRepository.class);
    private final TokenTecnicoService service = new TokenTecnicoService(repository, usuariosRepository);

    @Test
    void deveRejeitarReemissaoEnquantoTokenEstiverValido() {
        var token = tokenExistente(OffsetDateTime.now().plusDays(1));

        assertThrows(RegistroDuplicado.class, () -> service.criar(token.getUsuario().getId()));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deveSubstituirTokenExpiradoNoMesmoRegistro() {
        var token = tokenExistente(OffsetDateTime.now().minusDays(1));
        when(repository.saveAndFlush(token)).thenReturn(token);

        assertSame(token, service.criar(token.getUsuario().getId()));
        assertNotEquals("token-antigo", token.getTokenHash());
        assertTrue(token.getExpiraEm().isAfter(OffsetDateTime.now().plusDays(4)));
        assertNull(token.getConfirmadoEm());
        verify(repository).saveAndFlush(token);
    }

    private TokenTecnicoEntity tokenExistente(OffsetDateTime expiraEm) {
        var usuario = new UsuariosEntity();
        usuario.setId(UUID.randomUUID());
        var token = new TokenTecnicoEntity();
        token.setUsuario(usuario);
        token.setTokenHash("token-antigo");
        token.setExpiraEm(expiraEm);
        when(usuariosRepository.findForUpdateById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(repository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(token));
        return token;
    }
}
