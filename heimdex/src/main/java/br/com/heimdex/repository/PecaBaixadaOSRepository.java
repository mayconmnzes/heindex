// Código Completo
package br.com.heimdex.repository;

import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.ModeloEquipamento; // NOVO IMPORT
import br.com.heimdex.model.PecaBaixadaOS;
import br.com.heimdex.model.PecaReposicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PecaBaixadaOSRepository extends JpaRepository<PecaBaixadaOS, Long> {

    List<PecaBaixadaOS> findByOrdemServicoId(Long ordemServicoId);

    // --- Record ConsumoPeca (sem alteração) ---
    record ConsumoPeca(PecaReposicao peca, long total) {}

    // --- Query findConsumoTotalPorPeca (sem alteração) ---
    @Query("SELECT new br.com.heimdex.repository.PecaBaixadaOSRepository$ConsumoPeca(p.peca, SUM(p.quantidadeBaixada)) " +
           "FROM PecaBaixadaOS p " +
           "GROUP BY p.peca " +
           "ORDER BY SUM(p.quantidadeBaixada) DESC")
    List<ConsumoPeca> findConsumoTotalPorPeca();


    // --- ALTERAÇÃO NO RECORD E NA QUERY ---
    /**
     * ATUALIZADO: O record agora inclui o ModeloEquipamento (Tipo).
     */
    record ConsumoEquipamentoPeca(Equipamento equipamento, ModeloEquipamento modelo, PecaReposicao peca, long total) {}

    /**
     * ATUALIZADO: A query agora faz JOIN com o Modelo do Equipamento (os.equipamento.modelo).
     * O agrupamento agora inclui o modelo.
     */
    @Query("SELECT new br.com.heimdex.repository.PecaBaixadaOSRepository$ConsumoEquipamentoPeca(os.equipamento, os.equipamento.modelo, p.peca, SUM(p.quantidadeBaixada)) " +
           "FROM PecaBaixadaOS p JOIN p.ordemServico os " +
           "JOIN os.equipamento.modelo " + // Garante o JOIN com o modelo
           "GROUP BY os.equipamento, os.equipamento.modelo, p.peca " +
           "ORDER BY os.equipamento.nome, SUM(p.quantidadeBaixada) DESC")
    List<ConsumoEquipamentoPeca> findConsumoTotalPorEquipamentoEPeca();
    // --- FIM DA ALTERAÇÃO ---


    // --- Método existsByPecaId (sem alteração) ---
    boolean existsByPecaId(Long pecaId);
}