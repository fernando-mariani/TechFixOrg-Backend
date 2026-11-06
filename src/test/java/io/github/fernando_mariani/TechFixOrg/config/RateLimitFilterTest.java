package io.github.fernando_mariani.TechFixOrg.config;

import io.github.fernando_mariani.TechFixOrg.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {
    @Test
    void deveRetornar429SemExecutarCadeiaEIgnorarIpForjado() throws Exception {
        var filter = new RateLimitFilter(new RateLimitService(1, Duration.ofMinutes(1),
                5, Duration.ofMinutes(15), 10));
        var chamadas = new AtomicInteger();
        for (int i = 0; i < 2; i++) {
            var request = new MockHttpServletRequest("GET", "/pedidos");
            request.setRemoteAddr("192.0.2.1");
            request.addHeader("X-Forwarded-For", "192.0.2." + (i + 2));
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req, res) -> chamadas.incrementAndGet());
            if (i == 1) {
                assertEquals(429, response.getStatus());
                assertNotNull(response.getHeader("Retry-After"));
                assertTrue(response.getContentAsString().contains("\"status\":429"));
            }
        }
        assertEquals(1, chamadas.get());
    }
}
