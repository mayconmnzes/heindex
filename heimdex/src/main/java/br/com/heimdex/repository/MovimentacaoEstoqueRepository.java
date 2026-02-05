package br.com.heimdex.repository;

import br.com.heimdex.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByObservacaoContainingOrderByDataMovimentacaoDesc(String observacao);

    List<MovimentacaoEstoque> findByPecaIdOrderByDataMovimentacaoDesc(Long pecaId);

    List<MovimentacaoEstoque> findAllByOrderByDataMovimentacaoDesc();

    List<MovimentacaoEstoque> findByDataMovimentacaoBetweenOrderByDataMovimentacaoDesc(java.time.LocalDateTime inicio, java.time.LocalDateTime fim);

    List<MovimentacaoEstoque> findByEquipamentoIdOrderByDataMovimentacaoDesc(Long equipamentoId);

    List<MovimentacaoEstoque> findByTipoMovimentacaoOrderByDataMovimentacaoDesc(String tipoMovimentacao);

    // NOVO: busca por vários tipos (ex: SAIDA + SAIDA_AVULSA)
    List<MovimentacaoEstoque> findByTipoMovimentacaoInOrderByDataMovimentacaoDesc(List<String> tipos);
}