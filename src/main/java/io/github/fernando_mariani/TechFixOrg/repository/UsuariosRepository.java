package io.github.fernando_mariani.TechFixOrg.repository;

import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface UsuariosRepository extends JpaRepository<UsuariosEntity, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<UsuariosEntity> findByEmailIgnoreCase(String email);
}
