package br.com.api.astraai.historico.services;

import br.com.api.astraai.historico.models.HistoricoConexao;
import br.com.api.astraai.historico.models.HistoricoConexaoId;
import br.com.api.astraai.historico.repositories.HistoricoConexaoRepository;
import br.com.api.astraai.rectenna.models.Rectenna;
import br.com.api.astraai.satelite.models.Satelite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistoricoConexaoService {

    private final HistoricoConexaoRepository historicoConexaoRepository;

    public void registrarConexao(Satelite satelite, Rectenna rectenna, String statusConexao) {
        log.info("Tentando registrar conexão - Satélite: {}, Rectenna: {}, Status: {}",
                satelite.getIdSatelite(), rectenna.getIdRectenna(), statusConexao);

        if (existeConexaoAtiva(satelite, rectenna)) {
            log.info("Já existe uma conexão ativa (Transmitindo) para o Satélite {} e Rectenna {}. Registro ignorado.",
                    satelite.getIdSatelite(), rectenna.getIdRectenna());
            return;
        }
        log.info("Nenhuma conexão ativa encontrada. Registrando nova conexão.");
        historicoConexaoRepository.save(criaHistoricoConexao(satelite, rectenna, statusConexao));
        log.info("Conexão registrada com sucesso no histórico.");
    }

    public boolean existeConexaoAtiva(Satelite satelite, Rectenna rectenna) {
        return historicoConexaoRepository.existsBySateliteAndRectennaAndStatusConexao(satelite, rectenna, "Transmitindo");
    }

    private HistoricoConexao criaHistoricoConexao(Satelite satelite, Rectenna rectenna, String statusConexao){
        HistoricoConexaoId id = new HistoricoConexaoId(
                satelite.getIdSatelite(),
                rectenna.getIdRectenna(),
                LocalDateTime.now()
        );

        HistoricoConexao historico = new HistoricoConexao();
        historico.setId(id);
        historico.setSatelite(satelite);
        historico.setRectenna(rectenna);
        historico.setStatusConexao(statusConexao);

        return historico;
    }
}
