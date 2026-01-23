package br.com.heimdex.repository;

import br.com.heimdex.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByPecaIdOrderByDataMovimentacaoDesc(Long pecaId);
    
    // Busca por observação (onde guardamos o nome da máquina) com ordem cronológica
    List<MovimentacaoEstoque> findByObservacaoContainingOrderByDataMovimentacaoDesc(String termo);

    boolean existsByPecaId(Long pecaId);
}