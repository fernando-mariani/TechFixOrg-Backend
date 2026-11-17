package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.CriarTecnicoDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;
import io.github.fernando_mariani.TechFixOrg.entity.TecnicoEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.repository.TecnicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TecnicoService {

    private final TecnicoRepository repository;
    private final TokenTecnicoService tokenTecnicoService;

    @Transactional
    public void criar(UUID usuarioId, String token, CriarTecnicoDTO dto) {
        var usuario = tokenTecnicoService.confirmar(token, usuarioId);
        if (repository.findByUsuarioId(usuarioId).isPresent()) {
            throw new RegistroDuplicado("Este usuário já tem um cadastro de técnico.");
        }

        var tecnico = new TecnicoEntity();
        tecnico.setUsuario(usuario);
        tecnico.setNome(usuario.getNome());
        tecnico.setCategorias(dto.categorias().stream().distinct().toArray(Categoria[]::new));
        repository.save(tecnico);
    }
}
