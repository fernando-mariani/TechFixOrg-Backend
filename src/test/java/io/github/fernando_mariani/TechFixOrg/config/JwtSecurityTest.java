package io.github.fernando_mariani.TechFixOrg.config;

import io.github.fernando_mariani.TechFixOrg.controller.PedidosController;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PedidosController.class,
        properties = "security.jwt.secret=segredo-de-testes-com-pelo-menos-32-bytes")
@Import({SecurityConfiguration.class, JwtConfiguration.class, JwtUserAuthenticationConverter.class,
        UsuarioDetailsService.class, TokenService.class})
class JwtSecurityTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TokenService tokenService;
    @MockitoBean
    private PedidosService pedidosService;
    @MockitoBean
    private UsuariosRepository repository;

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
        String token = tokenService.gerar(usuario.getId()).getTokenValue();

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
                        .header("Authorization", "Bearer " + tokenService.gerar(id).getTokenValue()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(pedidosService);
    }
}
