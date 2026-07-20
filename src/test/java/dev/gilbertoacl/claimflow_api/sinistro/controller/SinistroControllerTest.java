package dev.gilbertoacl.claimflow_api.sinistro.controller;

import dev.gilbertoacl.claimflow_api.shared.exceptions.EstadoInvalidoException;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RecursoNaoEncontradoException;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RegraDeNegocioException;
import dev.gilbertoacl.claimflow_api.shared.util.MensagensConstants;
import dev.gilbertoacl.claimflow_api.sinistro.dto.AtualizarStatusSinistroRequest;
import dev.gilbertoacl.claimflow_api.sinistro.dto.SinistroRequest;
import dev.gilbertoacl.claimflow_api.sinistro.dto.SinistroResponse;
import dev.gilbertoacl.claimflow_api.sinistro.entity.StatusSinistro;
import dev.gilbertoacl.claimflow_api.sinistro.service.SinistroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SinistroController.class)
class SinistroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SinistroService sinistroService;

    private SinistroRequest requestValido(UUID apoliceId) {
        return new SinistroRequest(apoliceId, LocalDate.now(), "Colisão traseira", BigDecimal.valueOf(2000));
    }

    private SinistroResponse responseValido(UUID id, UUID apoliceId, StatusSinistro status) {
        return new SinistroResponse(
                id, apoliceId, "101202607100001", LocalDate.now(), "Colisão traseira",
                BigDecimal.valueOf(2000), null, status, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void deveAbrirSinistroERetornar201() throws Exception {
        UUID apoliceId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(sinistroService.abrirSinistro(any(SinistroRequest.class)))
                .thenReturn(responseValido(id, apoliceId, StatusSinistro.ABERTO));

        mockMvc.perform(post("/sinistros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido(apoliceId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.statusSinistro").value("ABERTO"));
    }

    @Test
    void deveRetornar404QuandoApoliceNaoEncontradaAoAbrir() throws Exception {
        when(sinistroService.abrirSinistro(any(SinistroRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException(MensagensConstants.APOLICE_NAO_ENCONTRADA));

        mockMvc.perform(post("/sinistros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido(UUID.randomUUID()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409QuandoApoliceNaoEstaAtiva() throws Exception {
        when(sinistroService.abrirSinistro(any(SinistroRequest.class)))
                .thenThrow(new RegraDeNegocioException(MensagensConstants.APOLICE_INATIVA_NAO_PODE_ABRIR_SINISTRO));

        mockMvc.perform(post("/sinistros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido(UUID.randomUUID()))))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar409QuandoValorSolicitadoMaiorQueSegurado() throws Exception {
        when(sinistroService.abrirSinistro(any(SinistroRequest.class)))
                .thenThrow(new RegraDeNegocioException(MensagensConstants.VALOR_SOLICITADO_MAIOR_QUE_SEGURADO));

        mockMvc.perform(post("/sinistros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido(UUID.randomUUID()))))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoApoliceIdForNulo() throws Exception {
        SinistroRequest request = new SinistroRequest(null, LocalDate.now(), "Sem apólice", BigDecimal.valueOf(500));

        mockMvc.perform(post("/sinistros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("apoliceId")));
    }

    @Test
    void deveRetornar400QuandoValorSolicitadoForNegativo() throws Exception {
        SinistroRequest request = new SinistroRequest(
                UUID.randomUUID(), LocalDate.now(), "Valor inválido", BigDecimal.valueOf(-100)
        );

        mockMvc.perform(post("/sinistros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("valorSolicitado")));
    }

    @Test
    void deveRetornar400QuandoDataOcorridoForFutura() throws Exception {
        SinistroRequest request = new SinistroRequest(
                UUID.randomUUID(), LocalDate.now().plusDays(1), "Data futura", BigDecimal.valueOf(500)
        );

        mockMvc.perform(post("/sinistros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("dataOcorrido")));
    }

    // ---------- GET /sinistros/{id} ----------

    @Test
    void deveBuscarSinistroPorIdERetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID apoliceId = UUID.randomUUID();
        when(sinistroService.buscarPorId(id)).thenReturn(responseValido(id, apoliceId, StatusSinistro.ABERTO));

        mockMvc.perform(get("/sinistros/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void deveRetornar404QuandoSinistroNaoEncontradoPorId() throws Exception {
        UUID id = UUID.randomUUID();
        when(sinistroService.buscarPorId(id))
                .thenThrow(new RecursoNaoEncontradoException(MensagensConstants.SINISTRO_NAO_ENCONTRADO));

        mockMvc.perform(get("/sinistros/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarSinistroPorNumeroERetornar200() throws Exception {
        String numero = "101202607100001";
        UUID id = UUID.randomUUID();
        SinistroResponse response = responseValido(id, UUID.randomUUID(), StatusSinistro.ABERTO);
        when(sinistroService.buscarPorNumeroSinistro(numero)).thenReturn(response);

        mockMvc.perform(get("/sinistros/numero/{numeroSinistro}", numero))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroSinistro").value(numero));
    }

    @Test
    void deveRetornar404QuandoSinistroNaoEncontradoPorNumero() throws Exception {
        String numero = "inexistente";
        when(sinistroService.buscarPorNumeroSinistro(numero))
                .thenThrow(new RecursoNaoEncontradoException(MensagensConstants.SINISTRO_NAO_ENCONTRADO));

        mockMvc.perform(get("/sinistros/numero/{numeroSinistro}", numero))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarSinistrosDaApoliceERetornar200() throws Exception {
        UUID apoliceId = UUID.randomUUID();
        List<SinistroResponse> sinistros = List.of(
                responseValido(UUID.randomUUID(), apoliceId, StatusSinistro.ABERTO),
                responseValido(UUID.randomUUID(), apoliceId, StatusSinistro.EM_ANALISE)
        );
        when(sinistroService.listarPorApolice(apoliceId)).thenReturn(sinistros);

        mockMvc.perform(get("/sinistros/apolice/{apoliceId}/listar", apoliceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveRetornarListaVaziaQuandoApoliceSemSinistros() throws Exception {
        UUID apoliceId = UUID.randomUUID();
        when(sinistroService.listarPorApolice(apoliceId)).thenReturn(List.of());

        mockMvc.perform(get("/sinistros/apolice/{apoliceId}/listar", apoliceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveAtualizarStatusERetornar200() throws Exception {
        String numero = "101202607100001";
        AtualizarStatusSinistroRequest request =
                new AtualizarStatusSinistroRequest(numero, StatusSinistro.EM_ANALISE, "Iniciando análise");
        SinistroResponse response = responseValido(UUID.randomUUID(), UUID.randomUUID(), StatusSinistro.EM_ANALISE);
        when(sinistroService.atualizarStatus(any(AtualizarStatusSinistroRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/sinistros/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusSinistro").value("EM_ANALISE"));
    }

    @Test
    void deveRetornar404QuandoSinistroNaoEncontradoAoAtualizarStatus() throws Exception {
        AtualizarStatusSinistroRequest request =
                new AtualizarStatusSinistroRequest("inexistente", StatusSinistro.EM_ANALISE, null);
        when(sinistroService.atualizarStatus(any(AtualizarStatusSinistroRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException(MensagensConstants.SINISTRO_NAO_ENCONTRADO));

        mockMvc.perform(patch("/sinistros/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409QuandoTransicaoDeStatusInvalida() throws Exception {
        String numero = "101202607100001";
        AtualizarStatusSinistroRequest request =
                new AtualizarStatusSinistroRequest(numero, StatusSinistro.APROVADO, null);
        when(sinistroService.atualizarStatus(any(AtualizarStatusSinistroRequest.class)))
                .thenThrow(new EstadoInvalidoException("Transição de ABERTO para APROVADO não é permitida"));

        mockMvc.perform(patch("/sinistros/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoNumeroSinistroForNulo() throws Exception {
        AtualizarStatusSinistroRequest request =
                new AtualizarStatusSinistroRequest(null, StatusSinistro.EM_ANALISE, null);

        mockMvc.perform(patch("/sinistros/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("numeroSinistro")));
    }
}