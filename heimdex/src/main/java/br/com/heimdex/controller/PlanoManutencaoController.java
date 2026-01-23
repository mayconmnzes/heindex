package br.com.heimdex.controller;

import br.com.heimdex.dto.PlanoManutencaoDTO;
import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.PlanoManutencao;
import br.com.heimdex.repository.EquipamentoRepository;
import br.com.heimdex.repository.PlanoManutencaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/planos-manutencao")
public class PlanoManutencaoController {

    @Autowired private PlanoManutencaoRepository planoRepository;
    @Autowired private EquipamentoRepository equipamentoRepository;

    private PlanoManutencaoDTO convertToDTO(PlanoManutencao plano) {
        PlanoManutencaoDTO dto = new PlanoManutencaoDTO();
        dto.setId(plano.getId());
        dto.setNome(plano.getNome());
        dto.setPeriodicidadeDias(plano.getPeriodicidadeDias());
        dto.setAtivo(plano.isAtivo());
        if (plano.getProximaExecucao() != null) {
            dto.setProximaExecucao(plano.getProximaExecucao().toString());
        }
        if (plano.getEquipamento() != null) {
            dto.setEquipamentoId(plano.getEquipamento().getId());
            dto.setNomeEquipamento(plano.getEquipamento().getNome());
            dto.setCodigoEquipamento(plano.getEquipamento().getCodigo());
        }
        return dto;
    }

    // CREATE e UPDATE (POST e PUT)
    @PostMapping
    @Transactional
    public ResponseEntity<?> criarOuAtualizarPlano(@RequestBody PlanoManutencaoDTO dto) {
        Equipamento equipamento = equipamentoRepository.findById(dto.getEquipamentoId()).orElse(null);
        if (equipamento == null) {
            return ResponseEntity.badRequest().body("Equipamento não encontrado.");
        }

        PlanoManutencao plano = dto.getId() != null ? planoRepository.findById(dto.getId()).orElse(new PlanoManutencao()) : new PlanoManutencao();
        
        // Verifica se já existe um plano para este equipamento (apenas para novos planos)
        if (plano.getId() == null && planoRepository.findByEquipamentoId(equipamento.getId()).isPresent()) {
             return ResponseEntity.status(HttpStatus.CONFLICT).body("Já existe um plano para este equipamento.");
        }

        plano.setNome(dto.getNome());
        plano.setEquipamento(equipamento);
        plano.setPeriodicidadeDias(dto.getPeriodicidadeDias());
        plano.setAtivo(dto.isAtivo());
        
        if (dto.getProximaExecucao() != null && !dto.getProximaExecucao().isEmpty()) {
            plano.setProximaExecucao(LocalDate.parse(dto.getProximaExecucao()));
        } else if (plano.getId() == null) {
             plano.setProximaExecucao(LocalDate.now());
        }

        PlanoManutencao savedPlano = planoRepository.save(plano);
        return new ResponseEntity<>(convertToDTO(savedPlano), HttpStatus.CREATED);
    }
    
    @GetMapping
    public List<PlanoManutencaoDTO> listarPlanos() {
        return planoRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // DELETE (DELETE)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletarPlano(@PathVariable Long id) {
        if (!planoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        planoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}