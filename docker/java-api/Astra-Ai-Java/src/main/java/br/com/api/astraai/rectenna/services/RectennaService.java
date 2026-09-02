package br.com.api.astraai.rectenna.services;

import br.com.api.astraai.exceptions.CoordenadasDuplicadasException;
import br.com.api.astraai.exceptions.RecursoNaoEncontradoException;
import br.com.api.astraai.rectenna.dtos.RectennaRequestDto;
import br.com.api.astraai.rectenna.dtos.RectennaResponseDto;
import br.com.api.astraai.rectenna.models.Rectenna;
import br.com.api.astraai.rectenna.repositories.RectennaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class RectennaService {

    private final RectennaRepository rectennaRepository;
    private static final String STATUS_ATIVA = "Ativa";
    private static final String STATUS_INATIVA = "Inativa";

    public RectennaService(RectennaRepository rectennaRepository) {
        this.rectennaRepository = rectennaRepository;
    }

    public Page<RectennaResponseDto> listarRectennas(Pageable pageable, String status) {
        if (StringUtils.hasText(status)) {
            return rectennaRepository.findByStatusOperacionalIgnoreCase(status, pageable).map(this::toResponse);
        }
        return rectennaRepository.findByStatusOperacionalIgnoreCase(STATUS_ATIVA, pageable).map(this::toResponse);
    }

    public RectennaResponseDto listarRectennasPorId(Long id) {
        return toResponse(encontrarRectennaPorId(id));
    }

    public RectennaResponseDto criarRectenna(RectennaRequestDto dto) {
        validarCoordenadas(dto.latitude(), dto.longitude());

        Rectenna rectenna = new Rectenna();
        rectenna.setNomeSubestacao(dto.nomeSubestacao());
        rectenna.setLatitude(dto.latitude());
        rectenna.setLongitude(dto.longitude());
        rectenna.setCapacidadeSuportadaGwh(dto.capacidadeSuportadaGwh());
        rectenna.setStatusOperacional(dto.statusOperacional());

        return toResponse(rectennaRepository.save(rectenna));
    }

    public RectennaResponseDto atualizarRectenna(Long id, RectennaRequestDto dto) {
        Rectenna rectenna = encontrarRectennaPorId(id);
        validarCoordenadasAoAtualizar(id, dto.latitude(), dto.longitude());

        rectenna.setNomeSubestacao(dto.nomeSubestacao());
        rectenna.setLatitude(dto.latitude());
        rectenna.setLongitude(dto.longitude());
        rectenna.setCapacidadeSuportadaGwh(dto.capacidadeSuportadaGwh());
        rectenna.setStatusOperacional(dto.statusOperacional());

        return toResponse(rectennaRepository.save(rectenna));
    }

    public void deletarRectenna(Long id) {
        Rectenna rectenna = encontrarRectennaPorId(id);
        rectenna.setStatusOperacional(STATUS_INATIVA);
        rectennaRepository.save(rectenna);
    }

    public Rectenna encontrarRectennaPorId(Long id) {
        return rectennaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Rectenna nao encontrada para o id informado"));
    }

    private void validarCoordenadas(BigDecimal latitude, BigDecimal longitude) {
        if (rectennaRepository.existsByLatitudeAndLongitude(latitude, longitude)) {
            throw new CoordenadasDuplicadasException("Ja existe uma rectenna cadastrada com o mesmo par de coordenadas");
        }
    }

    private void validarCoordenadasAoAtualizar(Long id, BigDecimal latitude, BigDecimal longitude) {
        if (rectennaRepository.existsByLatitudeAndLongitudeAndIdRectennaNot(latitude, longitude, id)) {
            throw new CoordenadasDuplicadasException("Ja existe uma rectenna cadastrada com o mesmo par de coordenadas");
        }
    }

    private RectennaResponseDto toResponse(Rectenna rectenna) {
        return new RectennaResponseDto(
                rectenna.getIdRectenna(),
                rectenna.getNomeSubestacao(),
                rectenna.getLatitude(),
                rectenna.getLongitude(),
                rectenna.getCapacidadeSuportadaGwh(),
                rectenna.getStatusOperacional()
        );
    }

    public Long buscarRectennaMaisProxima(Long idRectennaOrigem) {
        Long idDestino = rectennaRepository.buscarRectennaMaisProxima(idRectennaOrigem);

        if (idDestino == null)
            throw new RecursoNaoEncontradoException("Nenhuma rectenna 'Ativa' encontrada como alternativa para redirecionamento.");

        return idDestino;
    }
}
