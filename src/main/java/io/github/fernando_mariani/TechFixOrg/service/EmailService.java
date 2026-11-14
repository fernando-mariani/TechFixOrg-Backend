package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final PedidosRepository pedidosRepository;
    private final String remetente;

    public EmailService(JavaMailSender mailSender, PedidosRepository pedidosRepository,
                        @Value("${spring.mail.from}") String remetente) {
        this.mailSender = mailSender;
        this.pedidosRepository = pedidosRepository;
        this.remetente = remetente;
    }

    public void enviarDetalhesPedido(UUID id) {
        var pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new NaoEncontrado("Pedido com ID " + id + " não encontrado."));

        if (pedido.getEmailCliente() == null || pedido.getEmailCliente().isBlank()) {
            throw new AcaoInvalida("O e-mail do cliente é obrigatório para enviar os detalhes do pedido.");
        }
        if (pedido.getTecnico() == null || pedido.getTecnico().isBlank()
                || pedido.getPreco() == null || pedido.getDataMarcada() == null) {
            throw new AcaoInvalida("Preencha o técnico, o preço e a data marcada antes de enviar os detalhes do pedido.");
        }

        String preco = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                .format(pedido.getPreco());
        String dataMarcada = pedido.getDataMarcada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));

        var mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(pedido.getEmailCliente());
        mensagem.setSubject("TechFixOrg - Detalhes do pedido " + pedido.getId());
        mensagem.setText("""
                Olá, %s!

                Confira os detalhes do seu pedido de conserto:

                Pedido: %s
                Título: %s
                Descrição: %s
                Técnico responsável: %s
                Preço: %s
                Data marcada para o conserto: %s

                Atenciosamente,
                Equipe TechFixOrg
                """.formatted(pedido.getNomeCliente(), pedido.getId(), pedido.getTitulo(),
                pedido.getDescricao(), pedido.getTecnico(), preco, dataMarcada));

        mailSender.send(mensagem);
    }
}
