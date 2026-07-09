package dev.gilbertoacl.claimflow_api.cliente.controller;

import dev.gilbertoacl.claimflow_api.cliente.dto.ClienteRequest;
import dev.gilbertoacl.claimflow_api.cliente.dto.ClienteResponse;
import dev.gilbertoacl.claimflow_api.cliente.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para gerenciar operações relacionadas a clientes.
 */
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Cria um novo cliente.
     *
     * @param request O objeto ClienteRequest contendo os dados do cliente a ser criado.
     * @return Um ResponseEntity contendo o objeto ClienteResponse representando o cliente criado.
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse clienteResponse = clienteService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponse);
    }

    /**
     * Busca um cliente por ID.
     *
     * @param id O ID do cliente a ser buscado.
     * @return Um ResponseEntity contendo o objeto ClienteResponse representando o cliente encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable("id") UUID id) {
        ClienteResponse clienteResponse = clienteService.buscarPorId(id);
        return ResponseEntity.ok(clienteResponse);
    }
}
