package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.dto.AtribuirPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.response.GerenteTecnicoResponse;
import io.github.fernando_mariani.TechFixOrg.response.PedidosResponse;
import io.github.fernando_mariani.TechFixOrg.service.GerenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/gerente")
@RequiredArgsConstructor
public class GerenteController {

    private final GerenteService service;

    @GetMapping("/tecnicos")
    public List<GerenteTecnicoResponse> listarTecnicos() {
        return service.listarTecnicos();
    }

    @PatchMapping("/pedido/atribuir/{id}")
    public PedidosResponse atribuirPedido(@PathVariable UUID id, @Valid @RequestBody AtribuirPedidoDTO dto) {
        return service.atribuirPedido(id, dto);
    }
}
