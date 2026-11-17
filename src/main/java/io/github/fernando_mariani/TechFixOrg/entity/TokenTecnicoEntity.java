package io.github.fernando_mariani.TechFixOrg.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "token_tecnico")
@Getter
@Setter
public class TokenTecnicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_token_tecnico"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UsuariosEntity usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime expiraEm;

    @Column(name = "confirmado_em", columnDefinition = "timestamp with time zone")
    private OffsetDateTime confirmadoEm;
}
