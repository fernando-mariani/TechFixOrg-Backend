package io.github.fernando_mariani.TechFixOrg.exceptions;

public class NaoVerificado extends RuntimeException {

    public NaoVerificado() {
        super("Usuário não verificado. Verifique sua conta antes de realizar o login.");
    }
}
