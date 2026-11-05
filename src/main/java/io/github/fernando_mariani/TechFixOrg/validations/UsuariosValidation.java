package io.github.fernando_mariani.TechFixOrg.validations;

import io.github.fernando_mariani.TechFixOrg.dto.UsuariosDTO;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.repository.UsuariosRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsuariosValidation {

    private final UsuariosRepository repository;
    private final Validator validator;

    public void validar(UsuariosDTO dto) {
        if (dto == null) {
            throw new AcaoInvalida("Os dados do usuário são obrigatórios.");
        }

        var violacoes = validator.validate(dto);
        if (!violacoes.isEmpty()) {
            String mensagem = violacoes.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(" "));
            throw new AcaoInvalida(mensagem);
        }

        if (dto.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new AcaoInvalida("A senha deve ter no máximo 72 bytes em UTF-8.");
        }

        if (repository.existsByEmailIgnoreCase(dto.email())) {
            throw new RegistroDuplicado("Já existe um usuário com este e-mail.");
        }
    }
}
