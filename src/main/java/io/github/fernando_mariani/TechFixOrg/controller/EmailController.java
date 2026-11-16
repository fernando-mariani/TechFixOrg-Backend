package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.service.ConfirmacaoPedidoService;
import io.github.fernando_mariani.TechFixOrg.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/enviar/{id}")
    public ResponseEntity<Void> enviar(@PathVariable UUID id) {
        emailService.enviarDetalhesPedido(id);
        return ResponseEntity.noContent().build();
    }
}
