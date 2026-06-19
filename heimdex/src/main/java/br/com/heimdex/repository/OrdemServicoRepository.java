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

    // 🔥 OTIMIZAÇÃO SEGURA: traz só relações *-para-um (sem coleções duplas)
    // Evita o produto cartesiano que estourou a memória.
    @Query("SELECT DISTINCT os FROM OrdemServico os " +
           "LEFT JOIN FETCH os.equipamento e " +
           "LEFT JOIN FETCH e.modelo " +
           "LEFT JOIN FETCH e.linha l " +
           "LEFT JOIN FETCH l.area " +
           "LEFT JOIN FETCH os.checklist " +
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

    // 🔥 OTIMIZAÇÃO SEGURA: histórico por equipamento, só relações *-para-um
    // O checklist/itens/resultados vêm pelo LAZY normal (poucas OS por equipamento, sem problema).
    @Query("SELECT DISTINCT os FROM OrdemServico os " +
           "LEFT JOIN FETCH os.equipamento e " +
           "LEFT JOIN FETCH e.modelo " +
           "LEFT JOIN FETCH e.linha l " +
           "LEFT JOIN FETCH l.area " +
           "LEFT JOIN FETCH os.checklist " +
           "WHERE os.equipamento.id = :id " +
           "ORDER BY os.dataAgendamento DESC")
    List<OrdemServico> findByEquipamentoIdOrderByDataAgendamentoDesc(Long id);

    // 🔥 OTIMIZAÇÃO: ids de equipamentos com OS preventiva ativa (uma query só)
    @Query("SELECT DISTINCT os.equipamento.id FROM OrdemServico os " +
           "WHERE os.tipoManutencao = :tipo AND os.status IN :status")
    List<Long> findEquipamentoIdsComOsAtiva(String tipo, List<StatusOrdemServico> status);
}
