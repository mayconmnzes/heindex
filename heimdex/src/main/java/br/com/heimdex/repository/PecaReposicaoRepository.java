package br.com.heimdex.repository;

import br.com.heimdex.model.PecaReposicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PecaReposicaoRepository extends JpaRepository<PecaReposicao, Long> {

    @Query("SELECT p FROM PecaReposicao p LEFT JOIN FETCH p.modeloEquipamento m LEFT JOIN FETCH m.area")
    List<PecaReposicao> findAllWithDetails();
}