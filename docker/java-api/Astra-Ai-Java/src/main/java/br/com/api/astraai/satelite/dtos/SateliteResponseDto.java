package br.com.api.astraai.satelite.dtos;

import java.math.BigDecimal;

public record SateliteResponseDto(
        Long idSatelite,
        String nomeSatelite,
        String statusOperacional,
        BigDecimal eficienciaPaineis,
        BigDecimal capacidadeMaxGw
) {
}
