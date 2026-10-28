package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.PedidosDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.mapper.PedidosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
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
    private final PedidosMapper mapper;
    private final PedidosValidation validation;

    @Transactional
    public PedidosResponse criar(PedidosDTO dto) {
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
    public PedidosResponse atualizar(UUID id, PedidosDTO dto) {
        PedidosEntity pedido = buscarPedido(id);
        validation.validar(dto, id);
        mapper.atualizarEntity(dto, pedido);
        return mapper.toResponse(repository.saveAndFlush(pedido));
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
