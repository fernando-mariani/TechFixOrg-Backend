package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.UsuariosDTO;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.mapper.UsuariosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import io.github.fernando_mariani.TechFixOrg.response.UsuariosResponse;
import io.github.fernando_mariani.TechFixOrg.validations.UsuariosValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuariosService {

    private final UsuariosRepository repository;
    private final UsuariosMapper mapper;
    private final UsuariosValidation validation;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuariosResponse criar(UsuariosDTO dto) {
        validation.validar(dto);
        UsuariosEntity usuario = mapper.toEntity(dto);
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        return mapper.toResponse(repository.saveAndFlush(usuario));
    }
}
