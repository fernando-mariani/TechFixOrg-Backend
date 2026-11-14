package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.config.JwtUserAuthenticationConverter;
import io.github.fernando_mariani.TechFixOrg.config.SecurityConfiguration;
import io.github.fernando_mariani.TechFixOrg.service.RateLimitService;
import io.github.fernando_mariani.TechFixOrg.service.PedidosService;
import io.github.fernando_mariani.TechFixOrg.validations.PedidosValidation;
import io.github.fernando_mariani.TechFixOrg.mapper.PedidosMapperImpl;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GoogleFormsController.class,
        properties = "integrations.google-forms.api-key=chave-forms-teste")
@Import({SecurityConfiguration.class, PedidosService.class, PedidosValidation.class, PedidosMapperImpl.class})
class GoogleFormsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private JwtUserAuthenticationConverter converter;
    @MockitoBean
    private RateLimitService rateLimitService;
    @MockitoBean
    private PedidosRepository pedidosRepository;

    private static final String PEDIDO = """
            {"titulo":"Reparo","descricao":"Computador não liga","origem":"FORMS",
             "nomeCliente":"Maria","emailCliente":"maria@example.com",
             "dataLimite":"2027-01-11","categoria":"HARDWARE",
             "preco":150.50,"tecnico":"João","imagem":"imagem.png",
             "dataMarcada":"2027-01-10T14:30:00"}
            """;

    @Test
    void deveReceberPostComChaveValidaSemJwt() throws Exception {
        UUID id = UUID.randomUUID();
        when(pedidosRepository.saveAndFlush(any(PedidosEntity.class))).thenAnswer(invocation -> {
            PedidosEntity pedido = invocation.getArgument(0);
            assertNull(pedido.getPreco());
            assertNull(pedido.getTecnico());
            assertNull(pedido.getImagem());
            assertNull(pedido.getDataMarcada());
            pedido.setId(id);
            return pedido;
        });
        mvc.perform(post("/integracoes/forms")
                        .header("X-Integration-Key", "chave-forms-teste")
                        .contentType("application/json").content(PEDIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/pedidos/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.origem").value("FORMS"))
                .andExpect(jsonPath("$.dataLimite").value("2027-01-11"));
        verify(pedidosRepository).saveAndFlush(any(PedidosEntity.class));
    }

    @Test
    void deveRejeitarChaveInvalida() throws Exception {
        mvc.perform(post("/integracoes/forms").header("X-Integration-Key", "incorreta"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(pedidosRepository);
    }

    @Test
    void deveRejeitarHeaderAusente() throws Exception {
        mvc.perform(post("/integracoes/forms"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(pedidosRepository);
    }

    @Test
    void deveRejeitarChaveVazia() throws Exception {
        mvc.perform(post("/integracoes/forms").header("X-Integration-Key", ""))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(pedidosRepository);
    }

    @Test
    void deveRejeitarCamposObrigatoriosAusentes() throws Exception {
        mvc.perform(post("/integracoes/forms").header("X-Integration-Key", "chave-forms-teste")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(pedidosRepository);
    }

    @Test
    void deveRejeitarEmailInvalido() throws Exception {
        mvc.perform(post("/integracoes/forms").header("X-Integration-Key", "chave-forms-teste")
                        .contentType("application/json").content(PEDIDO.replace("maria@example.com", "invalido")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(pedidosRepository);
    }

    @Test
    void deveRejeitarPedidoDuplicado() throws Exception {
        when(pedidosRepository.existsByTituloIgnoreCaseAndDescricaoIgnoreCase("Reparo", "Computador não liga"))
                .thenReturn(true);
        mvc.perform(post("/integracoes/forms").header("X-Integration-Key", "chave-forms-teste")
                        .contentType("application/json").content(PEDIDO))
                .andExpect(status().isConflict());
        verify(pedidosRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveRejeitarCorpoAusente() throws Exception {
        mvc.perform(post("/integracoes/forms").header("X-Integration-Key", "chave-forms-teste"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(pedidosRepository);
    }

    @Test
    void deveRejeitarDataInvalida() throws Exception {
        mvc.perform(post("/integracoes/forms").header("X-Integration-Key", "chave-forms-teste")
                        .contentType("application/json").content(PEDIDO.replace("2027-01-11", "2027-02-30")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(pedidosRepository);
    }
}
