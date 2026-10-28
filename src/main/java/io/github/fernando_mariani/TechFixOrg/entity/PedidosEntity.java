package io.github.fernando_mariani.TechFixOrg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
@Data
@DynamicInsert
public class PedidosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "descricao", nullable = false, length = 300)
    private String descricao;

    @Column(name = "imagem", length = 300)
    private String imagem;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "origem", nullable = false, columnDefinition = "origem")
    private Origem origem;

    @Column(name = "tecnico", length = 100)
    private String tecnico;

    @Column(name = "data_marcada")
    private LocalDateTime dataMarcada;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Generated(event = EventType.INSERT)
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    public enum Origem {
        EMAIL,
        FORMS,
        WHATSAPP
    }

}
