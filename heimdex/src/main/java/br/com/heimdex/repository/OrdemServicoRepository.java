package br.com.heimdex.repository;

import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.model.enums.StatusOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    List<OrdemServico> findByStatus(StatusOrdemServico status);
    List<OrdemServico> findByTecnicoExecutorId(Long tecnicoId);

    @Query("SELECT os FROM OrdemServico os " +
           "LEFT JOIN FETCH os.equipamento eq " + 
           "LEFT JOIN FETCH eq.modelo m " +      
           "LEFT JOIN FETCH os.checklist c " +
           "LEFT JOIN FETCH c.itens i " +
           "LEFT JOIN FETCH i.pecasSugeridas ps " +
           "LEFT JOIN FETCH os.resultados r " +
           "LEFT JOIN FETCH r.itemTemplate " + 
           "LEFT JOIN FETCH os.fotosEvidencia fe " +
           "LEFT JOIN FETCH os.pecasConsumidas pc LEFT JOIN FETCH pc.peca " + 
           "LEFT JOIN FETCH os.tecnicoExecutor tec " + 
           "LEFT JOIN FETCH os.liderValidador lid " + 
           "WHERE os.id = :id")
    Optional<OrdemServico> findOsByIdWithDetails(@Param("id") Long id);

    boolean existsByEquipamentoIdAndTipoManutencaoAndStatusIn(Long equipamentoId, String tipoManutencao, List<StatusOrdemServico> status);

    List<OrdemServico> findAllByStatusAndTipoManutencaoAndDataFimExecucaoIsNotNullAndDataInicioExecucaoIsNotNullAndDataFimExecucaoBetween(
        StatusOrdemServico status, String tipoManutencao, LocalDateTime start, LocalDateTime end
    );

    long countByDataAgendamentoBetween(LocalDateTime start, LocalDateTime end);

    // Método utilizado pelo Controller para filtrar o histórico por máquina específica
    List<OrdemServico> findByEquipamentoIdOrderByDataAgendamentoDesc(Long equipamentoId);

    List<OrdemServico> findAllByOrderByDataAgendamentoAsc();
}