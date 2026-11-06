package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.exceptions.CredenciaisInvalidas;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoVerificado;
import io.github.fernando_mariani.TechFixOrg.exceptions.NaoEncontrado;
import io.github.fernando_mariani.TechFixOrg.exceptions.RegistroDuplicado;
import io.github.fernando_mariani.TechFixOrg.response.ErroResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(io.github.fernando_mariani.TechFixOrg.exceptions.LimiteRequisicoes.class)
    public ResponseEntity<ErroResponse> limiteRequisicoes(
            io.github.fernando_mariani.TechFixOrg.exceptions.LimiteRequisicoes exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Long.toString(exception.getRetryAfter()))
                .body(new ErroResponse(429, exception.getMessage()));
    }

    @ExceptionHandler(NaoVerificado.class)
    public ResponseEntity<ErroResponse> naoVerificado(NaoVerificado exception) {
        return responder(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidas.class)
    public ResponseEntity<ErroResponse> credenciaisInvalidas(CredenciaisInvalidas exception) {
        return responder(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(NaoEncontrado.class)
    public ResponseEntity<ErroResponse> naoEncontrado(NaoEncontrado exception) {
        return responder(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(RegistroDuplicado.class)
    public ResponseEntity<ErroResponse> registroDuplicado(RegistroDuplicado exception) {
        return responder(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(AcaoInvalida.class)
    public ResponseEntity<ErroResponse> acaoInvalida(AcaoInvalida exception) {
        return responder(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> dadosInvalidos(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));
        return acaoInvalida(new AcaoInvalida(mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException exception) {
        return acaoInvalida(new AcaoInvalida(
                "Corpo da requisição inválido. Envie um JSON válido."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException exception) {
        return acaoInvalida(new AcaoInvalida(
                "O parâmetro '" + exception.getName() + "' possui um formato inválido."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroResponse> errosNaoTratados(RuntimeException exception) {
        log.error("Erro inesperado ao processar a requisição", exception);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno ao processar a requisição. Tente novamente mais tarde.");
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(status.value(), mensagem));
    }
}
