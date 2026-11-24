package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.entity.ConfirmacaoPedidoEntity;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.repository.ConfirmacaoPedidoRepository;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmacaoPedidoService {

    private final ConfirmacaoPedidoRepository repository;
    private final PedidosRepository pedidosRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void confirmar(String token, UUID pedidoId) {
        if (token == null || token.isBlank() || pedidoId == null) {
            throw new AcaoInvalida("Token e ID do pedido são obrigatórios.");
        }

        var confirmacao = repository.findByTokenHash(token)
                .filter(registro -> pedidoId.equals(registro.getPedido().getId()))
                .orElseThrow(() -> new AcaoInvalida("Token inválido para o pedido informado."));

        if (confirmacao.getConfirmadoEm() != null) {
            throw new AcaoInvalida("Este pedido já foi confirmado.");
        }

        var agora = OffsetDateTime.now();
        if (!confirmacao.getExpiraEm().isAfter(agora)) {
            throw new AcaoInvalida("O token de confirmação expirou.");
        }

        confirmacao.setConfirmadoEm(agora);
        confirmacao.getPedido().setStatus(PedidosEntity.Status.PENDENTE);
        pedidosRepository.save(confirmacao.getPedido());
        repository.save(confirmacao);
    }

    @Transactional(readOnly = true)
    public boolean verificarToken(String token, UUID pedidoId) {
        if (token == null || token.isBlank() || pedidoId == null) {
            return false;
        }

        return repository.findByTokenHash(token)
                .map(confirmacao -> pedidoId.equals(confirmacao.getPedido().getId()))
                .orElse(false);
    }

    @Transactional
    public ConfirmacaoPedidoEntity criar(UUID pedidoId) {
        var pedido = pedidosRepository.findForUpdateById(pedidoId)
                .orElseThrow(() -> new NaoEncontrado("Pedido com ID " + pedidoId + " não encontrado."));

        var confirmacao = repository.findByPedidoId(pedidoId).orElseGet(ConfirmacaoPedidoEntity::new);
        var agora = OffsetDateTime.now();
        if (confirmacao.getExpiraEm() != null && confirmacao.getExpiraEm().isAfter(agora)) {
            throw new RegistroDuplicado("Já existe uma confirmação para o pedido com ID " + pedidoId + ".");
        }

        byte[] token = new byte[32];
        random.nextBytes(token);

        confirmacao.setPedido(pedido);
        confirmacao.setTokenHash(HexFormat.of().formatHex(token));
        confirmacao.setExpiraEm(agora.plusDays(5));

        return repository.saveAndFlush(confirmacao);
    }
}
