package io.github.fernando_mariani.TechFixOrg.dto;

import io.github.fernando_mariani.TechFixOrg.enums.Role;
import jakarta.validation.constraints.NotNull;

public record AtualizarRoleDTO(
        @NotNull(message = "A role é obrigatória.") Role role
) {
}
