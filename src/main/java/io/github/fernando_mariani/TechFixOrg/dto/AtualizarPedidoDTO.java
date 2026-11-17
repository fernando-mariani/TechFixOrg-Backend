package io.github.fernando_mariani.TechFixOrg.dto;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Origem;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record AtualizarPedidoDTO(
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
        UUID tecnicoId,
        @NotBlank(message = "O nome do cliente é obrigatório.")
        @Size(max = 100, message = "O nome do cliente deve ter no máximo 100 caracteres.")
        String nomeCliente,
        @NotBlank(message = "O e-mail do cliente é obrigatório.")
        @Email(message = "O e-mail do cliente deve ser válido.")
        @Size(max = 200, message = "O e-mail do cliente deve ter no máximo 200 caracteres.")
        String emailCliente,
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 8 dígitos inteiros e 2 casas decimais.")
        BigDecimal preco,
        @NotNull(message = "A categoria é obrigatória.")
        Categoria categoria
) {
}
