package io.github.fernando_mariani.TechFixOrg.response;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Origem;

import java.time.LocalDateTime;
import java.util.UUID;

public record PedidosResponse(
        UUID id,
        String titulo,
        String descricao,
        String imagem,
        Origem origem,
        String tecnico,
        LocalDateTime dataMarcada,
        LocalDateTime dataCriacao
) {
}
