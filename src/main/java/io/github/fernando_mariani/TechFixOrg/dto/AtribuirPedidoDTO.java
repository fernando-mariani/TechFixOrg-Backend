package io.github.fernando_mariani.TechFixOrg.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AtribuirPedidoDTO(
        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 8 dígitos inteiros e 2 casas decimais.")
        BigDecimal preco,

        @NotNull(message = "O ID do técnico é obrigatório.")
        UUID tecnicoId
) {
}
