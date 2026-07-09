package dev.gilbertoacl.claimflow_api.cliente.controller;

import dev.gilbertoacl.claimflow_api.cliente.dto.ClienteRequest;
import dev.gilbertoacl.claimflow_api.cliente.dto.ClienteResponse;
import dev.gilbertoacl.claimflow_api.cliente.dto.EnderecoRequest;
import dev.gilbertoacl.claimflow_api.cliente.dto.EnderecoResponse;
import dev.gilbertoacl.claimflow_api.cliente.service.ClienteService;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RecursoNaoEncontradoException;
import dev.gilbertoacl.claimflow_api.shared.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    private ClienteRequest requestValido() {
        EnderecoRequest endereco = new EnderecoRequest(
                "Rua das Flores", "123", null, "Centro", "RJ", "20000-000"
        );
        return new ClienteRequest(
                "João Silva", "52998224725", "joao@email.com",
                "21999999999", endereco, LocalDate.of(1990, 5, 10)
        );
    }

    private ClienteResponse responseValido(UUID id) {
        EnderecoResponse endereco = new EnderecoResponse(
                "Rua das Flores", "123", null, "Centro", "RJ", "20000-000"
        );
        return new ClienteResponse(
                id, "João Silva", "52998224725", "joao@email.com",
                "21999999999", endereco, LocalDate.of(1990, 5, 10), LocalDateTime.now()
        );
    }

    @Test
    void deveCriarClienteERetornar201() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.criar(any(ClienteRequest.class))).thenReturn(responseValido(id));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.cpf").value("52998224725"));
    }

    @Test
    void deveRetornar409QuandoCpfDuplicado() throws Exception {
        when(clienteService.criar(any(ClienteRequest.class)))
                .thenThrow(new RegraDeNegocioException("CPF já cadastrado"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("CPF já cadastrado"));
    }

    @Test
    void deveRetornar409QuandoEmailDuplicado() throws Exception {
        when(clienteService.criar(any(ClienteRequest.class)))
                .thenThrow(new RegraDeNegocioException("Email já cadastrado"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Email já cadastrado"));
    }

    @Test
    void deveRetornar400QuandoNomeEstiverEmBranco() throws Exception {
        ClienteRequest request = new ClienteRequest(
                "", "12345678900", "joao@email.com", "21999999999",
                requestValido().endereco(), LocalDate.of(1990, 5, 10)
        );

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("nome")));
    }

    @Test
    void deveRetornar400QuandoCpfForInvalido() throws Exception {
        ClienteRequest request = new ClienteRequest(
                "João Silva", "111", "joao@email.com", "21999999999",
                requestValido().endereco(), LocalDate.of(1990, 5, 10)
        );

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("cpf")));
    }

    @Test
    void deveRetornar400QuandoEmailForInvalido() throws Exception {
        ClienteRequest request = new ClienteRequest(
                "João Silva", "12345678900", "nao-eh-email", "21999999999",
                requestValido().endereco(), LocalDate.of(1990, 5, 10)
        );

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("email")));
    }

    @Test
    void deveRetornar400QuandoDataNascimentoForFutura() throws Exception {
        ClienteRequest request = new ClienteRequest(
                "João Silva", "12345678900", "joao@email.com", "21999999999",
                requestValido().endereco(), LocalDate.now().plusDays(1)
        );

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposComErro[*].campo", hasItem("dataNascimento")));
    }

    @Test
    void deveRetornar400QuandoEnderecoForInvalido() throws Exception {
        EnderecoRequest enderecoInvalido = new EnderecoRequest(
                "", "123", null, "Centro", "RJ", "abc"
        );
        ClienteRequest request = new ClienteRequest(
                "João Silva", "12345678900", "joao@email.com", "21999999999",
                enderecoInvalido, LocalDate.of(1990, 5, 10)
        );

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBuscarClientePorIdERetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.buscarPorId(id)).thenReturn(responseValido(id));

        mockMvc.perform(get("/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    void deveRetornar404QuandoClienteNaoExiste() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.buscarPorId(id))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado"));

        mockMvc.perform(get("/clientes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente não encontrado"));
    }

    @Test
    void deveRetornar400QuandoIdNaoForUuidValido() throws Exception {
        mockMvc.perform(get("/clientes/{id}", "nao-eh-um-uuid"))
                .andExpect(status().isBadRequest());
    }
}