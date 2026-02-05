package br.com.heimdex.repository;

import br.com.heimdex.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    // Método usado pelo controller para garantir unicidade do código
    boolean existsByCodigo(String codigo);

    Optional<Equipamento> findByCodigo(String codigo);

    // Preserva a query customizada que o projeto já usava para buscar equipamentos com detalhes.
    // Se no seu repositório original o nome/método for diferente, ajuste para o nome correto.
    @Query("select e from Equipamento e left join fetch e.modelo left join fetch e.linha l left join fetch l.area left join fetch e.checklist")
    List<Equipamento> findAllWithDetails();
}