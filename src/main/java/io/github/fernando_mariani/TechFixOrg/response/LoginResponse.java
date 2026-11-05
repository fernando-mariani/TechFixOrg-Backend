package io.github.fernando_mariani.TechFixOrg.response;

import java.time.Instant;

public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEm,
        UsuariosResponse usuario
) {
}
