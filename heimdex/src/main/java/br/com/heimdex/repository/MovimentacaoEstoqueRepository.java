package br.com.heimdex.repository;

import br.com.heimdex.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    // ✅ Este método mata o erro no MovimentacaoEstoqueController
    List<MovimentacaoEstoque> findByObservacaoContainingOrderByDataMovimentacaoDesc(String observacao);
    
    List<MovimentacaoEstoque> findByPecaIdOrderByDataHoraDesc(Long pecaId);
}