package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.AtribuirPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Status;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.mapper.PedidosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import io.github.fernando_mariani.TechFixOrg.repository.TecnicoRepository;
import io.github.fernando_mariani.TechFixOrg.response.GerenteTecnicoResponse;
import io.github.fernando_mariani.TechFixOrg.response.PedidosResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GerenteService {

    private final TecnicoRepository tecnicoRepository;
    private final PedidosRepository pedidosRepository;
    private final PedidosMapper pedidosMapper;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<GerenteTecnicoResponse> listarTecnicos() {
        return tecnicoRepository.listarComUsuario().stream()
                .map(tecnico -> new GerenteTecnicoResponse(tecnico.getId(), tecnico.getNome(),
                        tecnico.getUsuario().getEmail(), tecnico.getCategorias()))
                .toList();
    }

    @Transactional
    public PedidosResponse atribuirPedido(UUID id, AtribuirPedidoDTO dto) {
        var pedido = pedidosRepository.findForUpdateById(id)
                .orElseThrow(() -> new NaoEncontrado("Pedido com ID " + id + " não encontrado."));
        if (pedido.getStatus() == Status.PENDENTE || pedido.getStatus() == Status.PRONTO) {
            throw new AcaoInvalida("O pedido já foi confirmado pelo cliente.");
        }

        var tecnico = tecnicoRepository.findById(dto.tecnicoId())
                .orElseThrow(() -> new NaoEncontrado("Técnico com ID " + dto.tecnicoId() + " não encontrado."));
        pedido.setTecnico(tecnico);
        pedido.setPreco(dto.preco());
        pedido.setStatus(Status.AGUARDANDO_CONFIRMACAO);

        var response = pedidosMapper.toResponse(pedidosRepository.saveAndFlush(pedido));
        emailService.enviarDetalhesPedido(id);
        return response;
    }
}
