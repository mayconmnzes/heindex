// Código Completo
package br.com.heimdex.model.enums;

/**
 * Define os status do ciclo de vida de uma Ordem de Serviço (OS).
 */
public enum StatusOrdemServico {
    SUGESTAO,                   // NOVO: Gerada automaticamente, aguardando confirmação do planejador.
    AGENDADA,                   // Confirmada pelo planejador ou criada manualmente, aguardando execução.
    EM_EXECUCAO,                // O técnico iniciou a execução da OS.
    AGUARDANDO_VALIDACAO,       // O técnico finalizou, aguardando aprovação do líder.
    CONCLUIDA,                  // A OS foi executada e validada com sucesso.
    PENDENTE_DE_CORRECAO,       // O líder reprovou a execução e devolveu para o técnico.
    // Manter outros status se necessários
    REAGENDADA_PENDENTE_JUSTIFICATIVA,
    BLOQUEADA_ACAO_URGENTE
}