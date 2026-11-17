package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.dto.CriarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.dto.AtualizarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;
import io.github.fernando_mariani.TechFixOrg.response.PedidosResponse;
import io.github.fernando_mariani.TechFixOrg.service.PedidosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidosController {

    private final PedidosService service;

    @PostMapping
    public ResponseEntity<PedidosResponse> criar(@Valid @RequestBody CriarPedidoDTO dto) {
        PedidosResponse response = service.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PedidosResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidosResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/busca")
    public PagedModel<PedidosResponse> buscar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) UUID tecnico,
            @RequestParam(name = "categoria", required = false) List<Categoria> categorias,
            @RequestParam(required = false) Sort.Direction ordenarPreco,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return new PagedModel<>(service.buscar(busca, tecnico, categorias, ordenarPreco, pagina, tamanho));
    }

    @GetMapping("/meus")
    public PagedModel<PedidosResponse> buscarMeus(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String busca,
            @RequestParam(name = "categoria", required = false) List<Categoria> categorias,
            @RequestParam(required = false) Sort.Direction ordenarPreco,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return new PagedModel<>(service.buscarPorUsuarioTecnico(
                UUID.fromString(jwt.getSubject()), busca, categorias, ordenarPreco, pagina, tamanho));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidosResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarPedidoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<PedidosResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.confirmar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
