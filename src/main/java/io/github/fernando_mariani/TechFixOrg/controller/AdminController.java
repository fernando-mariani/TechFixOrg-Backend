package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.dto.AtualizarRoleDTO;
import io.github.fernando_mariani.TechFixOrg.response.UsuariosResponse;
import io.github.fernando_mariani.TechFixOrg.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

    @GetMapping
    public PagedModel<UsuariosResponse> buscar(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return new PagedModel<>(service.buscar(busca, pagina, tamanho));
    }

    @PatchMapping("/{id}/verificar")
    public ResponseEntity<UsuariosResponse> verificarUsuario(@PathVariable UUID id) {
        return ResponseEntity.ok(service.verificarUsuario(id));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UsuariosResponse> atualizarRole(
            @PathVariable UUID id, @Valid @RequestBody AtualizarRoleDTO dto) {
        return ResponseEntity.ok(service.atualizarRole(id, dto));
    }
}
