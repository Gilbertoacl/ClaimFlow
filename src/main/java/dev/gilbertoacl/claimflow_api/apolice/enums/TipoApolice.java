package dev.gilbertoacl.claimflow_api.apolice.enums;

public enum TipoApolice {
    AUTOMOVEL("101"),
    RESIDENCIAL("201"),
    VIDA("301"),
    SAUDE("401");

    private final String codigoRamo;

    TipoApolice(String codigoRamo) {
        this.codigoRamo = codigoRamo;
    }

    public String getCodigoRamo() {
        return codigoRamo;
    }
}
