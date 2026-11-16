package io.github.fernando_mariani.TechFixOrg.mapper;

import io.github.fernando_mariani.TechFixOrg.dto.AtualizarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.dto.CriarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PedidosMapperTest {

    private final PedidosMapper mapper = Mappers.getMapper(PedidosMapper.class);

    @Test
    void devePreservarCamposNaEdicaoENaResposta() {
        var dto = pedido("João", new BigDecimal("150.50"));

        var entity = new PedidosEntity();
        mapper.atualizarEntity(dto, entity);
        var response = mapper.toResponse(entity);

        assertEquals(dto.tecnico(), response.tecnico());
        assertEquals(dto.nomeCliente(), response.nomeCliente());
        assertEquals(dto.emailCliente(), response.emailCliente());
        assertEquals(dto.preco(), response.preco());
        assertEquals(dto.categoria(), response.categoria());
    }

    @Test
    void deveAceitarCadastroSemCamposOpcionais() {
        var entity = mapper.toEntity(new CriarPedidoDTO("Reparo", "Computador não liga", PedidosEntity.Origem.EMAIL, "Maria", "maria@example.com", PedidosEntity.Categoria.HARDWARE));

        assertNull(entity.getImagem());
        assertEquals("Maria", entity.getNomeCliente());
        assertNull(entity.getTecnico());
        assertNull(entity.getPreco());
    }

    @Test
    void deveAtualizarCamposPreservandoIdEDataCriacao() {
        var entity = new PedidosEntity();
        var id = UUID.randomUUID();
        var criacao = LocalDateTime.of(2026, 11, 14, 10, 0);
        entity.setId(id);
        entity.setStatus(PedidosEntity.Status.PENDENTE);
        entity.setDataCriacao(criacao);
        var dto = pedido("João", new BigDecimal("150.50"));

        mapper.atualizarEntity(dto, entity);

        assertEquals(id, entity.getId());
        assertEquals(PedidosEntity.Status.PENDENTE, entity.getStatus());
        assertEquals(criacao, entity.getDataCriacao());
        assertEquals(dto.nomeCliente(), entity.getNomeCliente());
        assertEquals(dto.emailCliente(), entity.getEmailCliente());
        assertEquals(dto.categoria(), entity.getCategoria());
        assertEquals(dto.tecnico(), entity.getTecnico());
        assertEquals(dto.preco(), entity.getPreco());
    }

    private AtualizarPedidoDTO pedido(String tecnico, BigDecimal preco) {
        return new AtualizarPedidoDTO("Reparo", "Computador não liga", null, PedidosEntity.Origem.EMAIL,
                tecnico, "Maria", "maria@example.com",
                preco, PedidosEntity.Categoria.HARDWARE);
    }
}
