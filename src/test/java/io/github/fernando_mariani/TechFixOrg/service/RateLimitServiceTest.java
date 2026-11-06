package io.github.fernando_mariani.TechFixOrg.service;

import io.github.fernando_mariani.TechFixOrg.exceptions.LimiteRequisicoes;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {
    @Test
    void deveLimitarPorChaveELiberarAposJanelaSemEstenderBloqueio() {
        var clock = new TestClock();
        var limiter = new RateLimitService.WindowLimiter(2, Duration.ofMinutes(1), 10, clock);
        limiter.verificar("ip1");
        limiter.verificar("ip1");
        assertEquals(60, assertThrows(LimiteRequisicoes.class, () -> limiter.verificar("ip1")).getRetryAfter());
        limiter.verificar("ip2");
        clock.agora = clock.agora.plusSeconds(30);
        assertEquals(30, assertThrows(LimiteRequisicoes.class, () -> limiter.verificar("ip1")).getRetryAfter());
        clock.agora = clock.agora.plusSeconds(30);
        assertDoesNotThrow(() -> limiter.verificar("ip1"));
    }

    @Test
    void naoDeveDescartarChavesAtivasQuandoMemoriaEstiverCheia() {
        var clock = new TestClock();
        var limiter = new RateLimitService.WindowLimiter(1, Duration.ofMinutes(1), 1, clock);
        limiter.verificar("ip1");
        assertThrows(LimiteRequisicoes.class, () -> limiter.verificar("ip2"));
        assertThrows(LimiteRequisicoes.class, () -> limiter.verificar("ip1"));
        clock.agora = clock.agora.plusSeconds(60);
        assertDoesNotThrow(() -> limiter.verificar("ip2"));
    }

    @Test
    void deveNormalizarEmailESepararLimiteGeralDoLogin() {
        var service = new RateLimitService(600, Duration.ofMinutes(1), 1, Duration.ofMinutes(15), 10);
        service.verificarLogin(" MARIA@example.com ", "192.0.2.1");
        assertThrows(LimiteRequisicoes.class,
                () -> service.verificarLogin("maria@EXAMPLE.com", "192.0.2.1"));
        assertDoesNotThrow(() -> service.verificarLogin("maria@example.com", "192.0.2.2"));
        assertDoesNotThrow(() -> service.verificarLogin("outro@example.com", "192.0.2.1"));
        assertDoesNotThrow(() -> service.verificarIp("127.0.0.1"));
    }

    @Test
    void deveRespeitarLimiteComRequisicoesConcorrentes() throws Exception {
        var limiter = new RateLimitService.WindowLimiter(5, Duration.ofMinutes(1), 10, new TestClock());
        var aceitas = new AtomicInteger();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var tasks = new java.util.ArrayList<Future<?>>();
            for (int i = 0; i < 100; i++) {
                tasks.add(executor.submit(() -> {
                    try {
                        limiter.verificar("mesmo-email");
                        aceitas.incrementAndGet();
                    } catch (LimiteRequisicoes ignored) { }
                }));
            }
            for (var task : tasks) task.get();
        }
        assertEquals(5, aceitas.get());
    }

    private static class TestClock extends Clock {
        private Instant agora = Instant.parse("2026-01-01T00:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return agora; }
    }
}
