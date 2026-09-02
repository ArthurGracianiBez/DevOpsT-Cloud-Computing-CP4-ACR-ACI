package br.com.api.astraai.satelite.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record SateliteRequestDto(
        @NotBlank(message = "nomeSatelite e obrigatorio")
        String nomeSatelite,

        @NotBlank(message = "statusOperacional é obrigatório")
        @Pattern(regexp = "^(Ativo|Manutencao|Inativo)$", message = "statusOperacional deve ser Ativo, Inativo ou Manutencao")
        String statusOperacional,

        @NotNull(message = "eficienciaPaineis é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "eficienciaPaineis deve ser maior que zero")
        @DecimalMax(value = "100.0", inclusive = false, message = "eficienciaPaineis deve ser menor que cem")
        @Digits(integer = 3, fraction = 2, message = "eficienciaPaineis deve conter no maximo 2 casas decimais")
        BigDecimal eficienciaPaineis,

        @NotNull(message = "capacidadeMaxGw é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "capacidadeMaxGw deve ser maior que zero")
        @Digits(integer = 3, fraction = 2, message = "capacidadeMaxGw deve conter no maximo 2 casas decimais")
        BigDecimal capacidadeMaxGw
) {
}
