package io.github.fernando_mariani.TechFixOrg.controller;

import io.github.fernando_mariani.TechFixOrg.dto.CriarTecnicoDTO;
import io.github.fernando_mariani.TechFixOrg.entity.PedidosEntity.Categoria;
import io.github.fernando_mariani.TechFixOrg.exceptions.AcaoInvalida;
import io.github.fernando_mariani.TechFixOrg.response.TecnicoResponse;
import io.github.fernando_mariani.TechFixOrg.service.TecnicoService;
import io.github.fernando_mariani.TechFixOrg.service.TokenTecnicoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tecnico")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoService service;
    private final TokenTecnicoService tokenTecnicoService;

    @GetMapping("/me")
    public TecnicoResponse buscarMeuPerfil(@AuthenticationPrincipal Jwt jwt) {
        return service.buscarPorUsuarioId(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping(value = "/params", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> exibirFormulario(@RequestParam("id") UUID usuarioId,
                                                    @RequestParam("token") String token,
                                                    HttpServletRequest request) {
        if (!tokenTecnicoService.verificarToken(token, usuarioId)) {
            throw new AcaoInvalida("Convite inválido, expirado ou já utilizado.");
        }

        String opcoes = Arrays.stream(Categoria.values())
                .map(categoria -> "<option value=\"%s\">%s</option>".formatted(categoria.name(), categoria.name()))
                .collect(Collectors.joining("\n"));

        String pagina = """
                <!doctype html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Cadastro de técnico</title>
                </head>
                <body>
                    <main>
                        <h1>Concluir cadastro como técnico</h1>
                        <form method="post" action="%s">
                            <input type="hidden" name="id" value="%s">
                            <input type="hidden" name="token" value="%s">
                            <p><label for="categorias">Categorias de atuação</label><br>
                            <select id="categorias" name="categorias" multiple required aria-describedby="ajuda">
                                %s
                            </select><br>
                            <small id="ajuda">Selecione pelo menos uma categoria. No computador, use Ctrl ou ⌘ para selecionar várias.</small></p>
                            <button type="submit">Confirmar cadastro</button>
                        </form>
                    </main>
                </body>
                </html>
                """.formatted(HtmlUtils.htmlEscape(request.getRequestURI()), usuarioId, HtmlUtils.htmlEscape(token), opcoes);

        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .header("Referrer-Policy", "no-referrer").body(pagina);
    }

    @PostMapping(value = "/params", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> criar(@RequestParam("id") UUID usuarioId,
                                        @RequestParam("token") String token,
                                        @Valid @ModelAttribute CriarTecnicoDTO dto) {
        service.criar(usuarioId, token, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Cache-Control", "no-store")
                .header("Referrer-Policy", "no-referrer")
                .body("""
                        <!doctype html>
                        <html lang="pt-BR"><head><meta charset="UTF-8"><title>Cadastro concluído</title></head>
                        <body><main><h1>Cadastro de técnico concluído</h1></main></body>
                        </html>
                        """);
    }
}
