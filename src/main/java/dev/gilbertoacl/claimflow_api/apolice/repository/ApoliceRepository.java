package dev.gilbertoacl.claimflow_api.apolice.repository;

import dev.gilbertoacl.claimflow_api.apolice.entity.Apolice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApoliceRepository extends JpaRepository<Apolice, UUID> {
    Optional<Apolice> findByNumeroApolice(String numeroApolice);
    List<Apolice> findAllByClienteId(UUID clienteId);
}
