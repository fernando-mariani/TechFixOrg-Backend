package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.service.ConfirmacaoPedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConfirmacaoPedidoControllerTest {

    @Test
    void deveEnviarFormularioSemRepetirParametrosDaUrl() throws Exception {
        var service = mock(ConfirmacaoPedidoService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ConfirmacaoPedidoController(service)).build();
        var id = UUID.randomUUID();
        var token = "a".repeat(64);

        var pagina = mvc.perform(get("/confirmacao-pedido/confirmar")
                        .queryParam("token", token).queryParam("pedidoId", id.toString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        var action = Pattern.compile("<form method=\"post\" action=\"([^\"]+)\">").matcher(pagina);
        assertTrue(action.find());
        var destino = URI.create(action.group(1));
        assertEquals("/confirmacao-pedido/confirmar", destino.getPath());
        assertNull(destino.getQuery());
        assertTrue(pagina.contains("name=\"token\" value=\"" + token + "\""));
        assertTrue(pagina.contains("name=\"pedidoId\" value=\"" + id + "\""));

        mvc.perform(post(destino).param("token", token).param("pedidoId", id.toString()))
                .andExpect(status().isNoContent());
        verify(service).confirmar(token, id);
    }
}
