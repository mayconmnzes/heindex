// Código Completo
package br.com.heimdex.repository;

import br.com.heimdex.model.PecaReposicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PecaReposicaoRepository extends JpaRepository<PecaReposicao, Long> {
}