package br.com.api.astraai.historico.repositories;

import br.com.api.astraai.historico.models.HistoricoConexao;
import br.com.api.astraai.historico.models.HistoricoConexaoId;
import br.com.api.astraai.rectenna.models.Rectenna;
import br.com.api.astraai.satelite.models.Satelite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoConexaoRepository extends JpaRepository<HistoricoConexao, HistoricoConexaoId> {

    boolean existsBySateliteAndRectennaAndStatusConexao(Satelite satelite, Rectenna rectenna, String statusConexao);
}
