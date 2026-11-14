package io.github.fernando_mariani.TechFixOrg.validations;

import io.github.fernando_mariani.TechFixOrg.dto.CriarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.dto.AtualizarPedidoDTO;
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


    public void validar(CriarPedidoDTO dto, UUID id) {
        validarDados(dto);
        validarDuplicidade(dto.titulo(), dto.descricao(), id);
    }

    public void validar(AtualizarPedidoDTO dto, UUID id) {
        validarDados(dto);
        validarDuplicidade(dto.titulo(), dto.descricao(), id);
    }

    private void validarDados(Object dto) {
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

    }

    private void validarDuplicidade(String titulo, String descricao, UUID id) {
        boolean duplicado = id == null
                ? repository.existsByTituloIgnoreCaseAndDescricaoIgnoreCase(titulo, descricao)
                : repository.existsByTituloIgnoreCaseAndDescricaoIgnoreCaseAndIdNot(titulo, descricao, id);

        if (duplicado) {
            throw new RegistroDuplicado("Já existe um pedido com o mesmo título e descrição.");
        }
    }
}
