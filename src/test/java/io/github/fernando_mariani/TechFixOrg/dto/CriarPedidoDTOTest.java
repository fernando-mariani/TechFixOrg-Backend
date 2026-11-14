package io.github.fernando_mariani.TechFixOrg.dto;

import io.github.fernando_mariani.TechFixOrg.mapper.PedidosMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CriarPedidoDTOTest {

    @Test
    void deveIgnorarCamposExtrasEnviadosNoCadastro() {
        var dto = JsonMapper.builder().findAndAddModules().build().readValue("""
                {
                  "titulo": "Reparo",
                  "descricao": "Computador não liga",
                  "origem": "EMAIL",
                  "nomeCliente": "Maria",
                  "emailCliente": "maria@example.com",
                  "dataLimite": "2027-01-11",
                  "categoria": "HARDWARE",
                  "imagem": "imagem.png",
                  "tecnico": "João",
                  "dataMarcada": "2027-01-10T14:30:00",
                  "preco": 150.50,
                  "id": "ignorado",
                  "dataCriacao": "ignorada"
                }
                """, CriarPedidoDTO.class);

        var entity = Mappers.getMapper(PedidosMapper.class).toEntity(dto);

        assertEquals("Reparo", entity.getTitulo());
        assertEquals(LocalDate.of(2027, 1, 11), entity.getDataLimite());
        assertNull(entity.getImagem());
        assertNull(entity.getTecnico());
        assertNull(entity.getDataMarcada());
        assertNull(entity.getPreco());
        assertNull(entity.getId());
        assertNull(entity.getDataCriacao());
    }
}
