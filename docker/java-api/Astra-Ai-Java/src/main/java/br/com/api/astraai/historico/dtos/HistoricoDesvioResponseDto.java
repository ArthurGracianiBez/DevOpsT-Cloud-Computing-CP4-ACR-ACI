package br.com.api.astraai.historico.dtos;

import br.com.api.astraai.historico.models.HistoricoDesvio;

import java.time.LocalDateTime;

public record HistoricoDesvioResponseDto(
    Long idDesvio,
    Long idSatelite,
    String nomeSatelite,
    Long idRectennaOrigem,
    String nomeRectennaOrigem,
    Long idRectennaDestino,
    String nomeRectennaDestino,
    LocalDateTime dataHoraManobra
) {
    public HistoricoDesvioResponseDto(HistoricoDesvio historico) {
        this(
            historico.getIdDesvio(),
            historico.getSatelite().getIdSatelite(),
            historico.getSatelite().getNomeSatelite(),
            historico.getRectennaOrigem().getIdRectenna(),
            historico.getRectennaOrigem().getNomeSubestacao(),
            historico.getRectennaDestino().getIdRectenna(),
            historico.getRectennaDestino().getNomeSubestacao(),
            historico.getDataHoraManobra()
        );
    }
}
