package io.github.fernando_mariani.TechFixOrg.mapper;

import io.github.fernando_mariani.TechFixOrg.dto.UsuariosDTO;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.response.UsuariosResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UsuariosMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "verificado", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    UsuariosEntity toEntity(UsuariosDTO dto);

    UsuariosResponse toResponse(UsuariosEntity entity);
}
