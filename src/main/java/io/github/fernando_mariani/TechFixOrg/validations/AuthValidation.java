package io.github.fernando_mariani.TechFixOrg.validations;

import io.github.fernando_mariani.TechFixOrg.dto.LoginDTO;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthValidation {

    private final Validator validator;

    public void validar(LoginDTO dto) {
        if (dto == null) {
            throw new AcaoInvalida("Os dados de login são obrigatórios.");
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
    }
}
