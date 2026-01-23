// Código Completo
package br.com.heimdex.repository;

import br.com.heimdex.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
}