package br.com.api.astraai.satelite.controllers;

import br.com.api.astraai.satelite.dtos.SateliteRequestDto;
import br.com.api.astraai.satelite.dtos.SateliteResponseDto;
import br.com.api.astraai.satelite.services.SateliteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/satelites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Satélites", description = "Endpoints para o gerenciamento da frota orbital de satélites responsáveis pela captação e transmissão contínua de energia solar (SBSP).")
public class SateliteController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SateliteService sateliteService;

    @GetMapping
    @Operation(summary = "Lista toda a frota de satélites",
               description = "Retorna uma lista paginada dos satélites que compõem a malha de captação orbital do Astra AI. Permite filtro opcional por status operacional.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de satélites retornada com sucesso.")
    })
    public ResponseEntity<Page<SateliteResponseDto>> listarSatelites(
            Pageable pageable,
            @RequestParam(required = false) String statusOperacional
    ) {
        log.info("Requisição recebida para listar satelites. Filtro statusOperacional: {}", statusOperacional);
        return ResponseEntity.ok(sateliteService.listarSatelites(pageable, statusOperacional));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de um satélite específico",
               description = "Recupera as informações operacionais e técnicas de um satélite ativo na constelação por meio do seu ID único.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Satélite localizado com sucesso."),
        @ApiResponse(responseCode = "404", description = "Nenhum satélite correspondente encontrado na órbita para o ID informado.")
    })
    public ResponseEntity<EntityModel<SateliteResponseDto>> listarSatelitePorId(@PathVariable Long id) {
        log.info("Requisição recebida para listar a satelite com id: {}", id);
        SateliteResponseDto satelite = sateliteService.listarSatelitePorId(id);

        EntityModel<SateliteResponseDto> resource = EntityModel.of(satelite)
                .add(linkTo(methodOn(SateliteController.class).listarSatelitePorId(id)).withSelfRel())
                .add(linkTo(methodOn(SateliteController.class).listarSatelites(PageRequest.of(0, 10), null)).withRel("listar-todos"))
                .add(linkTo(methodOn(SateliteController.class).atualizarSatelite(id, null)).withRel("atualizar"))
                .add(linkTo(methodOn(SateliteController.class).deletarSatelite(id)).withRel("deletar"));

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @Operation(summary = "Registra um novo satélite na rede",
               description = "Cadastra um recém-lançado equipamento orbital no ecossistema Astra AI, habilitando-o para roteamento e transmissão de energia via micro-ondas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Satélite comissionado e registrado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados técnicos do satélite inválidos. Verifique o corpo da requisição.")
    })
    public ResponseEntity<SateliteResponseDto> criarSatelite(@Valid @RequestBody SateliteRequestDto requestDto) {
        log.info("Requisição recebida para criar um novo satelite");
        SateliteResponseDto created = sateliteService.criarSatelite(requestDto);
        log.info("Satelite criado com sucesso: {}", created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza parâmetros operacionais do satélite",
               description = "Permite a modificação das especificações e do status de operação de um satélite existente na malha.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Parâmetros do satélite atualizados com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos."),
        @ApiResponse(responseCode = "404", description = "Satélite alvo não encontrado.")
    })
    public ResponseEntity<SateliteResponseDto> atualizarSatelite(@PathVariable Long id, @Valid @RequestBody SateliteRequestDto requestDto) {
        log.info("Requisição recebida para atualizar a satelite com id: {}", id);
        SateliteResponseDto updated = sateliteService.atualizarSatelite(id, requestDto);
        log.info("Satelite atualizada com sucesso: {}", updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Descomissiona um satélite",
               description = "Remove o satélite da plataforma Astra AI, sinalizando seu descomissionamento definitivo da rede de transmissão de energia.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Satélite descomissionado e removido com sucesso."),
        @ApiResponse(responseCode = "404", description = "Satélite alvo não encontrado.")
    })
    public ResponseEntity<Void> deletarSatelite(@PathVariable Long id) {
        log.info("Requisição recebida para deletar a satelite com id: {}", id);
        sateliteService.deletarAntena(id);
        log.info("Satelite deletada com sucesso: {}", id);
        return ResponseEntity.noContent().build();
    }
}
