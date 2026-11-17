package io.github.fernando_mariani.TechFixOrg.repository;

import io.github.fernando_mariani.TechFixOrg.entity.TecnicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TecnicoRepository extends JpaRepository<TecnicoEntity, UUID> {

    Optional<TecnicoEntity> findByUsuarioId(UUID usuarioId);
}
