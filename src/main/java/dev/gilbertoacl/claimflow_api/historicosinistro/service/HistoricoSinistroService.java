package dev.gilbertoacl.claimflow_api.historicosinistro.service;

import dev.gilbertoacl.claimflow_api.historicosinistro.dto.HistoricoSinistroResponse;
import dev.gilbertoacl.claimflow_api.historicosinistro.entity.HistoricoSinistro;
import dev.gilbertoacl.claimflow_api.historicosinistro.repository.HistoricoSinistroRepository;
import dev.gilbertoacl.claimflow_api.sinistro.entity.Sinistro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoricoSinistroService {
    private final HistoricoSinistroRepository historicoSinistroRepository;

    /**
     * Responsável pela criação do historico de andamento de cada sinistro
     *
     * @param sinistro o sinistro a ser vinculado o histórico
     * @param responsavelId o responsável pela atualização do sinistro
     * @param observacao as informações que o responsável passou para atualização do sinistro.
     */
    public void registrar(Sinistro sinistro, UUID responsavelId, String observacao) {
        HistoricoSinistro historico = HistoricoSinistro.builder()
                .sinistroId(sinistro.getId())
                .numeroSinistro(sinistro.getNumeroSinistro())
                .statusSinistro(sinistro.getStatusSinistro())
                .responsavelId(responsavelId)
                .observacao(observacao)
                .build();

        historicoSinistroRepository.save(historico);
    }

    /**
     * Lista o hitorico de alterações do sinistro através do id
     *
     * @param sinistroId id do sinistro a ser buscado
     * @return uma lista de HistoricoSinistroResponse com os dados registrados
     */
    public List<HistoricoSinistroResponse> listarPorSinistroId(UUID sinistroId) {
        return historicoSinistroRepository.findAllBySinistroIdOrderByDataAlteracaoAsc(sinistroId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lista o hitorico de alterações do sinistro através do numero do sinistro
     *
     * @param numeroSinistro id do sinistro a ser buscado
     * @return uma lista de HistoricoSinistroResponse com os dados registrados
     */
    public List<HistoricoSinistroResponse> listarPorNumeroSinistro(String numeroSinistro) {
        return historicoSinistroRepository.findAllByNumeroSinistroOrderByDataAlteracaoAsc(numeroSinistro).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Consverte um historico de sinistro em um HistoricoSinistroResponse
     * @param historicoSinistro objeto a ser convertido
     * @return um objeto de HistoricoSinistroResponse representando o historico.
     */
    private HistoricoSinistroResponse toResponse(HistoricoSinistro historicoSinistro) {
        return new HistoricoSinistroResponse(
                historicoSinistro.getId(),
                historicoSinistro.getNumeroSinistro(),
                historicoSinistro.getStatusSinistro(),
                historicoSinistro.getResponsavelId(),
                historicoSinistro.getObservacao(),
                historicoSinistro.getDataAlteracao()
        );
    }
}
