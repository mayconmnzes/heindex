package br.com.heimdex.repository;

import br.com.heimdex.model.PlanoManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanoManutencaoRepository extends JpaRepository<PlanoManutencao, Long> {
    
    // Encontra planos de manutenção ativos que precisam ser gerados
    List<PlanoManutencao> findByAtivoTrueAndProximaExecucaoLessThanEqual(java.time.LocalDate dataLimite);
    
    // Garante que só há um plano por equipamento
    Optional<PlanoManutencao> findByEquipamentoId(Long equipamentoId);
}