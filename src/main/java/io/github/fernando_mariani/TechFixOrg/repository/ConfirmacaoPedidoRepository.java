package io.github.fernando_mariani.TechFixOrg.repository;

import io.github.fernando_mariani.TechFixOrg.entity.ConfirmacaoPedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmacaoPedidoRepository extends JpaRepository<ConfirmacaoPedidoEntity, UUID> {

    Optional<ConfirmacaoPedidoEntity> findByTokenHash(String tokenHash);

    Optional<ConfirmacaoPedidoEntity> findByPedidoId(UUID pedidoId);
}
