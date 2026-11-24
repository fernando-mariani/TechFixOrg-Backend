package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.dto.CriarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.dto.AtualizarPedidoDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.mapper.PedidosMapper;
import io.github.fernando_mariani.TechFixOrg.repository.PedidosRepository;
import io.github.fernando_mariani.TechFixOrg.repository.TecnicoRepository;
import io.github.fernando_mariani.TechFixOrg.response.PedidosResponse;
import io.github.fernando_mariani.TechFixOrg.validations.PedidosValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
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

    public Page<PedidosResponse> buscar(String busca, UUID tecnicoId, List<Categoria> categorias,
                                         Sort.Direction ordenarPreco, int pagina, int tamanho) {
        Specification<PedidosEntity> filtroTecnico = tecnicoId == null ? Specification.unrestricted()
                : (pedido, consulta, cb) -> cb.equal(pedido.get("tecnico").get("id"), tecnicoId);
        return buscar(filtroTecnico, busca, categorias, ordenarPreco, pagina, tamanho);
    }

    public Page<PedidosResponse> buscarPorUsuarioTecnico(UUID usuarioId, String busca, List<Categoria> categorias,
                                                         Sort.Direction ordenarPreco, int pagina, int tamanho) {
        Specification<PedidosEntity> filtroPedidosPendentesDoTecnico = (pedido, consulta, cb) -> cb.and(
                cb.equal(pedido.get("tecnico").get("usuario").get("id"), usuarioId),
                cb.equal(pedido.get("status"), PedidosEntity.Status.PENDENTE));
        return buscar(filtroPedidosPendentesDoTecnico, busca, categorias, ordenarPreco, pagina, tamanho);
    }

    private Page<PedidosResponse> buscar(Specification<PedidosEntity> filtros, String busca, List<Categoria> categorias,
                                         Sort.Direction ordenarPreco, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1) {
            throw new AcaoInvalida("A página deve ser maior ou igual a zero e o tamanho deve ser positivo.");
        }
        if (busca != null && !busca.isBlank()) {
            String termo = "%" + busca.strip().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            filtros = filtros.and((pedido, consulta, cb) -> cb.or(
                    cb.like(cb.lower(pedido.get("titulo")), termo, '!'),
                    cb.like(cb.lower(pedido.get("descricao")), termo, '!')));
        }
        if (categorias != null && !categorias.isEmpty()) {
            filtros = filtros.and((pedido, consulta, cb) -> pedido.get("categoria").in(categorias));
        }

        Sort ordem = Sort.by(Sort.Order.desc("dataCriacao"), Sort.Order.asc("id"));
        if (ordenarPreco != null) {
            ordem = Sort.by(ordenarPreco, "preco").and(ordem);
        }
        return repository.findAll(filtros, PageRequest.of(pagina, tamanho, ordem)).map(mapper::toResponse);
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
