package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.AtualizarRoleDTO;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.mapper.UsuariosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import io.github.fernando_mariani.TechFixOrg.response.UsuariosResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UsuariosRepository repository;
    private final UsuariosMapper mapper;

    @Transactional
    public UsuariosResponse verificarUsuario(UUID id) {
        UsuariosEntity usuario = buscarUsuario(id);
        usuario.setVerificado(true);
        return mapper.toResponse(repository.saveAndFlush(usuario));
    }

    @Transactional
    public UsuariosResponse atualizarRole(UUID id, AtualizarRoleDTO dto) {
        UsuariosEntity usuario = buscarUsuario(id);
        usuario.setRole(dto.role());
        return mapper.toResponse(repository.saveAndFlush(usuario));
    }

    private UsuariosEntity buscarUsuario(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NaoEncontrado("Usuário com ID " + id + " não encontrado."));
    }
}
