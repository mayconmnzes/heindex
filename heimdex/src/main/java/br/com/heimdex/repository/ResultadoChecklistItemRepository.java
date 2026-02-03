package br.com.heimdex.repository;

import br.com.heimdex.model.ResultadoChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultadoChecklistItemRepository extends JpaRepository<ResultadoChecklistItem, Long> {

    /**
     * Deleta todos os registros de resultados de checklist associados a uma Ordem de Serviço.
     * O Spring Data JPA cria o código SQL (DELETE FROM...) automaticamente.
     */
    void deleteByOrdemServicoId(Long id);
}