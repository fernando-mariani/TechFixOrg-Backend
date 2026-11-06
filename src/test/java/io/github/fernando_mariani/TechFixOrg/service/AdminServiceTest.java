package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.AtualizarRoleDTO;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.enums.Role;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.mapper.UsuariosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private final UsuariosRepository repository = mock(UsuariosRepository.class);
    private final AdminService service = new AdminService(repository, Mappers.getMapper(UsuariosMapper.class));

    @Test
    void deveVerificarUsuarioSemAlterarRole() {
        var usuario = usuarioExistente();
        var response = service.verificarUsuario(usuario.getId());
        assertTrue(response.verificado());
        assertEquals(Role.USER, response.role());
        verify(repository).saveAndFlush(usuario);
    }

    @Test
    void deveSubstituirRoleSemAlterarVerificacao() {
        var usuario = usuarioExistente();
        for (Role role : Role.values()) {
            var response = service.atualizarRole(usuario.getId(), new AtualizarRoleDTO(role));
            assertEquals(role, response.role());
            assertFalse(response.verificado());
        }
    }

    @Test
    void deveRejeitarUsuarioInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NaoEncontrado.class, () -> service.verificarUsuario(id));
        assertThrows(NaoEncontrado.class,
                () -> service.atualizarRole(id, new AtualizarRoleDTO(Role.ADMIN)));
        verify(repository, never()).saveAndFlush(any());
    }

    private UsuariosEntity usuarioExistente() {
        var usuario = new UsuariosEntity();
        usuario.setId(UUID.randomUUID());
        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(repository.saveAndFlush(usuario)).thenReturn(usuario);
        return usuario;
    }
}
