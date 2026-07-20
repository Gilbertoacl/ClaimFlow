package dev.gilbertoacl.claimflow_api.sinistro.controller;

import dev.gilbertoacl.claimflow_api.sinistro.dto.AtualizarStatusSinistroRequest;
import dev.gilbertoacl.claimflow_api.sinistro.dto.SinistroRequest;
import dev.gilbertoacl.claimflow_api.sinistro.dto.SinistroResponse;
import dev.gilbertoacl.claimflow_api.sinistro.service.SinistroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sinistros")
@RequiredArgsConstructor
public class SinistroController {

    private final SinistroService sinistroService;

    /**
     * Abre um novo sinistro vinculado a uma apólice.
     *
     * @param request os dados do sinistro a ser aberto.
     * @return um ResponseEntity contendo o SinistroResponse representando o sinistro criado.
     */
    @PostMapping
    public ResponseEntity<SinistroResponse> abrir(@Valid @RequestBody SinistroRequest request) {
        SinistroResponse response = sinistroService.abrirSinistro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um sinistro pelo seu ID.
     *
     * @param id o ID do sinistro.
     * @return um ResponseEntity contendo o SinistroResponse encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SinistroResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(sinistroService.buscarPorId(id));
    }

    /**
     * Lista todos os sinistros vinculados a uma apólice.
     *
     * @param apoliceId o ID da apólice.
     * @return um ResponseEntity contendo a lista de SinistroResponse encontrados.
     */
    @GetMapping("/apolice/{apoliceId}/listar")
    public ResponseEntity<List<SinistroResponse>> listarPorApolice(@PathVariable UUID apoliceId) {
        return ResponseEntity.ok(sinistroService.listarPorApolice(apoliceId));
    }

    /**
     * Atualiza o status de um sinistro, respeitando a máquina de estados.
     *
     * @param request o novo status desejado e observação opcional.
     * @return um ResponseEntity contendo o SinistroResponse atualizado.
     */
    @PatchMapping("/status")
    public ResponseEntity<SinistroResponse> atualizarStatus(@Valid @RequestBody AtualizarStatusSinistroRequest request) {
        SinistroResponse response = sinistroService.atualizarStatus(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um sinistro pelo seu número de identificação (número do comunicado).
     *
     * @param numeroSinistro o número único do sinistro.
     * @return um ResponseEntity contendo o SinistroResponse encontrado.
     */
        @GetMapping("/numero/{numeroSinistro}")
        public ResponseEntity<SinistroResponse> buscarPorNumero(@PathVariable String numeroSinistro) {
            return ResponseEntity.ok(sinistroService.buscarPorNumeroSinistro(numeroSinistro));
        }
}