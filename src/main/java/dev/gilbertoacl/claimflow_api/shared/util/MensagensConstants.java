package dev.gilbertoacl.claimflow_api.shared.util;

public class MensagensConstants {
    public static final String CLASSE_UTILIDADE_NAO_INSTANCIADA = "Esta é uma classe de utilidade e não pode ser instanciada";
    public static final String CLIENTE_NAO_ENCONTRADO = "Cliente não encontrado.";
    public static final String APOLICE_NAO_ENCONTRADA = "Apólice não encontrada.";
    public static final String APOLICE_JA_EXISTE = "Apólice já existe.";
    public static final String FIM_VIGENCIA_DEVE_SER_POSTERIOR_A_INICIO = "Fim de vigência deve ser posterior ao início.";
    public static final String APOLICE_JA_CANCELADA = "Apólice ja Cancelada." ;
    public static final String APOLICE_VENCIDA_NAO_PODE_CANCELAR = "Apólice Vencida. Não pode Cancelar.";
    public static final String APOLICE_INATIVA_NAO_PODE_ABRIR_SINISTRO = "Apólice Vencida. Não pode abrir Sinistro.";
    public static final String SINISTRO_FORA_DE_VIGTENCIA = "A ocorrência está fora da vigência da apólice.";
    public static final String VALOR_SOLICITADO_MAIOR_QUE_SEGURADO = "O valor solicitado não pode ser maior que o valor segurado.";
    public static final String LIMITE_DIARIO_SINISTRO_ATINGIDO = "Limite de sinistros diário atingido para o tipo de apolice: ";
    public static final String SINISTRO_NAO_ENCONTRADO = "Sinistro não encontrado";
    public static final String SINISTRO_ABERTO = "Sinistro Aberto";

    private MensagensConstants() {
        throw new UnsupportedOperationException(CLASSE_UTILIDADE_NAO_INSTANCIADA);
    }
}
