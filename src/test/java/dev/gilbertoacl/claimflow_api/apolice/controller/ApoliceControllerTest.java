package dev.gilbertoacl.claimflow_api.apolice.controller;

import dev.gilbertoacl.claimflow_api.apolice.dto.ApoliceRequest;
import dev.gilbertoacl.claimflow_api.apolice.dto.ApoliceResponse;
import dev.gilbertoacl.claimflow_api.apolice.enums.StatusApolice;
import dev.gilbertoacl.claimflow_api.apolice.enums.TipoApolice;
import dev.gilbertoacl.claimflow_api.apolice.service.ApoliceService;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RecursoNaoEncontradoException;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RegraDeNegocioException;
import dev.gilbertoacl.claimflow_api.shared.util.MensagensConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ApoliceController.class)
class ApoliceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApoliceService apoliceService;

    private ApoliceRequest requestValido() {
        return new ApoliceRequest(
                UUID.randomUUID(), TipoApolice.AUTOMOVEL, "1234567890",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(100),
                OffsetDateTime.now(), OffsetDateTime.now().plusYears(1)
        );
    }

    private ApoliceResponse responseValido(UUID id, UUID clienteId) {
        return new ApoliceResponse(
                id, clienteId, TipoApolice.AUTOMOVEL, "1234567890",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(100),
                OffsetDateTime.now(), OffsetDateTime.now().plusYears(1),
                StatusApolice.ATIVA, LocalDateTime.now()
        );
    }

    // ---------- POST /apolices ----------

    @Test
    void deveCriarApoliceERetornar201() throws Exception {
        UUID id = UUID.randomUUID();
        ApoliceRequest request = requestValido();
        when(apoliceService.criar(any(ApoliceRequest.class)))
                .thenReturn(responseValido(id, request.clienteId()));

        mockMvc.perform(post("/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.statusApolice").value("ATIVA"));
    }

    @Test
    void deveRetornar404QuandoClienteNaoExiste() throws Exception {
        when(apoliceService.criar(any(ApoliceRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException(MensagensConstants.CLIENTE_NAO_ENCONTRADO));

        mockMvc.perform(post("/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409QuandoNumeroApoliceDuplicado() throws Exception {
        when(apoliceService.criar(any(ApoliceRequest.class)))
                .thenThrow(new RegraDeNegocioException(MensagensConstants.APOLICE_JA_EXISTE));

        mockMvc.perform(post("/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoVigenciaInvalida() throws Exception {
        when(apoliceService.criar(any(ApoliceRequest.class)))
                .thenThrow(new RegraDeNegocioException(MensagensConstants.FIM_VIGENCIA_DEVE_SER_POSTERIOR_A_INICIO));

        mockMvc.perform(post("/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoClienteIdForNulo() throws Exception {
        ApoliceRequest request = new ApoliceRequest(
                null, TipoApolice.AUTOMOVEL, "1234567890",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(100),
                OffsetDateTime.now(), OffsetDateTime.now().plusYears(1)
        );

        mockMvc.perform(post("/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("clienteId")));
    }

    @Test
    void deveRetornar400QuandoValorSeguradoForNegativo() throws Exception {
        ApoliceRequest request = new ApoliceRequest(
                UUID.randomUUID(), TipoApolice.AUTOMOVEL, "1234567890",
                BigDecimal.valueOf(-100), BigDecimal.valueOf(100),
                OffsetDateTime.now(), OffsetDateTime.now().plusYears(1)
        );

        mockMvc.perform(post("/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("valorSegurado")));
    }

    // ---------- GET /apolices/{id} ----------

    @Test
    void deveBuscarApoliceERetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        when(apoliceService.buscarPorId(id)).thenReturn(responseValido(id, UUID.randomUUID()));

        mockMvc.perform(get("/apolices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void deveRetornar404QuandoApoliceNaoEncontrada() throws Exception {
        UUID id = UUID.randomUUID();
        when(apoliceService.buscarPorId(id))
                .thenThrow(new RecursoNaoEncontradoException(MensagensConstants.APOLICE_NAO_ENCONTRADA));

        mockMvc.perform(get("/apolices/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /apolices/{id}/listar ----------

    @Test
    void deveListarApolicesDoClienteERetornar200() throws Exception {
        UUID clienteId = UUID.randomUUID();
        List<ApoliceResponse> apolices = List.of(
                responseValido(UUID.randomUUID(), clienteId),
                responseValido(UUID.randomUUID(), clienteId)
        );
        when(apoliceService.listarApolicesPorCliente(clienteId)).thenReturn(apolices);

        mockMvc.perform(get("/apolices/{id}/listar", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteSemApolices() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(apoliceService.listarApolicesPorCliente(clienteId)).thenReturn(List.of());

        mockMvc.perform(get("/apolices/{id}/listar", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    @Test
    void deveCancelarApoliceERetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        ApoliceResponse response = responseValido(id, UUID.randomUUID());
        ApoliceResponse responseCancelada = new ApoliceResponse(
                response.id(), response.clienteId(), response.tipoApolice(), response.numeroApolice(),
                response.valorSegurado(), response.premioMensal(), response.inicioVigencia(),
                response.fimVigencia(), StatusApolice.CANCELADA, response.dataCriacao()
        );
        when(apoliceService.cancelarApolice(id)).thenReturn(responseCancelada);

        mockMvc.perform(patch("/apolices/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusApolice").value("CANCELADA"));
    }

    @Test
    void deveRetornar404AoCancelarApoliceInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        when(apoliceService.cancelarApolice(id))
                .thenThrow(new RecursoNaoEncontradoException(MensagensConstants.APOLICE_NAO_ENCONTRADA));

        mockMvc.perform(patch("/apolices/{id}/cancelar", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409AoCancelarApoliceJaCancelada() throws Exception {
        UUID id = UUID.randomUUID();
        when(apoliceService.cancelarApolice(id))
                .thenThrow(new RegraDeNegocioException(MensagensConstants.APOLICE_JA_CANCELADA));

        mockMvc.perform(patch("/apolices/{id}/cancelar", id))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409AoCancelarApoliceVencida() throws Exception {
        UUID id = UUID.randomUUID();
        when(apoliceService.cancelarApolice(id))
                .thenThrow(new RegraDeNegocioException(MensagensConstants.APOLICE_VENCIDA_NAO_PODE_CANCELAR));

        mockMvc.perform(patch("/apolices/{id}/cancelar", id))
                .andExpect(status().isConflict());
    }
}