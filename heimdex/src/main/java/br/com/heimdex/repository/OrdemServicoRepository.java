package br.com.heimdex.repository;

import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.model.enums.StatusOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    // 🔥 OTIMIZAÇÃO: traz equipamento + checklist + itens + resultados numa query só
    // (mata o N+1 que causava os ~46 segundos)
    @Query("SELECT DISTINCT os FROM OrdemServico os " +
           "LEFT JOIN FETCH os.equipamento e " +
           "LEFT JOIN FETCH e.modelo " +
           "LEFT JOIN FETCH e.linha l " +
           "LEFT JOIN FETCH l.area " +
           "LEFT JOIN FETCH os.tecnicosExecutores " +
           "LEFT JOIN FETCH os.checklist ck " +
           "LEFT JOIN FETCH ck.itens " +
           "LEFT JOIN FETCH os.resultados r " +
           "LEFT JOIN FETCH r.itemTemplate " +
           "ORDER BY os.dataAgendamento DESC")
    List<OrdemServico> findAllWithDetails();

    // ✅ Mantido: Relatórios
    List<OrdemServico> findAllByStatusAndTipoManutencaoAndDataFimExecucaoIsNotNullAndDataInicioExecucaoIsNotNullAndDataFimExecucaoBetween(
        StatusOrdemServico status, String tipoManutencao, LocalDateTime start, LocalDateTime end
    );

    // ✅ Mantido: Contagem
    long countByDataAgendamentoBetween(LocalDateTime start, LocalDateTime end);

    // ✅ Mantido: Compatibilidade com Dashboards
    boolean existsByEquipamentoIdAndTipoManutencaoAndStatusIn(Long id, String tipo, List<StatusOrdemServico> status);

    // 🔥 OTIMIZAÇÃO: histórico por equipamento agora também traz checklist + resultados de uma vez
    // (essencial pra tela de histórico que foi corrigida na auditoria não quebrar nem ficar lenta)
    @Query("SELECT DISTINCT os FROM OrdemServico os " +
           "LEFT JOIN FETCH os.equipamento e " +
           "LEFT JOIN FETCH e.modelo " +
           "LEFT JOIN FETCH e.linha l " +
           "LEFT JOIN FETCH l.area " +
           "LEFT JOIN FETCH os.tecnicosExecutores " +
           "LEFT JOIN FETCH os.checklist ck " +
           "LEFT JOIN FETCH ck.itens " +
           "LEFT JOIN FETCH os.resultados r " +
           "LEFT JOIN FETCH r.itemTemplate " +
           "WHERE os.equipamento.id = :id " +
           "ORDER BY os.dataAgendamento DESC")
    List<OrdemServico> findByEquipamentoIdOrderByDataAgendamentoDesc(Long id);
// 🔥 OTIMIZAÇÃO: busca de uma vez TODOS os ids de equipamentos que têm OS preventiva ativa
    // (elimina o N+1 de perguntar equipamento por equipamento)
    @Query("SELECT DISTINCT os.equipamento.id FROM OrdemServico os " +
           "WHERE os.tipoManutencao = :tipo AND os.status IN :status")
    List<Long> findEquipamentoIdsComOsAtiva(String tipo, List<StatusOrdemServico> status);
}
