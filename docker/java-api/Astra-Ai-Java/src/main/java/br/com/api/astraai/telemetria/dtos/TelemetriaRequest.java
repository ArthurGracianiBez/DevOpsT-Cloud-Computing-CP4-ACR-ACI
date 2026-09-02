package br.com.api.astraai.telemetria.dtos;

import jakarta.validation.constraints.NotNull;

public record TelemetriaRequest(
        @NotNull
        Long sateliteId,
        @NotNull
        Long rectennaId,
        @NotNull
        Integer leituraSensor
) {}
