package br.com.api.astraai.telemetria.dtos;

public record ComandoResponse(
        String comando,
        Long destinoId
) {}
