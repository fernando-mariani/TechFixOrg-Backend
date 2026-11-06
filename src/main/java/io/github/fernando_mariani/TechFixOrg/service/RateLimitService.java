package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.exceptions.LimiteRequisicoes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;

@Service
public class RateLimitService {
    private final WindowLimiter geral;
    private final WindowLimiter login;

    public RateLimitService(
            @Value("${security.rate-limit.general.limit:600}") int limiteGeral,
            @Value("${security.rate-limit.general.window:PT1M}") Duration janelaGeral,
            @Value("${security.rate-limit.login.limit:5}") int limiteLogin,
            @Value("${security.rate-limit.login.window:PT15M}") Duration janelaLogin,
            @Value("${security.rate-limit.max-entries:50000}") int maxEntries) {
        geral = new WindowLimiter(limiteGeral, janelaGeral, maxEntries, Clock.systemUTC());
        login = new WindowLimiter(limiteLogin, janelaLogin, maxEntries, Clock.systemUTC());
    }

    public void verificarIp(String ip) {
        geral.verificar(ip);
    }

    public void verificarLogin(String email, String ip) {
        String emailNormalizado = email.strip().toLowerCase(Locale.ROOT);
        login.verificar(emailNormalizado.length() + ":" + emailNormalizado + ip);
    }

    // Janelas fixas iniciadas na primeira requisição de cada chave.
    static final class WindowLimiter {
        private final int limite;
        private final long janelaMs;
        private final int maxEntries;
        private final Clock clock;
        private final LinkedHashMap<String, Janela> janelas = new LinkedHashMap<>();

        WindowLimiter(int limite, Duration janela, int maxEntries, Clock clock) {
            if (limite <= 0 || janela.isNegative() || janela.isZero() || janela.toMillis() <= 0 || maxEntries <= 0) {
                throw new IllegalArgumentException("Os limites e as janelas devem ser positivos.");
            }
            this.limite = limite;
            this.janelaMs = janela.toMillis();
            this.maxEntries = maxEntries;
            this.clock = clock;
        }

        synchronized void verificar(String chave) {
            long agora = clock.millis();
            // A ordem de inserção também é a ordem de expiração. Não removemos
            // chaves ativas para abrir espaço, pois isso permitiria burlar o limite.
            var iterator = janelas.entrySet().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getValue().expiraEm > agora) break;
                iterator.remove();
            }
            Janela janela = janelas.get(chave);
            if (janela == null) {
                if (janelas.size() >= maxEntries) {
                    throw excedido(janelas.firstEntry().getValue().expiraEm, agora);
                }
                janela = new Janela(agora + janelaMs);
                janelas.put(chave, janela);
            }
            if (janela.contagem >= limite) throw excedido(janela.expiraEm, agora);
            janela.contagem++;
        }

        private LimiteRequisicoes excedido(long expiraEm, long agora) {
            return new LimiteRequisicoes(Math.max(1, (expiraEm - agora + 999) / 1000));
        }

        private static final class Janela {
            private final long expiraEm;
            private int contagem;

            private Janela(long expiraEm) {
                this.expiraEm = expiraEm;
            }
        }
    }
}
