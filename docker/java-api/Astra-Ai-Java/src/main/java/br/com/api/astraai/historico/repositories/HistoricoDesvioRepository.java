package br.com.api.astraai.historico.repositories;

import br.com.api.astraai.historico.models.HistoricoDesvio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoDesvioRepository extends JpaRepository<HistoricoDesvio, Long> {
    Page<HistoricoDesvio> findAll(Pageable pageable);
}