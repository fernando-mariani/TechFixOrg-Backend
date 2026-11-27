package io.github.fernando_mariani.TechFixOrg.response;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;

import java.util.UUID;

public record GerenteTecnicoResponse(UUID id, String nome, String email, Categoria[] categorias) {
}
