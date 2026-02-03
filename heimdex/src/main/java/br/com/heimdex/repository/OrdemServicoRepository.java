package br.com.heimdex.repository;

import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.model.enums.StatusOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    // ✅ Este método mata os erros na linha 44 do RelatorioController
    List<OrdemServico> findAllByStatusAndTipoManutencaoAndDataFimExecucaoIsNotNullAndDataInicioExecucaoIsNotNullAndDataFimExecucaoBetween(
        StatusOrdemServico status, String tipoManutencao, LocalDateTime start, LocalDateTime end
    );

    // ✅ Este método mata o erro na linha 57 do RelatorioController
    long countByDataAgendamentoBetween(LocalDateTime start, LocalDateTime end);

    // Métodos extras de compatibilidade
    boolean existsByEquipamentoIdAndTipoManutencaoAndStatusIn(Long id, String tipo, List<StatusOrdemServico> status);
    List<OrdemServico> findByEquipamentoIdOrderByDataAgendamentoDesc(Long id);
}