package dev.gilbertoacl.claimflow_api.shared.util;

public class MensagensConstants {
    public static final String CLASSE_UTILIDADE_NAO_INSTANCIADA = "Esta é uma classe de utilidade e não pode ser instanciada";
    public static final String CLIENTE_NAO_ENCONTRADO = "Cliente não encontrado.";
    public static final String APOLICE_NAO_ENCONTRADA = "Apólice não encontrada.";
    public static final String APOLICE_JA_EXISTE = "Apólice já existe.";
    public static final String FIM_VIGENCIA_DEVE_SER_POSTERIOR_A_INICIO = "Fim de vigência deve ser posterior ao início.";
    public static final String APOLICE_JA_CANCELADA = "Apólice ja Cancelada" ;
    public static final String APOLICE_VENCIDA_NAO_PODE_CANCELAR = "Apólice Vencida. Não pode Cancelar.";

    private MensagensConstants() {
        throw new UnsupportedOperationException(CLASSE_UTILIDADE_NAO_INSTANCIADA);
    }
}
