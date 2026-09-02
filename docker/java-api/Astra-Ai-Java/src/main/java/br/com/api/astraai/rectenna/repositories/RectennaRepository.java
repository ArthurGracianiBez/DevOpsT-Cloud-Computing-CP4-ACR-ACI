package br.com.api.astraai.rectenna.repositories;

import br.com.api.astraai.rectenna.models.Rectenna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface RectennaRepository extends JpaRepository<Rectenna, Long> {

    boolean existsByLatitudeAndLongitude(BigDecimal latitude, BigDecimal longitude);

    boolean existsByLatitudeAndLongitudeAndIdRectennaNot(BigDecimal latitude, BigDecimal longitude, Long idRectenna);

    Page<Rectenna> findAll(Pageable pageable);

    Page<Rectenna> findByStatusOperacionalIgnoreCase(String statusOperacional, Pageable pageable);

    @Query(
            value = "SELECT PKG_GESTAO_CONEXOES.fn_rectenna_mais_proxima(:idRectenna) FROM dual",
            nativeQuery = true
    )
    Long buscarRectennaMaisProxima(
            @Param("idRectenna") Long idRectenna
    );
}
