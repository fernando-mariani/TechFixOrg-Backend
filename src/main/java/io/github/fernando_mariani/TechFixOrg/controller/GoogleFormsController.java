package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.dto.CriarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.response.PedidosResponse;
import io.github.fernando_mariani.TechFixOrg.service.PedidosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/integracoes/forms")
public class GoogleFormsController {

    private final String apiKey;
    private final PedidosService pedidosService;

    public GoogleFormsController(@Value("${integrations.google-forms.api-key}") String apiKey,
                                 PedidosService pedidosService) {
        this.apiKey = apiKey;
        this.pedidosService = pedidosService;
    }

    @PostMapping
    public ResponseEntity<PedidosResponse> receber(
            @RequestHeader(value = "X-Integration-Key", required = false) String integrationKey,
            @RequestBody(required = false) CriarPedidoDTO dto) {
        if (apiKey.isBlank() || integrationKey == null || !MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8), integrationKey.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(401).build();
        }

        PedidosResponse pedido = pedidosService.criar(dto);
        log.info("Pedido {} criado pela integração Google Forms.", pedido.id());
        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/pedidos/{id}")
                .buildAndExpand(pedido.id())
                .toUri();
        return ResponseEntity.created(location).body(pedido);
    }
}
