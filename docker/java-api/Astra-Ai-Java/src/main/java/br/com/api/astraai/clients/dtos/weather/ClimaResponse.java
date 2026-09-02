package br.com.api.astraai.clients.dtos.weather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClimaResponse(
        String cidade,
        String condicao,
        @JsonProperty("climaSevero")
        boolean climaSevero
) {}
