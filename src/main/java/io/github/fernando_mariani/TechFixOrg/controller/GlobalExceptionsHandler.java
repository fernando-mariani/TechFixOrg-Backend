package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
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
                "Corpo da requisição inválido. Envie um JSON válido, use EMAIL, FORMS ou WHATSAPP "
                        + "para origem e datas no formato yyyy-MM-dd'T'HH:mm:ss."));
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
