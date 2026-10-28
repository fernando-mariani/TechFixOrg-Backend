package io.github.fernando_mariani.TechFixOrg.dto;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Origem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PedidosDTO(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 100, message = "O título deve ter no máximo 100 caracteres.")
        String titulo,
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres.")
        String descricao,
        @Size(max = 300, message = "A imagem deve ter no máximo 300 caracteres.")
        String imagem,
        @NotNull(message = "A origem é obrigatória.")
        Origem origem,
        @Size(max = 100, message = "O técnico deve ter no máximo 100 caracteres.")
        String tecnico,
        LocalDateTime dataMarcada
) {
}
