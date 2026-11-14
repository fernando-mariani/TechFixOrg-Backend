package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final PedidosRepository repository = mock(PedidosRepository.class);
    private final EmailService service = new EmailService(mailSender, repository, "techfix@example.com");

    @Test
    void deveEnviarDetalhesAoClienteComPrecoEDataFormatados() {
        var pedido = pedido();
        when(repository.findById(pedido.getId())).thenReturn(Optional.of(pedido));

        service.enviarDetalhesPedido(pedido.getId());

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        var mensagem = captor.getValue();
        assertEquals("techfix@example.com", mensagem.getFrom());
        assertArrayEquals(new String[]{"maria@example.com"}, mensagem.getTo());
        assertTrue(mensagem.getSubject().contains(pedido.getId().toString()));
        assertTrue(mensagem.getText().contains("Olá, Maria!"));
        assertTrue(mensagem.getText().contains("Computador não liga"));
        assertTrue(mensagem.getText().contains("Técnico responsável: João"));
        assertTrue(mensagem.getText().contains("R$"));
        assertTrue(mensagem.getText().contains("150,50"));
        assertTrue(mensagem.getText().contains("10/01/2027 às 14:30"));
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
        for (String campo : new String[]{"tecnico", "tecnicoEmBranco", "preco", "dataMarcada", "email"}) {
            var pedido = pedido();
            switch (campo) {
                case "tecnico" -> pedido.setTecnico(null);
                case "tecnicoEmBranco" -> pedido.setTecnico(" ");
                case "preco" -> pedido.setPreco(null);
                case "dataMarcada" -> pedido.setDataMarcada(null);
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
        doThrow(new MailSendException("SMTP indisponível")).when(mailSender).send(any(SimpleMailMessage.class));
        assertThrows(MailSendException.class, () -> service.enviarDetalhesPedido(pedido.getId()));
    }

    private PedidosEntity pedido() {
        var pedido = new PedidosEntity();
        pedido.setId(UUID.randomUUID());
        pedido.setNomeCliente("Maria");
        pedido.setEmailCliente("maria@example.com");
        pedido.setTitulo("Reparo");
        pedido.setDescricao("Computador não liga");
        pedido.setTecnico("João");
        pedido.setPreco(new BigDecimal("150.50"));
        pedido.setDataMarcada(LocalDateTime.of(2027, 1, 10, 14, 30));
        return pedido;
    }
}
