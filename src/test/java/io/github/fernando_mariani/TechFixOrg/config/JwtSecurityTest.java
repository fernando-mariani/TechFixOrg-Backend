package io.github.fernando_mariani.TechFixOrg.config;

import io.github.fernando_mariani.TechFixOrg.controller.PedidosController;
import io.github.fernando_mariani.TechFixOrg.controller.AdminController;
import io.github.fernando_mariani.TechFixOrg.enums.Role;
import io.github.fernando_mariani.TechFixOrg.service.AdminService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import io.github.fernando_mariani.TechFixOrg.service.PedidosService;
import io.github.fernando_mariani.TechFixOrg.service.TokenService;
import io.github.fernando_mariani.TechFixOrg.service.UsuarioDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PedidosController.class, AdminController.class},
        properties = "security.jwt.secret=segredo-de-testes-com-pelo-menos-32-bytes")
@Import({SecurityConfiguration.class, JwtConfiguration.class, JwtUserAuthenticationConverter.class,
        UsuarioDetailsService.class, TokenService.class,
        io.github.fernando_mariani.TechFixOrg.service.RateLimitService.class})
class JwtSecurityTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TokenService tokenService;
    @MockitoBean
    private PedidosService pedidosService;
    @MockitoBean
    private AdminService adminService;
    @MockitoBean
    private UsuariosRepository repository;

    @ParameterizedTest
    @EnumSource(Role.class)
    void devePermitirAdministracaoSomenteParaAdmin(Role role) throws Exception {
        var usuario = new UsuariosEntity();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("admin@example.com");
        usuario.setSenha("hash");
        usuario.setRole(role);
        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        String token = tokenService.gerar(usuario).getTokenValue();
        UUID alvo = UUID.randomUUID();
        int esperado = role == Role.ADMIN ? 200 : 403;

        mvc.perform(patch("/admin/usuarios/{id}/verificar", alvo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(esperado));
        mvc.perform(patch("/admin/usuarios/{id}/role", alvo)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"GERENTE\"}"))
                .andExpect(status().is(esperado));

        if (role == Role.ADMIN) {
            verify(adminService).verificarUsuario(alvo);
            verify(adminService).atualizarRole(alvo,
                    new io.github.fernando_mariani.TechFixOrg.dto.AtualizarRoleDTO(Role.GERENTE));
        } else {
            verifyNoInteractions(adminService);
        }
    }

    @Test
    void deveCriarUmaUnicaAuthoritySemPrefixo() {
        var usuario = new UsuariosEntity();
        usuario.setEmail("usuario@example.com");
        usuario.setSenha("hash");
        when(repository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        var details = new UsuarioDetailsService(repository).loadUserByUsername(usuario.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("USER"),
                details.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }

    @Test
    void deveRejeitarRequisicaoSemToken() throws Exception {
        mvc.perform(get("/pedidos")).andExpect(status().isUnauthorized());
        verifyNoInteractions(pedidosService);
    }

    @Test
    void deveRejeitarTokenInvalido() throws Exception {
        mvc.perform(get("/pedidos").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(pedidosService, repository);
    }

    @Test
    void deveCarregarUserDetailsEPermitirTokenValido() throws Exception {
        var usuario = new UsuariosEntity();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("maria@example.com");
        usuario.setSenha("hash");
        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(pedidosService.listar()).thenReturn(List.of());
        String token = tokenService.gerar(usuario).getTokenValue();

        mvc.perform(get("/pedidos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(repository).findById(usuario.getId());
        verify(pedidosService).listar();
    }

    @Test
    void deveRejeitarTokenDeUsuarioExcluido() throws Exception {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        mvc.perform(get("/pedidos")
                        .header("Authorization", "Bearer " + tokenService.gerar(usuarioExcluido(id)).getTokenValue()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(pedidosService);
    }

    private UsuariosEntity usuarioExcluido(UUID id) {
        var usuario = new UsuariosEntity();
        usuario.setId(id);
        usuario.setNome("Maria");
        usuario.setEmail("maria@example.com");
        return usuario;
    }
}

