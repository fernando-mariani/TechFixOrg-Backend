package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.config.JwtUserAuthenticationConverter;
import io.github.fernando_mariani.TechFixOrg.config.SecurityConfiguration;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.service.EmailService;
import io.github.fernando_mariani.TechFixOrg.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
@Import(SecurityConfiguration.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private JwtUserAuthenticationConverter converter;
    @MockitoBean
    private RateLimitService rateLimitService;

    @Test
    void deveEnviarEmailDoPedido() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(post("/email/enviar").param("id", id.toString()).with(jwt()).with(csrf()))
                .andExpect(status().isNoContent());
        verify(emailService).enviarDetalhesPedido(id);
    }

    @Test
    void deveRejeitarIdInvalidoOuAusente() throws Exception {
        mvc.perform(post("/email/enviar").param("id", "invalido").with(jwt()).with(csrf()))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/email/enviar").with(jwt()).with(csrf()))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(emailService);
    }

    @Test
    void deveRejeitarRequisicaoNaoAutenticada() throws Exception {
        mvc.perform(post("/email/enviar").param("id", UUID.randomUUID().toString()).with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(emailService);
    }

    @Test
    void deveRetornarNaoEncontradoParaPedidoInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NaoEncontrado("Pedido não encontrado.")).when(emailService).enviarDetalhesPedido(id);
        mvc.perform(post("/email/enviar").param("id", id.toString()).with(jwt()).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarPedidoIncompleto() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AcaoInvalida("Preencha o agendamento.")).when(emailService).enviarDetalhesPedido(id);
        mvc.perform(post("/email/enviar").param("id", id.toString()).with(jwt()).with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
