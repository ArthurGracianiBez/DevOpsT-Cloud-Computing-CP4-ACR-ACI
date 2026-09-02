package br.com.api.astraai.telemetria.controllers;

import br.com.api.astraai.telemetria.dtos.ComandoResponse;
import br.com.api.astraai.telemetria.dtos.TelemetriaRequest;
import br.com.api.astraai.telemetria.services.TelemetriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/telemetria")
@RequiredArgsConstructor
@Tag(name = "Telemetria", description = "Endpoint central para a comunicação com o hardware de borda (IoT/ESP32).")
public class TelemetriaController {

    private final TelemetriaService telemetriaService;

    @PostMapping("/validar-feixe")
    @Operation(
        summary = "Valida o canal de transmissão de energia e comanda o atuador de borda.",
        description = "Recebe a telemetria da Rectenna (ID e leitura do sensor de atenuação), orquestra a validação climática com a OpenWeather API e, se necessário, aciona a function no banco de dados para recálculo de rota. Retorna o comando final ('ACENDER' ou 'REDIRECIONAR') para o dispositivo IoT."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validação concluída com sucesso. O corpo da resposta contém o comando final para o atuador."),
        @ApiResponse(responseCode = "400", description = "Dados de telemetria inválidos (ex: ID da Rectenna nulo)."),
        @ApiResponse(responseCode = "404", description = "Rectenna com o ID informado não foi encontrada no banco de dados."),
        @ApiResponse(responseCode = "500", description = "Falha interna no servidor, incluindo erros de comunicação com a API OpenWeather após o acionamento da contingência.")
    })
    public ResponseEntity<ComandoResponse> validarFeixe(@RequestBody @Valid TelemetriaRequest request) {
        ComandoResponse response = telemetriaService.validarFeixe(request);
        return ResponseEntity.ok(response);
    }
}
