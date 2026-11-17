package io.github.fernando_mariani.TechFixOrg.repository;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidosRepository extends JpaRepository<PedidosEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PedidosEntity> findForUpdateById(UUID id);

    boolean existsByTituloIgnoreCaseAndDescricaoIgnoreCase(String titulo, String descricao);

    boolean existsByTituloIgnoreCaseAndDescricaoIgnoreCaseAndIdNot(String titulo, String descricao, UUID id);
}
