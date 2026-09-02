package br.com.api.astraai.historico.services;

import br.com.api.astraai.historico.dtos.HistoricoDesvioResponseDto;
import br.com.api.astraai.historico.repositories.HistoricoDesvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoricoDesvioService {

    private final HistoricoDesvioRepository historicoDesvioRepository;

    public Page<HistoricoDesvioResponseDto> listaHistoricoDesvio(Pageable pageable) {
        return historicoDesvioRepository.findAll(pageable)
                .map(HistoricoDesvioResponseDto::new);
    }
}