package br.com.api.astraai.rectenna.controllers;

import br.com.api.astraai.rectenna.dtos.RectennaRequestDto;
import br.com.api.astraai.rectenna.dtos.RectennaResponseDto;
import br.com.api.astraai.rectenna.services.RectennaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/rectennas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Rectennas", description = "Endpoints para o gerenciamento completo das subestações terrestres (Rectennas) que recebem a energia espacial.")
public class RectennaController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RectennaService rectennaService;

    @GetMapping
    @Operation(summary = "Lista as Rectennas de forma paginada",
               description = "Retorna uma lista paginada de rectennas. Por padrão, lista apenas as 'Ativas'. Use o parâmetro 'status' para ver as 'Inativas' ou todas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de Rectennas retornada com sucesso.")
    })
    public ResponseEntity<Page<RectennaResponseDto>> listarRectennas(
            Pageable pageable,
            @Parameter(description = "Filtra as rectennas pelo status operacional (ex: 'Ativa', 'Inativa'). Se não for fornecido, o padrão é 'Ativa'.")
            @RequestParam(required = false) String status) {
        log.info("Requisição recebida para listar rectennas com status: {}", status);
        return ResponseEntity.ok(rectennaService.listarRectennas(pageable, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma Rectenna por ID",
               description = "Retorna os detalhes de uma subestação terrestre específica, incluindo suas coordenadas e capacidade, com links HATEOAS para outras ações.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rectenna encontrada com sucesso."),
        @ApiResponse(responseCode = "404", description = "Rectenna com o ID especificado não foi encontrada.")
    })
    public ResponseEntity<EntityModel<RectennaResponseDto>> listarRectennasPorId(@PathVariable Long id) {
        log.info("Requisição recebida para listar a rectenna com id: {}", id);
        RectennaResponseDto rectenna = rectennaService.listarRectennasPorId(id);

        EntityModel<RectennaResponseDto> resource = EntityModel.of(rectenna)
                .add(linkTo(methodOn(RectennaController.class).listarRectennasPorId(id)).withSelfRel())
                .add(linkTo(methodOn(RectennaController.class).listarRectennas(PageRequest.of(0, 10), null)).withRel("listar-todas"))
                .add(linkTo(methodOn(RectennaController.class).atualizarRectenna(id, null)).withRel("atualizar"))
                .add(linkTo(methodOn(RectennaController.class).deletarRectenna(id)).withRel("deletar"));

        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova Rectenna",
               description = "Cria uma nova subestação terrestre no sistema, que se torna um alvo potencial para o redirecionamento de feixes de energia.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Rectenna cadastrada com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos. Verifique o payload da requisição.")
    })
    public ResponseEntity<RectennaResponseDto> criarRectenna(@Valid @RequestBody RectennaRequestDto requestDto) {
        log.info("Requisição recebida para criar uma nova rectenna");
        RectennaResponseDto created = rectennaService.criarRectenna(requestDto);
        log.info("Rectenna criada com sucesso: {}", created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de uma Rectenna existente",
               description = "Permite a atualização dos dados cadastrais de uma subestação, como sua capacidade ou status operacional.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rectenna atualizada com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos."),
        @ApiResponse(responseCode = "404", description = "Rectenna com o ID especificado não foi encontrada.")
    })
    public ResponseEntity<RectennaResponseDto> atualizarRectenna(@PathVariable Long id, @Valid @RequestBody RectennaRequestDto requestDto) {
        log.info("Requisição recebida para atualizar a rectenna com id: {}", id);
        RectennaResponseDto updated = rectennaService.atualizarRectenna(id, requestDto);
        log.info("Rectenna atualizada com sucesso: {}", updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa uma Rectenna (Soft Delete)",
               description = "Realiza a exclusão lógica de uma subestação, alterando seu status para 'Inativa'. A rectenna não é fisicamente removida do banco de dados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Rectenna desativada com sucesso."),
        @ApiResponse(responseCode = "404", description = "Rectenna com o ID especificado não foi encontrada.")
    })
    public ResponseEntity<Void> deletarRectenna(@PathVariable Long id) {
        log.info("Requisição recebida para deletar a rectenna com id: {}", id);
        rectennaService.deletarRectenna(id);
        log.info("Rectenna deletada com sucesso: {}", id);
        return ResponseEntity.noContent().build();
    }
}
