package io.github.fernando_mariani.TechFixOrg.response;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Origem;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PedidosResponse(
        UUID id,
        String titulo,
        String descricao,
        String imagem,
        Origem origem,
        UUID tecnicoId,
        String tecnico,
        LocalDateTime dataCriacao,
        String nomeCliente,
        String emailCliente,
        BigDecimal preco,
        Categoria categoria,
        Status status
) {
}
