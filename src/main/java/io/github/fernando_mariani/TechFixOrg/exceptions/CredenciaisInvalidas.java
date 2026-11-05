package io.github.fernando_mariani.TechFixOrg.exceptions;

public class CredenciaisInvalidas extends RuntimeException {

    public CredenciaisInvalidas() {
        super("E-mail ou senha inválidos.");
    }
}
