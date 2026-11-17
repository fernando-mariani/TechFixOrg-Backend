package io.github.fernando_mariani.TechFixOrg.repository;

import io.github.fernando_mariani.TechFixOrg.entity.TokenTecnicoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenTecnicoRepository extends JpaRepository<TokenTecnicoEntity, UUID> {

    Optional<TokenTecnicoEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TokenTecnicoEntity> findFirstByTokenHash(String tokenHash);

    Optional<TokenTecnicoEntity> findByUsuarioId(UUID usuarioId);
}
