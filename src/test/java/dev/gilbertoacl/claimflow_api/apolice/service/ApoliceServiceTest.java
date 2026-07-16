package dev.gilbertoacl.claimflow_api.apolice.service;

import dev.gilbertoacl.claimflow_api.apolice.dto.ApoliceRequest;
import dev.gilbertoacl.claimflow_api.apolice.dto.ApoliceResponse;
import dev.gilbertoacl.claimflow_api.apolice.entity.Apolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.StatusApolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.TipoApolice;
import dev.gilbertoacl.claimflow_api.apolice.repository.ApoliceRepository;
import dev.gilbertoacl.claimflow_api.cliente.repository.ClienteRepository;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RecursoNaoEncontradoException;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RegraDeNegocioException;
import dev.gilbertoacl.claimflow_api.shared.util.MensagensConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApoliceServiceTest {

    @Mock
    private ApoliceRepository apoliceRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ApoliceService apoliceService;

    private ApoliceRequest requestValido(){
        return new ApoliceRequest(
                UUID.randomUUID(),
                TipoApolice.AUTOMOVEL,
                "1234567890",
                BigDecimal.valueOf(10000.00),
                BigDecimal.valueOf(100.00),
                OffsetDateTime.now(),
                OffsetDateTime.now().plusYears(1)
        );
    }

    private ApoliceRequest requestInvalido(){
        return new ApoliceRequest(
                UUID.randomUUID(),
                TipoApolice.AUTOMOVEL,
                "1234567890",
                BigDecimal.valueOf(10000.00),
                BigDecimal.valueOf(100.00),
                OffsetDateTime.now(),
                OffsetDateTime.now().minusYears(1)
        );
    }

    @Test
    void deveCriarApoliceComSucesso() {
        ApoliceRequest request = requestValido();

        when(clienteRepository.existsById(request.clienteId())).thenReturn(true);
        when(apoliceRepository.findByNumeroApolice(request.numeroApolice())).thenReturn(Optional.empty());
        when(apoliceRepository.save(any(Apolice.class))).thenAnswer(invocation -> {
            Apolice apolice = invocation.getArgument(0);
            apolice.setId(UUID.randomUUID());
            apolice.setDataCriacao(LocalDateTime.now());
            return apolice;
        });

        ApoliceResponse response = apoliceService.criar(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.statusApolice()).isEqualTo(StatusApolice.ATIVA);
        verify(apoliceRepository).save(any(Apolice.class));
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExistir() {
        ApoliceRequest req = requestValido();
        when(clienteRepository.existsById(req.clienteId())).thenReturn(false);

        assertThatThrownBy(() -> apoliceService.criar(req))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(MensagensConstants.CLIENTE_NAO_ENCONTRADO);
    }

    @Test
    void deveLancarExcecaoQuandoNumeroApolceJaExistir() {
        ApoliceRequest req = requestValido();
        when(clienteRepository.existsById(req.clienteId())).thenReturn(true);
        when(apoliceRepository.findByNumeroApolice(req.numeroApolice())).thenReturn(
                Optional.of(Apolice.builder().build())
        );

        assertThatThrownBy(() -> apoliceService.criar(req))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage(MensagensConstants.APOLICE_JA_EXISTE);
    }

    @Test
    void deveLancarExcecaoQuandoFimVigenciaForAnteriorAoInicio() {
        ApoliceRequest req = requestInvalido();
        when(clienteRepository.existsById(req.clienteId())).thenReturn(true);
        when(apoliceRepository.findByNumeroApolice(req.numeroApolice())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apoliceService.criar(req))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage(MensagensConstants.FIM_VIGENCIA_DEVE_SER_POSTERIOR_A_INICIO);
    }

    @Test
    void deveLancarExcecaoQuandoFimVigenciaForIgualAoInicio() {
        OffsetDateTime dataVigenciaIgual = OffsetDateTime.now();
        ApoliceRequest req = new ApoliceRequest(
                UUID.randomUUID(),
                TipoApolice.AUTOMOVEL,
                "1234567890",
                BigDecimal.valueOf(10000.00),
                BigDecimal.valueOf(100.00),
                dataVigenciaIgual,
                dataVigenciaIgual
        );

        when(clienteRepository.existsById(req.clienteId())).thenReturn(true);
        when(apoliceRepository.findByNumeroApolice(req.numeroApolice())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apoliceService.criar(req))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage(MensagensConstants.FIM_VIGENCIA_DEVE_SER_POSTERIOR_A_INICIO);
    }

    @Test
    void deveBuscarApolicePorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Apolice apolice = Apolice.builder().id(id).status(StatusApolice.ATIVA).build();
        when(apoliceRepository.findById(id)).thenReturn(Optional.of(apolice));

        ApoliceResponse response = apoliceService.buscarPorId(id);

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    void deveLancarExcecaoQuandoApoliceNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(apoliceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apoliceService.buscarPorId(id))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(MensagensConstants.APOLICE_NAO_ENCONTRADA);
    }

    @Test
    void deveListarApolicesPorCliente() {
        UUID clienteId = UUID.randomUUID();
        Apolice apolice1 = Apolice.builder().id(UUID.randomUUID()).clienteId(clienteId).status(StatusApolice.ATIVA).build();
        Apolice apolice2 = Apolice.builder().id(UUID.randomUUID()).clienteId(clienteId).status(StatusApolice.ATIVA).build();
        when(apoliceRepository.findAllByClienteId(clienteId)).thenReturn(List.of(apolice1, apolice2));

        List<ApoliceResponse> resultado = apoliceService.listarApolicesPorCliente(clienteId);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteNaoTemApolices() {
        UUID clienteId = UUID.randomUUID();
        when(apoliceRepository.findAllByClienteId(clienteId)).thenReturn(List.of());

        List<ApoliceResponse> resultado = apoliceService.listarApolicesPorCliente(clienteId);

        assertThat(resultado).isEmpty();
    }

}
