package dev.gilbertoacl.claimflow_api.cliente.service;

import dev.gilbertoacl.claimflow_api.cliente.dto.ClienteRequest;
import dev.gilbertoacl.claimflow_api.cliente.dto.ClienteResponse;
import dev.gilbertoacl.claimflow_api.cliente.dto.EnderecoRequest;
import dev.gilbertoacl.claimflow_api.cliente.dto.EnderecoResponse;
import dev.gilbertoacl.claimflow_api.cliente.entity.Cliente;
import dev.gilbertoacl.claimflow_api.cliente.entity.Endereco;
import dev.gilbertoacl.claimflow_api.cliente.repository.ClienteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço responsável pelas operações relacionadas a clientes.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    /**
     * Cria um novo cliente.
     *
     * @param request O objeto ClienteRequest contendo os dados do cliente a ser criado.
     * @return Um objeto ClienteResponse representando o cliente criado.
     * @throws IllegalArgumentException Se já existir um cliente com o mesmo CPF ou email.
     */
    public ClienteResponse criar(ClienteRequest request) {
        if (clienteRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF ja existente.");
        }

        if (clienteRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email ja existente.");
        }

        Cliente cliente = Cliente.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .email(request.email())
                .telefone(request.telefone())
                .endereco(toEndereco(request.endereco()))
                .dataNascimento(request.dataNascimento())
                .build();

        return toResponse(clienteRepository.save(cliente));
    }

    /**
     * Busca um cliente por ID.
     *
     * @param id O ID do cliente a ser buscado.
     * @return Um objeto ClienteResponse representando o cliente encontrado.
     * @throws IllegalArgumentException Se o cliente não for encontrado.
     */
    public ClienteResponse buscarPorId(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        return toResponse(cliente);
    }

    /**
     * Atualiza os dados de um cliente existente.
     *
     * @param id      O ID do cliente a ser atualizado.
     * @param request O objeto ClienteRequest contendo os novos dados do cliente.
     * @return Um objeto ClienteResponse representando o cliente atualizado.
     * @throws IllegalArgumentException Se o cliente não for encontrado ou se já existir outro cliente com o mesmo CPF ou email.
     */
    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getTelefone(),
                toEnderecoResponse(cliente.getEndereco()),
                cliente.getDataNascimento(),
                cliente.getDataCadastro()
        );
    }

    /**
     * Converte um objeto Endereco para EnderecoResponse.
     *
     * @param endereco O objeto Endereco a ser convertido.
     * @return Um objeto EnderecoResponse representando o endereço.
     */
    private EnderecoResponse toEnderecoResponse(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getEstado(),
                endereco.getCep()
        );
    }

    /**
     * Converte um objeto EnderecoRequest para Endereco.
     *
     * @param enderecoRequest O objeto EnderecoRequest a ser convertido.
     * @return Um objeto Endereco representando o endereço.
     */
    private Endereco toEndereco(@Valid EnderecoRequest enderecoRequest) {
        return Endereco.builder()
                .logradouro(enderecoRequest.logradouro())
                .numero(enderecoRequest.numero())
                .complemento(enderecoRequest.complemento())
                .bairro(enderecoRequest.bairro())
                .estado(enderecoRequest.estado())
                .cep(enderecoRequest.cep())
                .build();
    }
}
