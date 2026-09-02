package br.com.api.astraai.satelite.repositories;

import br.com.api.astraai.satelite.models.Satelite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SateliteRepository extends JpaRepository<Satelite, Long> {

    Page<Satelite> findByStatusOperacionalIgnoreCase(String statusOperacional, Pageable pageable);

    boolean existsByNomeSateliteIgnoreCase(String nomeSatelite);

    boolean existsByNomeSateliteIgnoreCaseAndIdSateliteNot(String nomeSatelite, Long idSatelite);
}
