package io.github.fernando_mariani.TechFixOrg.response;

import io.github.fernando_mariani.TechFixOrg.enums.Role;
import java.time.LocalDateTime;
import java.util.UUID;

public record UsuariosResponse(
        UUID id,
        String nome,
        String email,
        Role role,
        Boolean verificado,
        LocalDateTime dataCriacao
) {
}
