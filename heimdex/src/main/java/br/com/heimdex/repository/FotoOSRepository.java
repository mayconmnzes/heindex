package br.com.heimdex.repository;

import br.com.heimdex.model.FotoOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoOSRepository extends JpaRepository<FotoOS, Long> {
    List<FotoOS> findByOrdemServicoId(Long ordemServicoId);
}