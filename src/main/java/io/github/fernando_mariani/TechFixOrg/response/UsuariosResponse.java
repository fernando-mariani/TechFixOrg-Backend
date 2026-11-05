package io.github.fernando_mariani.TechFixOrg.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuariosResponse(
        UUID id,
        String nome,
        String email,
        String role,
        Boolean verificado,
        LocalDateTime dataCriacao
) {
}
