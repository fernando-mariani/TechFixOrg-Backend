package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import io.github.fernando_mariani.TechFixOrg.entity.TecnicoEntity;
import io.github.fernando_mariani.TechFixOrg.entity.TokenTecnicoEntity;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import jakarta.mail.Session;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import io.github.fernando_mariani.TechFixOrg.entity.ConfirmacaoPedidoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final PedidosRepository repository = mock(PedidosRepository.class);
    private final ConfirmacaoPedidoService confirmacaoService = mock(ConfirmacaoPedidoService.class);
    private final TokenTecnicoService tokenTecnicoService = mock(TokenTecnicoService.class);
    private final EmailService service = new EmailService(mailSender, repository, "techfix@example.com",
            confirmacaoService, "https://api.example.com/", tokenTecnicoService);

    @BeforeEach
    void configurarMensagem() {
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        var confirmacao = new ConfirmacaoPedidoEntity();
        confirmacao.setTokenHash("a".repeat(64));
        when(confirmacaoService.criar(any())).thenReturn(confirmacao);
    }

    @Test
    void deveEnviarDetalhesAoClienteComPrecoFormatado() throws Exception {
        var pedido = pedido();
        when(repository.findById(pedido.getId())).thenReturn(Optional.of(pedido));

        service.enviarDetalhesPedido(pedido.getId());

        var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        var mensagem = captor.getValue();
        assertEquals("techfix@example.com", mensagem.getFrom()[0].toString());
        assertEquals("maria@example.com", mensagem.getAllRecipients()[0].toString());
        assertTrue(mensagem.getSubject().contains(pedido.getId().toString()));
        assertTrue(conteudo(mensagem).contains("Olá, Maria!"));
        assertTrue(conteudo(mensagem).contains("Computador não liga"));
        assertTrue(conteudo(mensagem).contains("Técnico responsável: João"));
        assertTrue(conteudo(mensagem).contains("R$"));
        assertTrue(conteudo(mensagem).contains("150,50"));
        String corpo = conteudo(mensagem);
        assertTrue(corpo.contains("Confirme seu pedido aqui!"));
        assertTrue(corpo.contains("href=\"https://api.example.com/confirmacao-pedido/confirmar?token="
                + "a".repeat(64) + "&amp;pedidoId=" + pedido.getId() + "\""));
        verify(confirmacaoService).criar(pedido.getId());
    }

    @Test
    void deveEnviarConviteAoEmailCadastradoComTokenDoUsuario() throws Exception {
        var usuario = new UsuariosEntity();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João");
        usuario.setEmail("joao@example.com");
        var token = new TokenTecnicoEntity();
        token.setUsuario(usuario);
        token.setTokenHash("b".repeat(64));
        when(tokenTecnicoService.criar(usuario.getId())).thenReturn(token);

        service.enviarConviteTecnico(usuario.getId());

        var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        var mensagem = captor.getValue();
        assertEquals(usuario.getEmail(), mensagem.getAllRecipients()[0].toString());
        String corpo = conteudo(mensagem);
        assertTrue(corpo.contains("Concluir cadastro como técnico"));
        assertTrue(corpo.contains("href=\"https://api.example.com/tecnico/params?id="
                + usuario.getId() + "&amp;token=" + token.getTokenHash() + "\""));
    }

    @Test
    void deveRejeitarPedidoInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NaoEncontrado.class, () -> service.enviarDetalhesPedido(id));
        verifyNoInteractions(mailSender);
    }

    @Test
    void deveRejeitarAgendamentoIncompleto() {
        for (String campo : new String[]{"tecnico", "tecnicoEmBranco", "preco", "email"}) {
            var pedido = pedido();
            switch (campo) {
                case "tecnico" -> pedido.setTecnico(null);
                case "tecnicoEmBranco" -> pedido.getTecnico().setNome(" ");
                case "preco" -> pedido.setPreco(null);
                case "email" -> pedido.setEmailCliente(" ");
            }
            when(repository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
            assertThrows(AcaoInvalida.class, () -> service.enviarDetalhesPedido(pedido.getId()));
        }
        verifyNoInteractions(mailSender);
    }

    @Test
    void devePropagarFalhaNoEnvio() {
        var pedido = pedido();
        when(repository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        doThrow(new MailSendException("SMTP indisponível")).when(mailSender).send(any(MimeMessage.class));
        assertThrows(MailSendException.class, () -> service.enviarDetalhesPedido(pedido.getId()));
    }

    private String conteudo(Part parte) throws Exception {
        if (parte.isMimeType("text/*")) {
            return (String) parte.getContent();
        }
        if (parte.getContent() instanceof Multipart multipart) {
            var texto = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                texto.append(conteudo(multipart.getBodyPart(i)));
            }
            return texto.toString();
        }
        return "";
    }

    private PedidosEntity pedido() {
        var pedido = new PedidosEntity();
        pedido.setId(UUID.randomUUID());
        pedido.setNomeCliente("Maria");
        pedido.setEmailCliente("maria@example.com");
        pedido.setTitulo("Reparo");
        pedido.setDescricao("Computador não liga");
        var tecnico = new TecnicoEntity();
        tecnico.setNome("João");
        pedido.setTecnico(tecnico);
        pedido.setPreco(new BigDecimal("150.50"));
        return pedido;
    }
}
