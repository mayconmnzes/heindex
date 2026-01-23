package br.com.heimdex.repository;

import br.com.heimdex.model.LinhaDeProducao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinhaDeProducaoRepository extends JpaRepository<LinhaDeProducao, Long> {

    // Busca todas as linhas de produção pertencentes a uma área específica
    List<LinhaDeProducao> findByAreaId(Long areaId);
}