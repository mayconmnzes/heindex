package br.com.heimdex.repository;

import br.com.heimdex.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    // 🔥 OTIMIZAÇÃO: Busca Equipamento + Modelo + Linha + Área em 1 só SELECT
    @Query("SELECT DISTINCT e FROM Equipamento e " +
           "LEFT JOIN FETCH e.modelo " +
           "LEFT JOIN FETCH e.linha l " +
           "LEFT JOIN FETCH l.area")
    List<Equipamento> findAllWithDetails();
}
