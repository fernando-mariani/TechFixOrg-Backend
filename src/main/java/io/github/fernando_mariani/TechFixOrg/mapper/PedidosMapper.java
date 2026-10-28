package io.github.fernando_mariani.TechFixOrg.mapper;

import io.github.fernando_mariani.TechFixOrg.dto.PedidosDTO;
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
    PedidosEntity toEntity(PedidosDTO dto);

    PedidosResponse toResponse(PedidosEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    void atualizarEntity(PedidosDTO dto, @MappingTarget PedidosEntity entity);
}
