package dev.gilbertoacl.claimflow_api.shared.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraDeNegocio(RegraDeNegocioException ex, HttpServletRequest request) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        ErroResponse erro = ErroResponse.of(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        ErroResponse erro = ErroResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErroResponse.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroResponse.CampoErro(erro.getField(), erro.getDefaultMessage()))
                .toList();

        ErroResponse erro = new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erro de validação nos campos enviados",
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handleIntegridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade no banco: {}", ex.getMessage());
        ErroResponse erro = ErroResponse.of(HttpStatus.CONFLICT,
                "Operação viola uma restrição de integridade dos dados (ex: valor duplicado)", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex, HttpServletRequest request) {
        log.error("Erro não tratado", ex);
        ErroResponse erro = ErroResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request.getRequestURI());
        return ResponseEntity.internalServerError().body(erro);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String mensagem = "Parâmetro '%s' com valor inválido: '%s'".formatted(ex.getName(), ex.getValue());
        ErroResponse erro = ErroResponse.of(HttpStatus.BAD_REQUEST, mensagem, request.getRequestURI());
        return ResponseEntity.badRequest().body(erro);
    }
}
