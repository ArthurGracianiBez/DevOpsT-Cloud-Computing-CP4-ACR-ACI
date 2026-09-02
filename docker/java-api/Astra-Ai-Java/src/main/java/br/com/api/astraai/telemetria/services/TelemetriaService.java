package br.com.api.astraai.telemetria.services;

import br.com.api.astraai.clients.ClimaApiClient;
import br.com.api.astraai.historico.services.HistoricoConexaoService;
import br.com.api.astraai.rectenna.services.RectennaService;
import br.com.api.astraai.satelite.models.Satelite;
import br.com.api.astraai.satelite.services.SateliteService;
import br.com.api.astraai.telemetria.dtos.ComandoResponse;
import br.com.api.astraai.telemetria.dtos.TelemetriaRequest;
import br.com.api.astraai.clients.dtos.weather.OpenWeatherResponse;
import br.com.api.astraai.rectenna.models.Rectenna;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class TelemetriaService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ClimaApiClient climaApiClient;
    private final RectennaService rectennaService;
    private final HistoricoConexaoService historicoConexaoService;
    private final SateliteService sateliteService;

    @Value("${api.clima.key}")
    private String climaApiKey;

    private static final int LIMITE_SENSOR_CRITICO = 3500;
    private static final String STATUS_TRANSMITINDO = "Transmitindo";
    private static final String COMANDO_ACENDER = "ACENDER";
    private static final String COMANDO_REDIRECIONAR = "REDIRECIONAR";

    public ComandoResponse validarFeixe(TelemetriaRequest request) {
        log.info("Iniciando validação de feixe para o satélite ID: {} e rectenna ID: {}", 
                request.sateliteId(), request.rectennaId());

        Satelite satelite = sateliteService.encontrarSatelitePorId(request.sateliteId());

        if (isLeituraSensorCritica(request.leituraSensor())) {
            return processarRedirecionamento(satelite, request.rectennaId());
        }

        Rectenna rectenna = rectennaService.encontrarRectennaPorId(request.rectennaId());
        
        if (isBloqueioClimaticoDetectado(rectenna, request.leituraSensor())) {
            return processarRedirecionamento(satelite, rectenna.getIdRectenna());
        }

        return processarConexaoSegura(satelite, rectenna.getIdRectenna());
    }

    private ComandoResponse processarRedirecionamento(Satelite satelite, Long idRectennaOrigem) {
        log.info("Bloqueio crítico detectado ou sensor crítico. Solicitando recálculo de rota.");
        ComandoResponse response = calcularNovaRota(idRectennaOrigem);
        registrarHistorico(satelite, response.destinoId(), STATUS_TRANSMITINDO);
        return response;
    }

    private ComandoResponse processarConexaoSegura(Satelite satelite, Long idRectenna) {
        log.info("Canal seguro. Mantendo transmissão na rectenna de origem ID: {}", idRectenna);
        registrarHistorico(satelite, idRectenna, STATUS_TRANSMITINDO);
        return new ComandoResponse(COMANDO_ACENDER, idRectenna);
    }

    private boolean isBloqueioClimaticoDetectado(Rectenna rectenna, Integer leituraSensor) {
        try {
            log.info("Consultando API de clima para as coordenadas: lat={}, lon={}", rectenna.getLatitude(), rectenna.getLongitude());
            OpenWeatherResponse clima = climaApiClient.getClimaAtual(
                    rectenna.getLatitude(),
                    rectenna.getLongitude(),
                    "metric",
                    climaApiKey
            );
            boolean climaSevero = isClimaSevero(clima);
            log.info("API de clima respondeu. Condição de redirecionamento: {}", climaSevero);
            return climaSevero;

        } catch (RestClientException e) {
            log.warn("Falha ao comunicar com a API de clima. Erro: {}. Acionando contingência de borda.", e.getMessage());
            boolean sensorCritico = isLeituraSensorCritica(leituraSensor);
            log.info("Contingência acionada. Leitura do sensor: {}. Condição de redirecionamento: {}", leituraSensor, sensorCritico);
            return sensorCritico;
        }
    }

    private boolean isClimaSevero(OpenWeatherResponse clima) {
        if (clima == null || clima.weather() == null || clima.weather().isEmpty()) {
            return false;
        }
        int climaId = clima.weather().getFirst().id();
        log.debug("ID do clima recebido: {}", climaId);

        // IDs de 200 a 232 (Tempestades) ou 502 a 504 (Chuvas Extremas)
        // https://openweathermap.org/api/weather-conditions#Weather-Condition-Codes-2
        return (climaId >= 200 && climaId <= 232) || climaId == 502 || climaId == 503 || climaId == 504;
    }

    private boolean isLeituraSensorCritica(Integer leituraSensor) {
        return leituraSensor > LIMITE_SENSOR_CRITICO;
    }

    private ComandoResponse calcularNovaRota(Long idRectenna) {
        log.info("Buscando rectenna mais próxima para a rectenna ID: {}", idRectenna);
        Long idDestino = rectennaService.buscarRectennaMaisProxima(idRectenna);
        log.info("Encontrado destino ID: {} para a rectenna ID: {}", idDestino, idRectenna);
        return new ComandoResponse(COMANDO_REDIRECIONAR, idDestino);
    }

    private void registrarHistorico(Satelite satelite, Long idRectennaDestino, String status) {
        Rectenna rectennaDestino = rectennaService.encontrarRectennaPorId(idRectennaDestino);
        historicoConexaoService.registrarConexao(satelite, rectennaDestino, status);
    }
}
