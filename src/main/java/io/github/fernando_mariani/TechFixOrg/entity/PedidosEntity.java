package io.github.fernando_mariani.TechFixOrg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ForeignKey;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

import java.math.BigDecimal;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id", foreignKey = @ForeignKey(name = "fk_pedidos_tecnico"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private TecnicoEntity tecnico;

    @Column(name = "nome_cliente", nullable = false, length = 100)
    private String nomeCliente;

    @Column(name = "email_cliente", nullable = false, length = 200)
    private String emailCliente;

    @Column(name = "preco", precision = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "categoria", nullable = false, columnDefinition = "categoria")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @ColumnDefault("'AGUARDANDO_VERIFICACAO'")
    @Column(name = "status", columnDefinition = "status")
    private Status status = Status.AGUARDANDO_VERIFICACAO;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Generated(event = EventType.INSERT)
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    public enum Status {
        AGUARDANDO_VERIFICACAO,
        AGUARDANDO_CONFIRMACAO,
        PENDENTE,
        PRONTO
    }

    public enum Categoria {
        HARDWARE,
        SOFTWARE,
        REDE,
        OUTROS
    }

    public enum Origem {
        EMAIL,
        FORMS,
        WHATSAPP
    }

}
