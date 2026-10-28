package io.github.fernando_mariani.TechFixOrg.validations;

import io.github.fernando_mariani.TechFixOrg.dto.PedidosDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Origem;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PedidosValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final PedidosRepository repository = mock(PedidosRepository.class);
    private final PedidosValidation validation = new PedidosValidation(repository, FACTORY.getValidator());

    @AfterAll
    static void fecharValidator() {
        FACTORY.close();
    }

    @Test
    void deveRejeitarDadosObrigatoriosAntesDeConsultarRepositorio() {
        var dto = new PedidosDTO(" ", null, null, null, null, null);

        AcaoInvalida erro = assertThrows(AcaoInvalida.class, () -> validation.validar(dto, null));

        assertTrue(erro.getMessage().contains("O título é obrigatório."));
        assertTrue(erro.getMessage().contains("A descrição é obrigatória."));
        assertTrue(erro.getMessage().contains("A origem é obrigatória."));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarCamposQueExcedemLimitesDaTabela() {
        var dto = new PedidosDTO("a".repeat(101), "a".repeat(301), "a".repeat(301),
                Origem.EMAIL, "a".repeat(101), null);

        AcaoInvalida erro = assertThrows(AcaoInvalida.class, () -> validation.validar(dto, null));

        assertTrue(erro.getMessage().contains("O título deve ter no máximo 100 caracteres."));
        assertTrue(erro.getMessage().contains("A descrição deve ter no máximo 300 caracteres."));
        assertTrue(erro.getMessage().contains("A imagem deve ter no máximo 300 caracteres."));
        assertTrue(erro.getMessage().contains("O técnico deve ter no máximo 100 caracteres."));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarCriacaoDuplicada() {
        var dto = pedidoValido();
        when(repository.existsByTituloIgnoreCaseAndDescricaoIgnoreCase(dto.titulo(), dto.descricao()))
                .thenReturn(true);

        assertThrows(RegistroDuplicado.class, () -> validation.validar(dto, null));
    }

    @Test
    void devePermitirAtualizarProprioPedidoSemConsideraLoDuplicado() {
        UUID id = UUID.randomUUID();
        var dto = pedidoValido();

        assertDoesNotThrow(() -> validation.validar(dto, id));

        verify(repository).existsByTituloIgnoreCaseAndDescricaoIgnoreCaseAndIdNot(dto.titulo(), dto.descricao(), id);
    }

    @Test
    void deveRejeitarAtualizacaoQueDuplicaOutroPedido() {
        UUID id = UUID.randomUUID();
        var dto = pedidoValido();
        when(repository.existsByTituloIgnoreCaseAndDescricaoIgnoreCaseAndIdNot(dto.titulo(), dto.descricao(), id))
                .thenReturn(true);

        assertThrows(RegistroDuplicado.class, () -> validation.validar(dto, id));
    }

    @Test
    void devePermitirCriacaoComCamposOpcionaisNulos() {
        assertDoesNotThrow(() -> validation.validar(pedidoValido(), null));
    }

    private PedidosDTO pedidoValido() {
        return new PedidosDTO("Reparo", "Computador não liga", null, Origem.EMAIL, null, null);
    }
}
