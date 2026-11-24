package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.entity.TokenTecnicoEntity;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.repository.TokenTecnicoRepository;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenTecnicoService {

    private final TokenTecnicoRepository repository;
    private final UsuariosRepository usuariosRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public UsuariosEntity confirmar(String token, UUID usuarioId) {
        if (token == null || token.isBlank() || usuarioId == null) {
            throw new AcaoInvalida("Token e ID do usuário são obrigatórios.");
        }

        var confirmacao = repository.findFirstByTokenHash(token)
                .filter(registro -> usuarioId.equals(registro.getUsuario().getId()))
                .orElseThrow(() -> new AcaoInvalida("Token inválido para o usuário informado."));

        if (confirmacao.getConfirmadoEm() != null) {
            throw new AcaoInvalida("Este usuário já foi confirmado.");
        }

        var agora = OffsetDateTime.now();
        if (!confirmacao.getExpiraEm().isAfter(agora)) {
            throw new AcaoInvalida("O token de confirmação expirou.");
        }

        confirmacao.setConfirmadoEm(agora);
        var usuario = confirmacao.getUsuario();
        usuario.setVerificado(true);
        usuariosRepository.save(usuario);
        repository.save(confirmacao);
        return usuario;
    }

    @Transactional(readOnly = true)
    public boolean verificarToken(String token, UUID usuarioId) {
        if (token == null || token.isBlank() || usuarioId == null) {
            return false;
        }

        return repository.findByTokenHash(token)
                .filter(confirmacao -> usuarioId.equals(confirmacao.getUsuario().getId()))
                .filter(confirmacao -> confirmacao.getConfirmadoEm() == null)
                .filter(confirmacao -> confirmacao.getExpiraEm().isAfter(OffsetDateTime.now()))
                .isPresent();
    }

    @Transactional
    public TokenTecnicoEntity criar(UUID usuarioId) {
        var usuario = usuariosRepository.findForUpdateById(usuarioId)
                .orElseThrow(() -> new NaoEncontrado("Usuário com ID " + usuarioId + " não encontrado."));

        var confirmacao = repository.findByUsuarioId(usuarioId).orElseGet(TokenTecnicoEntity::new);
        var agora = OffsetDateTime.now();
        if (confirmacao.getExpiraEm() != null && confirmacao.getExpiraEm().isAfter(agora)) {
            throw new RegistroDuplicado("Já existe uma confirmação para o usuário com ID " + usuarioId + ".");
        }

        byte[] token = new byte[32];
        random.nextBytes(token);

        confirmacao.setUsuario(usuario);
        confirmacao.setTokenHash(HexFormat.of().formatHex(token));
        confirmacao.setExpiraEm(agora.plusDays(5));

        return repository.saveAndFlush(confirmacao);
    }
}
