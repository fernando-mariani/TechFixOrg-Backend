package io.github.fernando_mariani.TechFixOrg.repository;

import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PedidosRepository extends JpaRepository<PedidosEntity, UUID> {

    boolean existsByTituloIgnoreCaseAndDescricaoIgnoreCase(String titulo, String descricao);

    boolean existsByTituloIgnoreCaseAndDescricaoIgnoreCaseAndIdNot(String titulo, String descricao, UUID id);
}
