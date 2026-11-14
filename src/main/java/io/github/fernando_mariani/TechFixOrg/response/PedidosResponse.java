package io.github.fernando_mariani.TechFixOrg.response;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Origem;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        LocalDateTime dataCriacao,
        String nomeCliente,
        String emailCliente,
        LocalDate dataLimite,
        BigDecimal preco,
        Categoria categoria
) {
}
