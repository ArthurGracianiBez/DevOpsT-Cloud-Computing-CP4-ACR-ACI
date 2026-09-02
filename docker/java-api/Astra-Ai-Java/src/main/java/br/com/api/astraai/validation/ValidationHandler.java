package br.com.api.astraai.validation;

import br.com.api.astraai.exceptions.CoordenadasDuplicadasException;
import br.com.api.astraai.exceptions.NomeSateliteDuplicadoException;
import br.com.api.astraai.exceptions.RecursoNaoEncontradoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@RestControllerAdvice
public class ValidationHandler {

    public record ErrorResponse(String mensagem, Integer codigoStatus) {
    }

    public record ValidationErrorResponse(String field, String message) {
        public ValidationErrorResponse(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorResponse>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<ValidationErrorResponse> errors = exception.getFieldErrors().stream()
                .map(ValidationErrorResponse::new)
                .toList();
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Recurso não encontrado: " + exception.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleRestClientResponseException(RestClientResponseException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String message = "Erro ao comunicar com serviço externo: ";
        if (ex.getStatusCode().value() == 401) {
            message += "Falha de Autenticação (Chave de API inválida)";
        } else if (ex.getStatusCode().value() == 400) {
            message += "Requisição mal formatada (Bad Request)";
        } else {
            message += "Status " + ex.getStatusCode().value();
        }

        return ResponseEntity.status(status)
                .body(new ErrorResponse(message, status.value()));
    }

    @ExceptionHandler(CoordenadasDuplicadasException.class)
    public ResponseEntity<ErrorResponse> handleCoordenadasDuplicadas(CoordenadasDuplicadasException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(NomeSateliteDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleNomeSateliteDuplicado(NomeSateliteDuplicadoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Erro interno no servidor: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
