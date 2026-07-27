package dev.gilbertoacl.claimflow_api.historicosinistro.service;

import dev.gilbertoacl.claimflow_api.historicosinistro.dto.HistoricoSinistroResponse;
import dev.gilbertoacl.claimflow_api.historicosinistro.entity.HistoricoSinistro;
import dev.gilbertoacl.claimflow_api.historicosinistro.repository.HistoricoSinistroRepository;
import dev.gilbertoacl.claimflow_api.sinistro.entity.Sinistro;
import dev.gilbertoacl.claimflow_api.sinistro.entity.StatusSinistro;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoSinistroServiceTest {

    @Mock
    private HistoricoSinistroRepository historicoSinistroRepository;

    @InjectMocks
    private HistoricoSinistroService historicoSinistroService;

    private Sinistro sinistroValido() {
        return Sinistro.builder()
                .id(UUID.randomUUID())
                .numeroSinistro("101202607100001")
                .statusSinistro(StatusSinistro.ABERTO)
                .build();
    }

    @Test
    void deveRegistrarHistoricoComSucesso() {
        Sinistro sinistro = sinistroValido();
        UUID responsavelId = UUID.randomUUID();
        String observacao = "Sinistro aberto";

        when(historicoSinistroRepository.save(any(HistoricoSinistro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        historicoSinistroService.registrar(sinistro, responsavelId, observacao);

        ArgumentCaptor<HistoricoSinistro> captor = ArgumentCaptor.forClass(HistoricoSinistro.class);
        verify(historicoSinistroRepository).save(captor.capture());

        HistoricoSinistro salvo = captor.getValue();
        assertThat(salvo.getSinistroId()).isEqualTo(sinistro.getId());
        assertThat(salvo.getNumeroSinistro()).isEqualTo(sinistro.getNumeroSinistro());
        assertThat(salvo.getStatusSinistro()).isEqualTo(sinistro.getStatusSinistro());
        assertThat(salvo.getResponsavelId()).isEqualTo(responsavelId);
        assertThat(salvo.getObservacao()).isEqualTo(observacao);
    }

    @Test
    void deveRegistrarHistoricoMesmoComObservacaoNula() {
        Sinistro sinistro = sinistroValido();
        UUID responsavelId = UUID.randomUUID();

        when(historicoSinistroRepository.save(any(HistoricoSinistro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        historicoSinistroService.registrar(sinistro, responsavelId, null);

        ArgumentCaptor<HistoricoSinistro> captor = ArgumentCaptor.forClass(HistoricoSinistro.class);
        verify(historicoSinistroRepository).save(captor.capture());
        assertThat(captor.getValue().getObservacao()).isNull();
    }

    @Test
    void deveCapturarOStatusAtualDoSinistroNoMomentoDoRegistro() {
        Sinistro sinistro = sinistroValido();
        sinistro.setStatusSinistro(StatusSinistro.EM_ANALISE);
        UUID responsavelId = UUID.randomUUID();

        when(historicoSinistroRepository.save(any(HistoricoSinistro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        historicoSinistroService.registrar(sinistro, responsavelId, "Iniciando análise");

        ArgumentCaptor<HistoricoSinistro> captor = ArgumentCaptor.forClass(HistoricoSinistro.class);
        verify(historicoSinistroRepository).save(captor.capture());
        assertThat(captor.getValue().getStatusSinistro()).isEqualTo(StatusSinistro.EM_ANALISE);
    }

    @Test
    void deveListarHistoricoPorNumeroSinistro() {
        String numero = "101202607100001";
        HistoricoSinistro h1 = HistoricoSinistro.builder()
                .id(UUID.randomUUID()).numeroSinistro(numero).statusSinistro(StatusSinistro.ABERTO).build();
        HistoricoSinistro h2 = HistoricoSinistro.builder()
                .id(UUID.randomUUID()).numeroSinistro(numero).statusSinistro(StatusSinistro.EM_ANALISE).build();

        when(historicoSinistroRepository.findAllByNumeroSinistroOrderByDataAlteracaoAsc(numero))
                .thenReturn(List.of(h1, h2));

        List<HistoricoSinistroResponse> resultado = historicoSinistroService.listarPorNumeroSinistro(numero);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).statusSinistro()).isEqualTo(StatusSinistro.ABERTO);
        assertThat(resultado.get(1).statusSinistro()).isEqualTo(StatusSinistro.EM_ANALISE);
    }

    @Test
    void deveRetornarListaVaziaQuandoSinistroSemHistorico() {
        String numero = "999999999999999";
        when(historicoSinistroRepository.findAllByNumeroSinistroOrderByDataAlteracaoAsc(numero))
                .thenReturn(List.of());

        List<HistoricoSinistroResponse> resultado = historicoSinistroService.listarPorNumeroSinistro(numero);

        assertThat(resultado).isEmpty();
    }
}