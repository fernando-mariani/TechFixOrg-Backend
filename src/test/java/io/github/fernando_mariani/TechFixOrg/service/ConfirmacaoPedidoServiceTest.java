package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.entity.ConfirmacaoPedidoEntity;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.repository.ConfirmacaoPedidoRepository;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfirmacaoPedidoServiceTest {

    private final ConfirmacaoPedidoRepository repository = mock(ConfirmacaoPedidoRepository.class);
    private final PedidosRepository pedidosRepository = mock(PedidosRepository.class);
    private final ConfirmacaoPedidoService service = new ConfirmacaoPedidoService(repository, pedidosRepository);

    @Test
    void deveConfirmarPedidoEAlterarStatusParaPendente() {
        var confirmacao = confirmacaoValida();

        service.confirmar("token", confirmacao.getPedido().getId());

        assertEquals(PedidosEntity.Status.PENDENTE, confirmacao.getPedido().getStatus());
        assertNotNull(confirmacao.getConfirmadoEm());
        verify(pedidosRepository).save(confirmacao.getPedido());
        verify(repository).save(confirmacao);
    }

    @Test
    void deveRejeitarTokenDeOutroPedidoOuInexistente() {
        var confirmacao = confirmacaoValida();

        assertThrows(AcaoInvalida.class, () -> service.confirmar("token", UUID.randomUUID()));
        assertThrows(AcaoInvalida.class, () -> service.confirmar("outro", confirmacao.getPedido().getId()));

        assertNull(confirmacao.getConfirmadoEm());
        verifyNoInteractions(pedidosRepository);
        verify(repository, never()).save(any());
    }

    @Test
    void deveRejeitarTokenExpiradoOuJaUtilizado() {
        var confirmacao = confirmacaoValida();
        confirmacao.setExpiraEm(OffsetDateTime.now().minusSeconds(1));
        assertThrows(AcaoInvalida.class, () -> service.confirmar("token", confirmacao.getPedido().getId()));

        confirmacao.setExpiraEm(OffsetDateTime.now().plusDays(1));
        confirmacao.setConfirmadoEm(OffsetDateTime.now());
        confirmacao.getPedido().setStatus(PedidosEntity.Status.PRONTO);
        assertThrows(AcaoInvalida.class, () -> service.confirmar("token", confirmacao.getPedido().getId()));
        assertEquals(PedidosEntity.Status.PRONTO, confirmacao.getPedido().getStatus());
        verifyNoInteractions(pedidosRepository);
        verify(repository, never()).save(any());
    }

    private ConfirmacaoPedidoEntity confirmacaoValida() {
        var pedido = new PedidosEntity();
        pedido.setId(UUID.randomUUID());
        pedido.setStatus(PedidosEntity.Status.AGUARDANDO_CONFIRMACAO);
        var confirmacao = new ConfirmacaoPedidoEntity();
        confirmacao.setPedido(pedido);
        confirmacao.setTokenHash("token");
        confirmacao.setExpiraEm(OffsetDateTime.now().plusDays(5));
        when(repository.findByTokenHash("token")).thenReturn(Optional.of(confirmacao));
        return confirmacao;
    }

    @Test
    void deveVerificarSeTokenPertenceAoPedidoInformado() {
        var pedido = new PedidosEntity();
        pedido.setId(UUID.randomUUID());
        var confirmacao = new ConfirmacaoPedidoEntity();
        confirmacao.setPedido(pedido);
        confirmacao.setTokenHash("token-salvo");
        when(repository.findByTokenHash("token-salvo")).thenReturn(Optional.of(confirmacao));

        assertTrue(service.verificarToken("token-salvo", pedido.getId()));
        assertFalse(service.verificarToken("token-salvo", UUID.randomUUID()));
        assertFalse(service.verificarToken("token-inexistente", pedido.getId()));
    }

    @Test
    void deveRetornarFalsoParaTokenOuPedidoAusente() {
        assertFalse(service.verificarToken(null, UUID.randomUUID()));
        assertFalse(service.verificarToken(" ", UUID.randomUUID()));
        assertFalse(service.verificarToken("token", null));
        verifyNoInteractions(repository);
    }

    @Test
    void deveCriarConfirmacaoComTokenAleatorioECincoDiasDeValidade() {
        var pedido = new PedidosEntity();
        pedido.setId(UUID.randomUUID());
        when(pedidosRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var antes = OffsetDateTime.now().plusDays(5);

        var confirmacao = service.criar(pedido.getId());
        var outraConfirmacao = service.criar(pedido.getId());

        assertSame(pedido, confirmacao.getPedido());
        assertTrue(confirmacao.getTokenHash().matches("[0-9a-f]{64}"));
        assertNotEquals(confirmacao.getTokenHash(), outraConfirmacao.getTokenHash());
        assertFalse(confirmacao.getExpiraEm().isBefore(antes));
        assertFalse(confirmacao.getExpiraEm().isAfter(OffsetDateTime.now().plusDays(5)));
        assertNull(confirmacao.getConfirmadoEm());
        verify(repository).saveAndFlush(confirmacao);
    }

    @Test
    void deveRejeitarPedidoInexistente() {
        assertThrows(NaoEncontrado.class, () -> service.criar(UUID.randomUUID()));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarConfirmacaoDuplicada() {
        var id = UUID.randomUUID();
        when(pedidosRepository.findById(id)).thenReturn(Optional.of(new PedidosEntity()));
        when(repository.findByPedidoId(id)).thenReturn(Optional.of(new ConfirmacaoPedidoEntity()));

        assertThrows(RegistroDuplicado.class, () -> service.criar(id));
        verify(repository, never()).saveAndFlush(any());
    }
}
