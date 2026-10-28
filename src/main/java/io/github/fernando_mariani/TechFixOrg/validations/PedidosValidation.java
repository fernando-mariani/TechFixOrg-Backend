package io.github.fernando_mariani.TechFixOrg.validations;

import io.github.fernando_mariani.TechFixOrg.dto.PedidosDTO;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PedidosValidation {

    private final PedidosRepository repository;
    private final Validator validator;

    // Na criação, o ID é nulo. Na atualização, o próprio pedido é excluído da busca.
    public void validar(PedidosDTO dto, UUID id) {
        if (dto == null) {
            throw new AcaoInvalida("Os dados do pedido são obrigatórios.");
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

        boolean duplicado = id == null
                ? repository.existsByTituloIgnoreCaseAndDescricaoIgnoreCase(dto.titulo(), dto.descricao())
                : repository.existsByTituloIgnoreCaseAndDescricaoIgnoreCaseAndIdNot(dto.titulo(), dto.descricao(), id);

        if (duplicado) {
            throw new RegistroDuplicado("Já existe um pedido com o mesmo título e descrição.");
        }
    }
}
