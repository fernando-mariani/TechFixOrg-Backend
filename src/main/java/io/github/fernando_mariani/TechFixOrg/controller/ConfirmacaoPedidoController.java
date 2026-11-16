package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.service.ConfirmacaoPedidoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/confirmacao-pedido")
@RequiredArgsConstructor
public class ConfirmacaoPedidoController {

    private final ConfirmacaoPedidoService service;

    @GetMapping(value = "/confirmar", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> exibirConfirmacao(@RequestParam("token") String token,
                                                   @RequestParam("pedidoId") UUID pedidoId,
                                                   HttpServletRequest request) {
        String pagina = """
                <!doctype html>
                <html lang="pt-BR"><head><meta charset="UTF-8"><title>Confirmar pedido</title></head>
                <body style="font-family:Arial,sans-serif;text-align:center;padding:48px">
                <h1>Confirme seu pedido</h1>
                <form method="post" action="%s">
                <input type="hidden" name="token" value="%s">
                <input type="hidden" name="pedidoId" value="%s">
                <button type="submit" style="background:#2563eb;color:white;padding:14px 24px;border:0;border-radius:6px;cursor:pointer">Confirme seu pedido aqui!</button>
                </form></body></html>
                """.formatted(HtmlUtils.htmlEscape(request.getRequestURI()), HtmlUtils.htmlEscape(token), pedidoId);
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .header("Referrer-Policy", "no-referrer").body(pagina);
    }

    @PostMapping("/confirmar")
    public ResponseEntity<Void> confirmar(@RequestParam("token") String token,
                                         @RequestParam("pedidoId") UUID pedidoId) {
        service.confirmar(token, pedidoId);
        return ResponseEntity.noContent().build();
    }
}
