package dev.gilbertoacl.claimflow_api.sinistro.service;

import dev.gilbertoacl.claimflow_api.apolice.entity.Apolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.StatusApolice;
import dev.gilbertoacl.claimflow_api.apolice.repository.ApoliceRepository;
import dev.gilbertoacl.claimflow_api.historicosinistro.service.HistoricoSinistroService;
import dev.gilbertoacl.claimflow_api.shared.exceptions.EstadoInvalidoException;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RecursoNaoEncontradoException;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RegraDeNegocioException;
import dev.gilbertoacl.claimflow_api.shared.util.MensagensConstants;
import dev.gilbertoacl.claimflow_api.sinistro.dto.AtualizarStatusSinistroRequest;
import dev.gilbertoacl.claimflow_api.sinistro.dto.SinistroRequest;
import dev.gilbertoacl.claimflow_api.sinistro.dto.SinistroResponse;
import dev.gilbertoacl.claimflow_api.sinistro.entity.Sinistro;
import dev.gilbertoacl.claimflow_api.sinistro.entity.StatusSinistro;
import dev.gilbertoacl.claimflow_api.sinistro.repository.SinistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SinistroService {
    private static final Map<StatusSinistro, Set<StatusSinistro>> TRANSICOES_VALIDAS = Map.of(
            StatusSinistro.ABERTO, Set.of(StatusSinistro.EM_ANALISE),
            StatusSinistro.EM_ANALISE, Set.of(StatusSinistro.APROVADO, StatusSinistro.NEGADO),
            StatusSinistro.APROVADO, Set.of(StatusSinistro.PAGO),
            StatusSinistro.NEGADO, Set.of(),
            StatusSinistro.PAGO, Set.of()
    );
    private final SinistroRepository sinistroRepository;
    private final ApoliceRepository apoliceRepository;
    private final HistoricoSinistroService historicoSinistroService;

    /**
     * Responsável pela abertura de sinistros vinculado a apolices no sistema.
     *
     * @param request os dados do sinistro a ser aberto.
     * @return contendo o SinistroResponse representando o sinistro criado.
     * @throws RecursoNaoEncontradoException Se a apolce não for encontrada.
     * @throws RegraDeNegocioException se A apolce não for ativa, estiver fora da vigência, Valores fora dos contratados
     */
    public SinistroResponse abrirSinistro(SinistroRequest request, UUID responsavelId) {
        Apolice apolice = apoliceRepository.findById(request.apoliceId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(MensagensConstants.APOLICE_NAO_ENCONTRADA));

        if (apolice.getStatus() != StatusApolice.ATIVA) {
            throw new RegraDeNegocioException(MensagensConstants.APOLICE_INATIVA_NAO_PODE_ABRIR_SINISTRO);
        }

        LocalDate inicioVigencia = apolice.getInicioVigencia().toLocalDate();
        LocalDate fimVigencia = apolice.getFimVigencia().toLocalDate();
        if (request.dataOcorrido().isBefore(inicioVigencia) || request.dataOcorrido().isAfter(fimVigencia)){
            throw new RegraDeNegocioException(MensagensConstants.SINISTRO_FORA_DE_VIGTENCIA);
        }

        if (request.valorSolicitado().compareTo(apolice.getValorSegurado()) > 0) {
            throw new RegraDeNegocioException(MensagensConstants.VALOR_SOLICITADO_MAIOR_QUE_SEGURADO);
        }

        String numeroSinistro = gerarNumeroSinistro(apolice);

        Sinistro sinistroBuilder = Sinistro.builder()
                .apoliceId(request.apoliceId())
                .dataOcorrido(request.dataOcorrido())
                .descricao(request.decricao())
                .valorSolicitado(request.valorSolicitado())
                .statusSinistro(StatusSinistro.ABERTO)
                .numeroSinistro(numeroSinistro)
                .build();

        Sinistro sinistro = sinistroRepository.save(sinistroBuilder);
        historicoSinistroService.registrar(sinistro, responsavelId, MensagensConstants.SINISTRO_ABERTO);
        return  toResponse(sinistro);
    }

    /**
     * Busca o sinistro por ID enviado
     *
     * @param id o ID do sinistro.
     * @return o SinistroResponse encontrado.
     */
    public SinistroResponse buscarPorId(UUID id) {
        Sinistro sinistro = sinistroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MensagensConstants.SINISTRO_NAO_ENCONTRADO));
        return toResponse(sinistro);
    }

    /**
     * Lista todos os sinistros por apoliceID
     *
     * @param apoliceId o ID da aplice.
     * @return o Lista de SinistroResponse encontrados.
     */
    public List<SinistroResponse> listarPorApolice(UUID apoliceId) {
        return sinistroRepository.findAllByApoliceId(apoliceId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Atualiza o status do andamento de um sinistro com verificações status válidos.
     *
     * @param request o request com os dados para atualização do sinsitro
     * @return o SinistroResponse com os dados atualizados.
     * @throws RecursoNaoEncontradoException Caso não encontre o sinistro
     * @throws EstadoInvalidoException caso a transição entre status não for válida
     */
    public SinistroResponse atualizarStatus(AtualizarStatusSinistroRequest request, UUID resposnsavelId){
        Sinistro sinistro = sinistroRepository.findByNumeroSinistro(request.numeroSinistro())
                .orElseThrow(() -> new RecursoNaoEncontradoException(MensagensConstants.SINISTRO_NAO_ENCONTRADO));

        StatusSinistro statusAtual = sinistro.getStatusSinistro();
        StatusSinistro novoStatus = request.novoStatusSinistro();

        if (!TRANSICOES_VALIDAS.getOrDefault(statusAtual, Set.of()).contains(novoStatus)) {
            throw new EstadoInvalidoException("Transição de %s para %s não é permitida.".formatted(statusAtual, novoStatus));
        }

        sinistro.setStatusSinistro(novoStatus);
        Sinistro sinistroAtualizado = sinistroRepository.save(sinistro);
        historicoSinistroService.registrar(sinistroAtualizado, resposnsavelId, request.observacao());
        return toResponse(sinistroAtualizado);
    }

    /**
     * Busca o sisnitro pelo numero unico criado na abertura
     *
     * @param numeroSinistro identificador unico do sisnitro
     * @return o SinistroResponse do sinistro encontrado
     * @throws RecursoNaoEncontradoException caso não encontre nenhum sinsitro
     */
    public SinistroResponse buscarPorNumeroSinistro(String numeroSinistro) {
        Sinistro sinistro = sinistroRepository.findByNumeroSinistro(numeroSinistro)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MensagensConstants.SINISTRO_NAO_ENCONTRADO));
        return toResponse(sinistro);
    }

    /**
     * Converte um objeto Sinistro para EnderecoResponse.
     *
     * @param sinistro O objeto Endereco a ser convertido.
     * @return Um objeto SinistroResponse representando o sinistro.
     */
    private SinistroResponse toResponse(Sinistro sinistro) {
        return  new SinistroResponse(
                sinistro.getId(),
                sinistro.getApoliceId(),
                sinistro.getNumeroSinistro(),
                sinistro.getDataOcorrido(),
                sinistro.getDescricao(),
                sinistro.getValorSolicitado(),
                sinistro.getValorAprovado(),
                sinistro.getStatusSinistro(),
                sinistro.getDataCriacao(),
                sinistro.getDataAtualizacao()
        );
    }

    /**
     * Responsável pela criaçaõ do identificador único do sinistro aberto, composto pelo ramo da apolice
     * a data corrente e aquantidade de sinsitros daquele ramo cadastrados no dia.
     *
     * @param apolice A apolice do sinistros que vai ser gerada a chave unica.
     * @return a String de chave unica do sinistro a ser aberto.
     * @throws RecursoNaoEncontradoException Caso exceda o limite de 9999 sinsitros do mesmo ramo no dia.
     */
    private String gerarNumeroSinistro(Apolice apolice) {
        String ramo = apolice.getTipoApolice().getCodigoRamo();
        String dataAtual = LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefixo = ramo + dataAtual;

        long qtSinistroDia = sinistroRepository.countByNumeroSinistroStartingWith(prefixo);
        if (qtSinistroDia >= 9999) {
            throw new RegraDeNegocioException(MensagensConstants.LIMITE_DIARIO_SINISTRO_ATINGIDO + apolice.getTipoApolice());
        }

        String sequecial = String.format("%04d", qtSinistroDia + 1);
        return prefixo + sequecial;
    }
}
