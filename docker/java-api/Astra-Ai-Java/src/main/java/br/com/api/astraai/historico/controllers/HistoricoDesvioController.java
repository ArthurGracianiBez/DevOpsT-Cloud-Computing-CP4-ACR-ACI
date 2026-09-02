package br.com.api.astraai.historico.controllers;

import br.com.api.astraai.historico.dtos.HistoricoDesvioResponseDto;
import br.com.api.astraai.historico.services.HistoricoDesvioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/historico-desvios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Histórico de Desvios", description = "Endpoints para consulta do histórico de redirecionamentos (desvios) de feixes de energia causados por contingências climáticas.")
public class HistoricoDesvioController {

    private final HistoricoDesvioService historicoDesvioService;

    @GetMapping
    @Operation(
        summary = "Lista o histórico de desvios de energia",
        description = "Retorna uma lista paginada de todos os registros de manobras de redirecionamento, exibindo o satélite envolvido e as subestações (Rectennas) de origem e destino final."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista do histórico de desvios retornada com sucesso.")
    })
    public ResponseEntity<Page<HistoricoDesvioResponseDto>> listaHistoricoDesvio(Pageable pageable) {
        Page<HistoricoDesvioResponseDto> page = historicoDesvioService.listaHistoricoDesvio(pageable);
        return ResponseEntity.ok(page);
    }
}