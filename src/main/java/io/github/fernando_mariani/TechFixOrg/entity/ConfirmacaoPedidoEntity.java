package io.github.fernando_mariani.TechFixOrg.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "confirmacao_pedido")
@Getter
@Setter
public class ConfirmacaoPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_confirmacao_pedido"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PedidosEntity pedido;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime expiraEm;

    @Column(name = "confirmado_em", columnDefinition = "timestamp with time zone")
    private OffsetDateTime confirmadoEm;
}
