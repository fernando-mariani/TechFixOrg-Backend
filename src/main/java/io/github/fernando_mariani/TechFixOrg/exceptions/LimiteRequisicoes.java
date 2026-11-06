package io.github.fernando_mariani.TechFixOrg.exceptions;

public class LimiteRequisicoes extends RuntimeException {
    private final long retryAfter;

    public LimiteRequisicoes(long retryAfter) {
        super("Limite de requisições excedido. Tente novamente mais tarde.");
        this.retryAfter = retryAfter;
    }

    public long getRetryAfter() {
        return retryAfter;
    }
}
