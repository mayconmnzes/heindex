package br.com.heimdex.repository;

import br.com.heimdex.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByObservacaoContainingOrderByDataMovimentacaoDesc(String observacao);
    
    // ✅ Adicione este método exato para o histórico funcionar:
    List<MovimentacaoEstoque> findByPecaIdOrderByDataMovimentacaoDesc(Long pecaId);
    
    // Pode manter o antigo se outras telas usarem:
    List<MovimentacaoEstoque> findByPecaIdOrderByDataHoraDesc(Long pecaId);

    List<MovimentacaoEstoque> findAllByOrderByDataMovimentacaoDesc();

    List<MovimentacaoEstoque> findByDataMovimentacaoBetweenOrderByDataMovimentacaoDesc(
    java.time.LocalDateTime inicio, 
    java.time.LocalDateTime fim);

    // NOVO: busca por equipamento id (retorna movimentos vinculados ao equipamento)
    List<MovimentacaoEstoque> findByEquipamentoIdOrderByDataMovimentacaoDesc(Long equipamentoId);
}