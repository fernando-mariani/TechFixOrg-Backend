package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.AtualizarRoleDTO;
import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.enums.Role;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.mapper.UsuariosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import io.github.fernando_mariani.TechFixOrg.response.UsuariosResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UsuariosRepository repository;
    private final UsuariosMapper mapper;
    private final EmailService emailService;

    public Page<UsuariosResponse> buscar(String busca, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1) {
            throw new AcaoInvalida("A página deve ser maior ou igual a zero e o tamanho deve ser positivo.");
        }

        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("verificado", "nome", "id"));
        if (busca == null || busca.isBlank()) {
            return repository.findAll(paginacao).map(mapper::toResponse);
        }

        String termo = busca.strip();
        return repository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(termo, termo, paginacao)
                .map(mapper::toResponse);
    }

    @Transactional
    public UsuariosResponse verificarUsuario(UUID id) {
        UsuariosEntity usuario = buscarUsuario(id);
        usuario.setVerificado(true);
        return mapper.toResponse(repository.saveAndFlush(usuario));
    }

    @Transactional
    public UsuariosResponse atualizarRole(UUID id, AtualizarRoleDTO dto) {
        UsuariosEntity usuario = buscarUsuario(id);
        if(usuario.getVerificado() == false) {
            usuario.setVerificado(true);
        }
        usuario.setRole(dto.role());
        if(dto.role() == Role.TECNICO) {
            emailService.enviarConviteTecnico(id);
        }
        return mapper.toResponse(repository.saveAndFlush(usuario));
    }

    private UsuariosEntity buscarUsuario(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NaoEncontrado("Usuário com ID " + id + " não encontrado."));
    }
}
