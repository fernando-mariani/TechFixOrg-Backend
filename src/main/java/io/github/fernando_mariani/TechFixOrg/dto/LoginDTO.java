package io.github.fernando_mariani.TechFixOrg.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,
        @NotBlank(message = "A senha é obrigatória.")
        @Size(max = 72, message = "A senha deve ter no máximo 72 caracteres.")
        String senha
) {
}
