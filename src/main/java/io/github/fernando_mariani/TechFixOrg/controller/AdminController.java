package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.dto.AtualizarRoleDTO;
import io.github.fernando_mariani.TechFixOrg.response.UsuariosResponse;
import io.github.fernando_mariani.TechFixOrg.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

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
