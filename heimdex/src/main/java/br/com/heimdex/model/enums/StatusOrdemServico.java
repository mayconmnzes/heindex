package br.com.heimdex.model.enums;

public enum StatusOrdemServico {
    SUGESTAO,
    AGENDADA,      // Use este no lugar de PLANEJADA
    PLANEJADA,     // Adicionado para compatibilidade com o Service
    EM_EXECUCAO,
    AGUARDANDO_VALIDACAO,
    CONCLUIDA,
    PENDENTE_DE_CORRECAO,
    REAGENDADA_PENDENTE_JUSTIFICATIVA,
    BLOQUEADA_ACAO_URGENTE
}
