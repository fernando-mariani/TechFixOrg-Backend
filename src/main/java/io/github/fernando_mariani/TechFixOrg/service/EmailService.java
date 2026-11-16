package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final PedidosRepository pedidosRepository;
    private final String remetente;
    private final String apiUrl;
    private final ConfirmacaoPedidoService confirmacaoPedidoService;

    public EmailService(JavaMailSender mailSender, PedidosRepository pedidosRepository,
                        @Value("${spring.mail.from}") String remetente, ConfirmacaoPedidoService confirmacaoPedidoService,
                        @Value("${api.url}") String apiUrl) {
        this.mailSender = mailSender;
        this.pedidosRepository = pedidosRepository;
        this.remetente = remetente;
        this.apiUrl = apiUrl;
        this.confirmacaoPedidoService =  confirmacaoPedidoService;
    }

    @Transactional
    public void enviarDetalhesPedido(UUID id) {
        var pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new NaoEncontrado("Pedido com ID " + id + " não encontrado."));

        if (pedido.getEmailCliente() == null || pedido.getEmailCliente().isBlank()) {
            throw new AcaoInvalida("O e-mail do cliente é obrigatório para enviar os detalhes do pedido.");
        }
        if (pedido.getTecnico() == null || pedido.getTecnico().isBlank()
                || pedido.getPreco() == null) {
            throw new AcaoInvalida("Preencha o técnico e o preço antes de enviar os detalhes do pedido.");
        }

        String preco = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                .format(pedido.getPreco());

        var confirmacao = confirmacaoPedidoService.criar(id);
        String link = UriComponentsBuilder.fromUriString(apiUrl.replaceAll("/+$", ""))
                .path("/confirmacao-pedido/confirmar")
                .queryParam("token", confirmacao.getTokenHash())
                .queryParam("pedidoId", pedido.getId())
                .build().encode().toUriString();

        String texto = """
                Olá, %s!

                Confira os detalhes do seu pedido de conserto:
                Pedido: %s
                Título: %s
                Descrição: %s
                Técnico responsável: %s
                Preço: %s

                Confirme seu pedido aqui!: %s

                Atenciosamente,
                Equipe TechFixOrg
                """.formatted(pedido.getNomeCliente(), pedido.getId(), pedido.getTitulo(),
                pedido.getDescricao(), pedido.getTecnico(), preco, link);
        String html = """
                <!doctype html>
                <html lang="pt-BR"><body style="font-family:Arial,sans-serif;color:#1f2937">
                <p>Olá, %s!</p>
                <p>Confira os detalhes do seu pedido de conserto:</p>
                <p>Pedido: %s<br>
                Título: %s<br>
                Descrição: %s<br>
                Técnico responsável: %s<br>
                Preço: %s</p>
                <p style="margin:28px 0"><a href="%s"
                style="display:inline-block;background-color:#2563eb;color:#ffffff;padding:14px 24px;border-radius:6px;text-decoration:none;font-weight:bold">Confirme seu pedido aqui!</a></p>
                <p>Este link expira em 5 dias.</p>
                <p>Atenciosamente,<br>Equipe TechFixOrg</p>
                </body></html>
                """.formatted(HtmlUtils.htmlEscape(pedido.getNomeCliente()), pedido.getId(),
                HtmlUtils.htmlEscape(pedido.getTitulo()), HtmlUtils.htmlEscape(pedido.getDescricao()),
                HtmlUtils.htmlEscape(pedido.getTecnico()), HtmlUtils.htmlEscape(preco), HtmlUtils.htmlEscape(link));

        var mensagem = mailSender.createMimeMessage();
        try {
            var helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(pedido.getEmailCliente());
            helper.setSubject("TechFixOrg - Detalhes do pedido " + pedido.getId());
            helper.setText(texto, html);
        } catch (MessagingException exception) {
            throw new MailPreparationException("Não foi possível preparar o e-mail de confirmação.", exception);
        }
        mailSender.send(mensagem);
    }
}
