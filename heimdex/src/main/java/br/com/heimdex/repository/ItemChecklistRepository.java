// Código Completo
package br.com.heimdex.repository;

import br.com.heimdex.model.ItemChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemChecklistRepository extends JpaRepository<ItemChecklist, Long> {
}