package io.github.fernando_mariani.TechFixOrg.mapper;

import io.github.fernando_mariani.TechFixOrg.dto.CriarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.dto.AtualizarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import io.github.fernando_mariani.TechFixOrg.response.PedidosResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PedidosMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "imagem", ignore = true)
    @Mapping(target = "tecnico", ignore = true)
    @Mapping(target = "preco", ignore = true)
    PedidosEntity toEntity(CriarPedidoDTO dto);

    PedidosResponse toResponse(PedidosEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "status", ignore = true)
    void atualizarEntity(AtualizarPedidoDTO dto, @MappingTarget PedidosEntity entity);
}
