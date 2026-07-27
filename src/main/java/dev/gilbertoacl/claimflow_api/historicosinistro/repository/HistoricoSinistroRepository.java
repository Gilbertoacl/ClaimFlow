package dev.gilbertoacl.claimflow_api.historicosinistro.repository;

import dev.gilbertoacl.claimflow_api.historicosinistro.entity.HistoricoSinistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoSinistroRepository extends JpaRepository<HistoricoSinistro, UUID> {
    List<HistoricoSinistro> findAllBySinistroIdOrderByDataAlteracaoAsc(UUID sinistroId);

    List<HistoricoSinistro> findAllByNumeroSinistroOrderByDataAlteracaoAsc(String numeroSinistro);
}
