package dev.gilbertoacl.claimflow_api.apolice.controller;

import dev.gilbertoacl.claimflow_api.apolice.dto.ApoliceRequest;
import dev.gilbertoacl.claimflow_api.apolice.dto.ApoliceResponse;
import dev.gilbertoacl.claimflow_api.apolice.service.ApoliceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/apolices")
@RequiredArgsConstructor
public class ApoliceController {
    private final ApoliceService apoliceService;

    /**
     * Busca a lista de apolices de um cliente
     *
     * @param id o ID do cliente para buscar as apolices vinculadas
     * @return Um ResponseEntity contendo a lista de AploceResponse representando todas as apolices encontradas
     */
    @GetMapping("/{clientId}/listar")
    public ResponseEntity<List<ApoliceResponse>> listarApolicesPorCliente(@PathVariable("clientId") UUID id) {
        List<ApoliceResponse> apolices = apoliceService.listarApolicesPorCliente(id);
        return ResponseEntity.ok(apolices);
    }

    /**
     * Realiza a busca de uma apolice por ID
     *
     * @param id o ID da apolice para buscar os dados
     * @return Um ResponseEntity contendo a AploceResponse representando os dados da apolice encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApoliceResponse> buscarApolice(@PathVariable("id") UUID id) {
        ApoliceResponse apolice = apoliceService.buscarPorId(id);
        return ResponseEntity.ok(apolice);
    }

    /**
     * Cria uma nova apolice
     *
     * @param request O objeto ApliceRequest contendo os dados de uma apolice a ser criada.
     * @return Um ResponseEntity contendo o objeto ApoliceResponse representando a apolice criada.
     */
    @PostMapping
    public ResponseEntity<ApoliceResponse> criar(@Valid @RequestBody ApoliceRequest request) {
        ApoliceResponse apoliceResponse = apoliceService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(apoliceResponse);
    }

    /**
     * Cancela uma apolice
     *
     * @param id ID da apólice a ser buscada.
     * @return Um ResponseEntity contendo o objeto ApoliceResponse representando a apolice cancelada.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApoliceResponse> cancelar(@PathVariable("id") UUID id) {
        ApoliceResponse apolice = apoliceService.cancelarApolice(id);
        return ResponseEntity.ok(apolice);
    }
}
