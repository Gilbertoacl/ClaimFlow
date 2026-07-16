package dev.gilbertoacl.claimflow_api.apolice.service;

import dev.gilbertoacl.claimflow_api.apolice.dto.ApoliceRequest;
import dev.gilbertoacl.claimflow_api.apolice.dto.ApoliceResponse;
import dev.gilbertoacl.claimflow_api.apolice.entity.Apolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.StatusApolice;
import dev.gilbertoacl.claimflow_api.apolice.repository.ApoliceRepository;
import dev.gilbertoacl.claimflow_api.cliente.repository.ClienteRepository;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RecursoNaoEncontradoException;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RegraDeNegocioException;
import dev.gilbertoacl.claimflow_api.shared.util.MensagensConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por gerenciar as operações relacionadas às apólices de seguro.
 */
@Service
@RequiredArgsConstructor
public class ApoliceService {

    private final ApoliceRepository apoliceRepository;
    private final ClienteRepository clienteRepository;

    /**
     * Cria uma nova apólice de seguro com base nos dados fornecidos.
     *
     * @param apoliceRequest Objeto contendo os dados da apólice a ser criada.
     * @return Objeto de resposta contendo os detalhes da apólice criada.
     * @throws RecursoNaoEncontradoException Se o cliente associado à apólice não for encontrado.
     * @throws RegraDeNegocioException        Se a apólice já existir ou se as datas de vigência forem inválidas.
     */
    public ApoliceResponse criar(ApoliceRequest apoliceRequest) {
        if (!clienteRepository.existsById(apoliceRequest.clienteId())) {
            throw new RecursoNaoEncontradoException(MensagensConstants.CLIENTE_NAO_ENCONTRADO);
        }

        if (apoliceRepository.findByNumeroApolice(apoliceRequest.numeroApolice()).isPresent()){
            throw new RegraDeNegocioException(MensagensConstants.APOLICE_JA_EXISTE);
        }

        if (!apoliceRequest.fimVigencia().isAfter(apoliceRequest.inicioVigencia())) {
            throw new RegraDeNegocioException(MensagensConstants.FIM_VIGENCIA_DEVE_SER_POSTERIOR_A_INICIO);
        }

        if (apoliceRequest.fimVigencia().isEqual(apoliceRequest.inicioVigencia())) {
            throw new RegraDeNegocioException(MensagensConstants.FIM_VIGENCIA_DEVE_SER_POSTERIOR_A_INICIO);
        }

        Apolice apolice = Apolice.builder()
                .clienteId(apoliceRequest.clienteId())
                .tipoApolice(apoliceRequest.tipoApolice())
                .numeroApolice(apoliceRequest.numeroApolice())
                .valorSegurado(apoliceRequest.valorSegurado())
                .premioMensal(apoliceRequest.premioMensal())
                .inicioVigencia(apoliceRequest.inicioVigencia())
                .fimVigencia(apoliceRequest.fimVigencia())
                .status(StatusApolice.ATIVA)
                .build();

        return toResponse(apoliceRepository.save(apolice));
    }

    /**
     * Busca uma apólice de seguro pelo seu ID.
     *
     * @param id ID da apólice a ser buscada.
     * @return Objeto de resposta contendo os detalhes da apólice encontrada.
     * @throws RecursoNaoEncontradoException Se a apólice não for encontrada.
     */
    public ApoliceResponse buscarPorId(UUID id) {
        Apolice apolice = apoliceRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MensagensConstants.APOLICE_NAO_ENCONTRADA));
        return toResponse(apolice);
    }

    /**
     * Lista todas as apólices de seguro associadas a um cliente específico.
     *
     * @param id ID do cliente cujas apólices serão listadas.
     * @return Lista de objetos de resposta contendo os detalhes das apólices encontradas.
     */
    public List<ApoliceResponse> listarApolicesPorCliente(UUID id){
        return  apoliceRepository.findAllByClienteId(id).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Converte uma entidade Apolice em um objeto de resposta ApoliceResponse.
     *
     * @param apolice Entidade Apolice a ser convertida.
     * @return Objeto de resposta ApoliceResponse contendo os detalhes da apólice.
     */
    private ApoliceResponse toResponse(Apolice apolice) {
        return new ApoliceResponse(
                apolice.getId(),
                apolice.getClienteId(),
                apolice.getTipoApolice(),
                apolice.getNumeroApolice(),
                apolice.getValorSegurado(),
                apolice.getPremioMensal(),
                apolice.getInicioVigencia(),
                apolice.getFimVigencia(),
                apolice.getStatus(),
                apolice.getDataCriacao()
        );
    }

    /**
     * Busca uma apolice existente e faz o cancelamento dela no sistema.
     *
     * @param id ID da apólice a ser buscada.
     * @return Objeto de resposta contendo os detalhes da apólice cancelada.
     * @throws RecursoNaoEncontradoException Se a apólice buscada não for encontrada.
     * @throws RegraDeNegocioException  Se a apólice ja for cancelada ou se a vigência estiver vencida.
     */
    public ApoliceResponse cancelarApolice(UUID id) {
        Apolice apolice = apoliceRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MensagensConstants.APOLICE_NAO_ENCONTRADA));

        if (apolice.getStatus() == StatusApolice.CANCELADA) {
            throw new RegraDeNegocioException(MensagensConstants.APOLICE_JA_CANCELADA);
        }

        if (apolice.getStatus() == StatusApolice.VENCIDA) {
            throw new RegraDeNegocioException(MensagensConstants.APOLICE_VENCIDA_NAO_PODE_CANCELAR);
        }

        apolice.setStatus(StatusApolice.CANCELADA);
        return toResponse(apoliceRepository.save(apolice));
    }
}
