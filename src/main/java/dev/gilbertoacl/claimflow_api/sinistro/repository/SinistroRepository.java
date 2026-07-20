package dev.gilbertoacl.claimflow_api.sinistro.repository;

import dev.gilbertoacl.claimflow_api.sinistro.entity.Sinistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SinistroRepository extends JpaRepository<Sinistro, UUID> {
    List<Sinistro> findAllByApoliceId(UUID apoliceId);

    long countByNumeroSinistroStartingWith(String prefixo);

    Optional<Sinistro> findByNumeroSinistro(String numeroSinistro);
}
