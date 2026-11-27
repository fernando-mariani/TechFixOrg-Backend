package io.github.fernando_mariani.TechFixOrg.repository;

import io.github.fernando_mariani.TechFixOrg.entity.TecnicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TecnicoRepository extends JpaRepository<TecnicoEntity, UUID> {

    Optional<TecnicoEntity> findByUsuarioId(UUID usuarioId);

    @Query("select tecnico from TecnicoEntity tecnico join fetch tecnico.usuario")
    List<TecnicoEntity> listarComUsuario();
}
