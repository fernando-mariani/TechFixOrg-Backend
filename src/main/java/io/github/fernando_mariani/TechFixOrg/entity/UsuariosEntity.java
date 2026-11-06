package io.github.fernando_mariani.TechFixOrg.entity;

import io.github.fernando_mariani.TechFixOrg.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data
@DynamicInsert
public class UsuariosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "senha", nullable = false, length = 300)
    private String senha;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @ColumnDefault("FALSE")
    @Column(name = "verificado", nullable = false)
    private Boolean verificado = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Generated(event = EventType.INSERT)
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
}
