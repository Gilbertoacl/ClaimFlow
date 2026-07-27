package dev.gilbertoacl.claimflow_api.sinistro.service;

import dev.gilbertoacl.claimflow_api.apolice.entity.Apolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.StatusApolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.TipoApolice;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
class SinistroServiceTest {
    @Mock
    private SinistroRepository sinistroRepository;
    @Mock
    private ApoliceRepository apoliceRepository;
    @Mock
    private HistoricoSinistroService historicoSinistroService;

    @InjectMocks
    private SinistroService sinistroService;

    private Apolice apoliceValida() {
        return Apolice.builder()
                .id(UUID.randomUUID())
                .tipoApolice(TipoApolice.AUTOMOVEL)
                .status(StatusApolice.ATIVA)
                .valorSegurado(BigDecimal.valueOf(10000))
                .inicioVigencia(OffsetDateTime.now().minusMonths(1))
                .fimVigencia(OffsetDateTime.now().plusMonths(11))
                .build();
    }

    private SinistroRequest requestValido(UUID apoliceId){
        return new SinistroRequest(
                apoliceId,
                LocalDate.now(),
                "Colisão Traseira.",
                BigDecimal.valueOf(2000)
        );
    }

    @Test
    void deveAbrirSinistroComSucesso() {
        Apolice apolice = apoliceValida();
        SinistroRequest request = requestValido(apolice.getId());

        when(apoliceRepository.findById(apolice.getId())).thenReturn(Optional.of(apolice));
        when(sinistroRepository.countByNumeroSinistroStartingWith(anyString())).thenReturn(0L);
        when(sinistroRepository.save(any(Sinistro.class))).thenAnswer(invocation -> {
            Sinistro s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            s.setDataCriacao(LocalDateTime.now());
            s.setDataAtualizacao(LocalDateTime.now());
            return s;
        });

        SinistroResponse response = sinistroService.abrirSinistro(request, UUID.randomUUID());

        assertThat(response.id()).isNotNull();
        assertThat(response.statusSinistro()).isEqualTo(StatusSinistro.ABERTO);
        verify(sinistroRepository).save(any(Sinistro.class));
    }

    @Test
    void deveLancarExcecaoQuandoApoliceNaoEncontrada() {
        UUID apoliceId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        when(apoliceRepository.findById(apoliceId)).thenReturn(Optional.empty());

        SinistroRequest request = requestValido(apoliceId);
        assertThatThrownBy(() -> sinistroService.abrirSinistro(request, responsavelId)).isInstanceOf(RecursoNaoEncontradoException.class);

        verify(sinistroRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoApoliceNaoEstaAtiva() {
        UUID id = UUID.randomUUID();
        Apolice apolice = apoliceValida();
        apolice.setStatus(StatusApolice.CANCELADA);
        when(apoliceRepository.findById(apolice.getId())).thenReturn(Optional.of(apolice));

        SinistroRequest request = requestValido(apolice.getId());
        assertThatThrownBy(() -> sinistroService.abrirSinistro(request, id))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage(MensagensConstants.APOLICE_INATIVA_NAO_PODE_ABRIR_SINISTRO);

        verify(sinistroRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoDataOcorridoForaDaVigencia() {
        Apolice apolice = apoliceValida();
        UUID id = UUID.randomUUID();
        SinistroRequest request = new SinistroRequest(
                apolice.getId(), LocalDate.now().minusYears(2), "Evento antigo", BigDecimal.valueOf(1000)
        );
        when(apoliceRepository.findById(apolice.getId())).thenReturn(Optional.of(apolice));

        assertThatThrownBy(() -> sinistroService.abrirSinistro(request, id))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage(MensagensConstants.SINISTRO_FORA_DE_VIGTENCIA);

        verify(sinistroRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoValorSolicitadoMaiorQueValorSegurado() {
        Apolice apolice = apoliceValida();
        UUID id = UUID.randomUUID();
        SinistroRequest request = new SinistroRequest(
                apolice.getId(), LocalDate.now(), "Perda total", BigDecimal.valueOf(999999)
        );
        when(apoliceRepository.findById(apolice.getId())).thenReturn(Optional.of(apolice));

        assertThatThrownBy(() -> sinistroService.abrirSinistro(request, id))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage(MensagensConstants.VALOR_SOLICITADO_MAIOR_QUE_SEGURADO);

        verify(sinistroRepository, never()).save(any());
    }

    @Test
    void deveBuscarSinistroPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Sinistro sinistro = Sinistro.builder().id(id).statusSinistro(StatusSinistro.ABERTO).build();
        when(sinistroRepository.findById(id)).thenReturn(Optional.of(sinistro));

        SinistroResponse response = sinistroService.buscarPorId(id);

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    void deveLancarExcecaoQuandoSinistroNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(sinistroRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sinistroService.buscarPorId(id))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveBuscarSinistroPorNumeroComSucesso() {
        String numero = "1002026071700 01";
        Sinistro sinistro = Sinistro.builder().id(UUID.randomUUID()).numeroSinistro(numero).build();
        when(sinistroRepository.findByNumeroSinistro(numero)).thenReturn(Optional.of(sinistro));

        SinistroResponse response = sinistroService.buscarPorNumeroSinistro(numero);

        assertThat(response.id()).isEqualTo(sinistro.getId());
    }

    @Test
    void deveLancarExcecaoQuandoSinistroNaoEncontradoPorNumero() {
        String numero = "inexistente";
        when(sinistroRepository.findByNumeroSinistro(numero)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sinistroService.buscarPorNumeroSinistro(numero))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveListarSinistrosPorApolice() {
        UUID apoliceId = UUID.randomUUID();
        Sinistro s1 = Sinistro.builder().id(UUID.randomUUID()).apoliceId(apoliceId).build();
        Sinistro s2 = Sinistro.builder().id(UUID.randomUUID()).apoliceId(apoliceId).build();
        when(sinistroRepository.findAllByApoliceId(apoliceId)).thenReturn(List.of(s1, s2));

        List<SinistroResponse> resultado = sinistroService.listarPorApolice(apoliceId);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void deveRetornarListaVaziaQuandoApoliceSemSinistros() {
        UUID apoliceId = UUID.randomUUID();
        when(sinistroRepository.findAllByApoliceId(apoliceId)).thenReturn(List.of());

        List<SinistroResponse> resultado = sinistroService.listarPorApolice(apoliceId);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveTransicionarDeAbertoParaEmAnalise() {
        String numeroSinistro = "101202607100001";
        Sinistro sinistro = Sinistro.builder()
                .id(UUID.randomUUID())
                .numeroSinistro(numeroSinistro)
                .statusSinistro(StatusSinistro.ABERTO)
                .build();
        when(sinistroRepository.findByNumeroSinistro(numeroSinistro)).thenReturn(Optional.of(sinistro));
        when(sinistroRepository.save(any(Sinistro.class))).thenAnswer(inv -> inv.getArgument(0));

        SinistroResponse response = sinistroService.atualizarStatus(
                new AtualizarStatusSinistroRequest(numeroSinistro, StatusSinistro.EM_ANALISE, "Iniciando análise")
                , UUID.randomUUID()
        );

        assertThat(response.statusSinistro()).isEqualTo(StatusSinistro.EM_ANALISE);
    }

    @Test
    void deveTransicionarDeEmAnaliseParaAprovado() {
        String numeroSinistro = "101202607100001";
        Sinistro sinistro = Sinistro.builder()
                .id(UUID.randomUUID())
                .numeroSinistro(numeroSinistro)
                .statusSinistro(StatusSinistro.EM_ANALISE)
                .build();
        when(sinistroRepository.findByNumeroSinistro(numeroSinistro)).thenReturn(Optional.of(sinistro));
        when(sinistroRepository.save(any(Sinistro.class))).thenAnswer(inv -> inv.getArgument(0));

        SinistroResponse response = sinistroService.atualizarStatus(
                new AtualizarStatusSinistroRequest(numeroSinistro, StatusSinistro.APROVADO, "Aprovado após vistoria")
                , UUID.randomUUID()
        );

        assertThat(response.statusSinistro()).isEqualTo(StatusSinistro.APROVADO);
    }

    @Test
    void deveLancarExcecaoAoPularEtapaDeAbertoParaAprovado() {
        String numeroSinistro = "101202607100001";
        UUID id = UUID.randomUUID();
        Sinistro sinistro = Sinistro.builder()
                .id(id)
                .numeroSinistro(numeroSinistro)
                .statusSinistro(StatusSinistro.ABERTO)
                .build();
        when(sinistroRepository.findByNumeroSinistro(numeroSinistro)).thenReturn(Optional.of(sinistro));

        AtualizarStatusSinistroRequest request = new AtualizarStatusSinistroRequest(numeroSinistro, StatusSinistro.APROVADO, null);
        assertThatThrownBy(() -> sinistroService.atualizarStatus(request, id)).isInstanceOf(EstadoInvalidoException.class);

        verify(sinistroRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoTentarAlterarStatusDeSinistroJaPago() {
        String numeroSinistro = "101202607100001";
        UUID id = UUID.randomUUID();
        Sinistro sinistro = Sinistro.builder()
                .id(id)
                .numeroSinistro(numeroSinistro)
                .statusSinistro(StatusSinistro.PAGO)
                .build();
        when(sinistroRepository.findByNumeroSinistro(numeroSinistro)).thenReturn(Optional.of(sinistro));

        AtualizarStatusSinistroRequest request = new AtualizarStatusSinistroRequest(numeroSinistro, StatusSinistro.EM_ANALISE, null);
        assertThatThrownBy(() -> sinistroService.atualizarStatus(request, id)).isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void deveLancarExcecaoAoTentarVoltarDeNegadoParaEmAnalise() {
        String numeroSinistro = "101202607100001";
        UUID id = UUID.randomUUID();
        Sinistro sinistro = Sinistro.builder()
                .id(id)
                .numeroSinistro(numeroSinistro)
                .statusSinistro(StatusSinistro.NEGADO)
                .build();
        when(sinistroRepository.findByNumeroSinistro(numeroSinistro)).thenReturn(Optional.of(sinistro));

        AtualizarStatusSinistroRequest request = new AtualizarStatusSinistroRequest(numeroSinistro, StatusSinistro.EM_ANALISE, null);
        assertThatThrownBy(() -> sinistroService.atualizarStatus(request, id)).isInstanceOf(EstadoInvalidoException.class);
    }
}

