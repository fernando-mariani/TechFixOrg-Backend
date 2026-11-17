package io.github.fernando_mariani.TechFixOrg.dto;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarTecnicoDTO(
        @NotEmpty(message = "Selecione pelo menos uma categoria.")
        List<@NotNull(message = "Informe uma categoria válida.") Categoria> categorias
) {
}
