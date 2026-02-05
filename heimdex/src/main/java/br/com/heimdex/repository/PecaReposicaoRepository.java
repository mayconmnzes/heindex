package br.com.heimdex.repository;

import br.com.heimdex.model.PecaReposicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PecaReposicaoRepository extends JpaRepository<PecaReposicao, Long> {

    Optional<PecaReposicao> findByCodigoControleIgnoreCase(String codigoControle);

    Optional<PecaReposicao> findByCodigoRequisicaoIgnoreCase(String codigoRequisicao);

    List<PecaReposicao> findByNomeContainingIgnoreCase(String nome);

    @Query("select p.codigoControle from PecaReposicao p where p.codigoControle is not null and p.codigoControle like ?1")
    List<String> findCodigoControleLike(String pattern);
}