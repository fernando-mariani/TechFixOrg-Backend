package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.entity.UsuariosEntity;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuariosRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return toUserDetails(repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado.")));
    }

    public UserDetails carregarPorId(UUID id) {
        return toUserDetails(repository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado.")));
    }

    private UserDetails toUserDetails(UsuariosEntity usuario) {
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .roles(usuario.getRole())
                .build();
    }
}
