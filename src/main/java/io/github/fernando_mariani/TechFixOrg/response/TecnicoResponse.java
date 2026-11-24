package io.github.fernando_mariani.TechFixOrg.response;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;

public record TecnicoResponse(
        String email,
        String nome,
        Categoria[] categorias
) {
}
