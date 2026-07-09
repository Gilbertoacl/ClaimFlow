package dev.gilbertoacl.claimflow_api.cliente.service;

import dev.gilbertoacl.claimflow_api.cliente.dto.ClienteRequest;
import dev.gilbertoacl.claimflow_api.cliente.dto.ClienteResponse;
import dev.gilbertoacl.claimflow_api.cliente.dto.EnderecoRequest;
import dev.gilbertoacl.claimflow_api.cliente.entity.Cliente;
import dev.gilbertoacl.claimflow_api.cliente.entity.Endereco;
import dev.gilbertoacl.claimflow_api.cliente.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequest requestValido() {
        EnderecoRequest enderecoRequest = new EnderecoRequest(
                "Rua Sao José",
                "908",
                "Apto 101",
                "São Paulo",
                "SP",
                "11111-111"
        );
        return new ClienteRequest(
                "João Silva",
                "12345678900",
                "mail@exemple.com.br",
                null,
                enderecoRequest,
                LocalDate.of(1990, 8, 15)
        );
    }

    private Endereco enderecoValido() {
        return new Endereco(
                "Rua Sao José",
                "908",
                "Apto 101",
                "São Paulo",
                "SP",
                "11111-111"
        );
    }

    @Test
    void deveCriarClienteComSucesso() {
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(clienteRepository.existsByEmail(request.email())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
           Cliente cliente = invocation.getArgument(0);
           cliente.setId(UUID.randomUUID());
           cliente.setDataCadastro(LocalDateTime.now());
           return cliente;
        });

        ClienteResponse response = clienteService.criar(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.nome()).isEqualTo("João Silva");
        assertThat(response.cpf()).isEqualTo("12345678900");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void deveLancarExcecaoAoTentarCriarClienteComCpfExistente() {
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByCpf(request.cpf())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF ja existente.");
    }

    @Test
    void deveLancarExcecaoAoTentarCriarClienteComEmailExistente() {
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email ja existente.");
    }

    @Test
    void deveBuscarClientePorIdComSucesso() {
        UUID id = UUID.randomUUID();
        Cliente cliente =  Cliente.builder()
                .id(id)
                .nome("João Silva")
                .cpf("12345678900")
                .email("mail@exemple.com.br")
                .telefone(null)
                .endereco(enderecoValido())
                .dataNascimento(LocalDate.of(1990, 8, 15))
                .dataCadastro(LocalDateTime.now())
                .build();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.buscarPorId(id);

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    void deveLancarExcecaoAoBuscarClienteInexistente() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cliente não encontrado.");
    }
}
