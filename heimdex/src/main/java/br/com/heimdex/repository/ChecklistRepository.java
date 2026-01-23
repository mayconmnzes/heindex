package br.com.heimdex.repository;

import br.com.heimdex.model.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    /**
     * Busca todos os Checklists que estão associados a um determinado ID de Área.
     * O Spring Data JPA gera a consulta SQL automaticamente (SELECT * FROM checklists WHERE area_id = ?).
     */
    List<Checklist> findByAreaId(Long areaId);
}