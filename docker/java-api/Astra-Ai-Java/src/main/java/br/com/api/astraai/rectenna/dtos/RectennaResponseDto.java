package br.com.api.astraai.rectenna.dtos;

import java.math.BigDecimal;

public record RectennaResponseDto(
        Long idRectenna,
        String nomeSubestacao,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal capacidadeSuportadaGwh,
        String statusOperacional
) {
}
