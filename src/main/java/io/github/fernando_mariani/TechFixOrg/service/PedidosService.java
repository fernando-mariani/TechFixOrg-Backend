package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.CriarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.dto.AtualizarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.mapper.PedidosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import io.github.fernando_mariani.TechFixOrg.repository.TecnicoRepository;
import io.github.fernando_mariani.TechFixOrg.response.PedidosResponse;
import io.github.fernando_mariani.TechFixOrg.validations.PedidosValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidosService {

    private final PedidosRepository repository;
    private final TecnicoRepository tecnicoRepository;
    private final PedidosMapper mapper;
    private final PedidosValidation validation;
    private final EmailService emailService;

    @Transactional
    public PedidosResponse criar(CriarPedidoDTO dto) {
        validation.validar(dto, null);
        PedidosEntity pedido = mapper.toEntity(dto);
        return mapper.toResponse(repository.saveAndFlush(pedido));
    }

    public List<PedidosResponse> listar() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PedidosResponse buscarPorId(UUID id) {
        return mapper.toResponse(buscarPedido(id));
    }

    @Transactional
    public PedidosResponse atualizar(UUID id, AtualizarPedidoDTO dto) {
        PedidosEntity pedido = buscarPedido(id);
        validation.validar(dto, id);
        var tecnico = dto.tecnicoId() == null ? null : tecnicoRepository.findById(dto.tecnicoId())
                .orElseThrow(() -> new NaoEncontrado("Técnico com ID " + dto.tecnicoId() + " não encontrado."));
        mapper.atualizarEntity(dto, pedido);
        pedido.setTecnico(tecnico);
        return mapper.toResponse(repository.saveAndFlush(pedido));
    }

    @Transactional
    public PedidosResponse confirmar(UUID id) {
        PedidosEntity pedido = repository.findForUpdateById(id)
                .orElseThrow(() -> new NaoEncontrado("Pedido com ID " + id + " não encontrado."));
        if (pedido.getStatus() != PedidosEntity.Status.PENDENTE) {
            throw new AcaoInvalida("Somente pedidos com status PENDENTE podem ser marcados como PRONTO.");
        }
        pedido.setStatus(PedidosEntity.Status.PRONTO);
        PedidosEntity pedidoPronto = repository.saveAndFlush(pedido);
        emailService.enviarPedidoPronto(pedidoPronto);
        return mapper.toResponse(pedidoPronto);
    }

    @Transactional
    public void excluir(UUID id) {
        PedidosEntity pedido = buscarPedido(id);
        repository.delete(pedido);
    }

    private PedidosEntity buscarPedido(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NaoEncontrado("Pedido com ID " + id + " não encontrado."));
    }
}
