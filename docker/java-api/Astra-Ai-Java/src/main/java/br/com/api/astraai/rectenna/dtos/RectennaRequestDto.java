package br.com.api.astraai.rectenna.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record RectennaRequestDto(
        @NotBlank(message = "nomeSubestacao e obrigatorio")
        String nomeSubestacao,

        @NotNull(message = "latitude é obrigatória")
        @DecimalMin(value = "-90.0", inclusive = true, message = "latitude deve ser maior ou igual a -90")
        @DecimalMax(value = "90.0", inclusive = true, message = "latitude deve ser menor ou igual a 90")
        @Digits(integer = 3, fraction = 7, message = "latitude deve conter no maximo 7 casas decimais")
        BigDecimal latitude,

        @NotNull(message = "longitude é obrigatório")
        @DecimalMin(value = "-180.0", inclusive = true, message = "longitude deve ser maior ou igual a -180")
        @DecimalMax(value = "180.0", inclusive = true, message = "longitude deve ser menor ou igual a 180")
        @Digits(integer = 3, fraction = 7, message = "longitude deve conter no maximo 7 casas decimais")
        BigDecimal longitude,

        @NotNull(message = "capacidadeSuportadaGwh é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "capacidadeSuportadaGwh deve ser maior que zero")
        @Digits(integer = 3, fraction = 2, message = "capacidadeSuportadaGwh deve conter no maximo 2 casas decimais")
        BigDecimal capacidadeSuportadaGwh,

        @NotBlank(message = "statusOperacional é obrigatório")
        @Pattern(regexp = "^(Ativa|Inativa)$", message = "statusOperacional deve ser Ativa ou Inativa")
        String statusOperacional
) {
}
