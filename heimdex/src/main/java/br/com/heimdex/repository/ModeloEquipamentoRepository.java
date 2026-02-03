// Código Completo
package br.com.heimdex.repository;

import br.com.heimdex.model.ModeloEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModeloEquipamentoRepository extends JpaRepository<ModeloEquipamento, Long> {

    /**
     * Busca todos os Modelos de Equipamento que estão associados a um
     * determinado ID de Área.
     */
    List<ModeloEquipamento> findByAreaId(Long areaId);
}