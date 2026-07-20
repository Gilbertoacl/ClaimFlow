package dev.gilbertoacl.claimflow_api.shared.exceptions;

public class EstadoInvalidoException extends RegraDeNegocioException {
    public EstadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
