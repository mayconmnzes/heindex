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

    // 🔥 OTIMIZAÇÃO DE PERFORMANCE: Busca tudo em uma única query (Mata a lentidão)
    @Query("SELECT DISTINCT os FROM OrdemServico os " +
           "LEFT JOIN FETCH os.equipamento e " +
           "LEFT JOIN FETCH e.modelo " +
           "LEFT JOIN FETCH e.linha l " +
           "LEFT JOIN FETCH l.area " +
           "LEFT JOIN FETCH os.tecnicosExecutores " +
           "ORDER BY os.dataAgendamento DESC")
    List<OrdemServico> findAllWithDetails();

    // ✅ Mantido: Relatórios (Linha 44 do RelatorioController)
    List<OrdemServico> findAllByStatusAndTipoManutencaoAndDataFimExecucaoIsNotNullAndDataInicioExecucaoIsNotNullAndDataFimExecucaoBetween(
        StatusOrdemServico status, String tipoManutencao, LocalDateTime start, LocalDateTime end
    );

    // ✅ Mantido: Contagem (Linha 57 do RelatorioController)
    long countByDataAgendamentoBetween(LocalDateTime start, LocalDateTime end);

    // ✅ Mantido: Compatibilidade com Dashboards
    boolean existsByEquipamentoIdAndTipoManutencaoAndStatusIn(Long id, String tipo, List<StatusOrdemServico> status);
    
    // ✅ Mantido: Histórico por Equipamento
    List<OrdemServico> findByEquipamentoIdOrderByDataAgendamentoDesc(Long id);
}
