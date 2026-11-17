package io.github.fernando_mariani.TechFixOrg.entity;

import jakarta.persistence.*;
import io.github.fernando_mariani.TechFixOrg.config.CategoriaArrayJdbcType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.util.UUID;

@Entity
@Table(name = "tecnico")
@Getter
@Setter
public class TecnicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_tecnico"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UsuariosEntity usuario;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @JdbcType(CategoriaArrayJdbcType.class)
    @Column(name = "categorias", nullable = false, columnDefinition = "categoria[]")
    @ColumnTransformer(write = "?::categoria[]")
    @ColumnDefault("'{}'")
    private PedidosEntity.Categoria[] categorias = new PedidosEntity.Categoria[0];
}
