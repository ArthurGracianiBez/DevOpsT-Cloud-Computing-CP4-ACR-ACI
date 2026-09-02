package br.com.api.astraai.satelite.services;

import br.com.api.astraai.exceptions.RecursoNaoEncontradoException;
import br.com.api.astraai.exceptions.NomeSateliteDuplicadoException;
import br.com.api.astraai.satelite.dtos.SateliteRequestDto;
import br.com.api.astraai.satelite.dtos.SateliteResponseDto;
import br.com.api.astraai.satelite.models.Satelite;
import br.com.api.astraai.satelite.repositories.SateliteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SateliteService {

    private final SateliteRepository sateliteRepository;

    public SateliteService(SateliteRepository sateliteRepository) {
        this.sateliteRepository = sateliteRepository;
    }


    public Page<SateliteResponseDto> listarSatelites(Pageable pageable, String statusOperacional) {
        if (!StringUtils.hasText(statusOperacional)) {
            return sateliteRepository.findAll(pageable).map(this::toResponse);
        }

        return sateliteRepository.findByStatusOperacionalIgnoreCase(statusOperacional.trim(), pageable)
                .map(this::toResponse);
    }

    public SateliteResponseDto listarSatelitePorId(Long id) {
        return toResponse(encontrarSatelitePorId(id));
    }

    public SateliteResponseDto criarSatelite(SateliteRequestDto requestDto) {
        if (sateliteRepository.existsByNomeSateliteIgnoreCase(requestDto.nomeSatelite()))
            throw new NomeSateliteDuplicadoException(
                    "Ja existe um satelite com o nome: " + requestDto.nomeSatelite());

        Satelite satelite = new Satelite();
        satelite.setNomeSatelite(requestDto.nomeSatelite());
        satelite.setStatusOperacional(requestDto.statusOperacional());
        satelite.setEficienciaPaineis(requestDto.eficienciaPaineis());
        satelite.setCapacidadeMaxGw(requestDto.capacidadeMaxGw());

        return toResponse(sateliteRepository.save(satelite));
    }

    public SateliteResponseDto atualizarSatelite(Long id, SateliteRequestDto requestDto) {
        Satelite satelite = encontrarSatelitePorId(id);

        if (sateliteRepository.existsByNomeSateliteIgnoreCaseAndIdSateliteNot(requestDto.nomeSatelite(), id))
            throw new NomeSateliteDuplicadoException(
                    "Ja existe outro satelite com o nome: " + requestDto.nomeSatelite());


        satelite.setNomeSatelite(requestDto.nomeSatelite());
        satelite.setStatusOperacional(requestDto.statusOperacional());
        satelite.setEficienciaPaineis(requestDto.eficienciaPaineis());
        satelite.setCapacidadeMaxGw(requestDto.capacidadeMaxGw());

        return toResponse(sateliteRepository.save(satelite));
    }

    public void deletarAntena(Long id) {
        sateliteRepository.delete(encontrarSatelitePorId(id));
    }

    public Satelite encontrarSatelitePorId(Long id){
        return sateliteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Satelite nao encontrado para o id informado"));
    }


    private SateliteResponseDto toResponse(Satelite satelite){
        return new SateliteResponseDto(
                satelite.getIdSatelite(),
                satelite.getNomeSatelite(),
                satelite.getStatusOperacional(),
                satelite.getEficienciaPaineis(),
                satelite.getCapacidadeMaxGw()
        );
    }
}
