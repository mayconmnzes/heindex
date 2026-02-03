package br.com.heimdex.controller;

import br.com.heimdex.dto.PlanoManutencaoDTO;
import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.PlanoManutencao;
import br.com.heimdex.repository.EquipamentoRepository;
import br.com.heimdex.repository.PlanoManutencaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/planos")
public class PlanoManutencaoController {

    @Autowired
    private PlanoManutencaoRepository repository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @GetMapping
    public List<PlanoManutencaoDTO> getAll() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<PlanoManutencaoDTO> create(@RequestBody PlanoManutencaoDTO dto) {
        PlanoManutencao plano = new PlanoManutencao();
        plano.setNome(dto.getNome());
        plano.setPeriodicidadeDias(dto.getPeriodicidadeDias());
        plano.setAtivo(dto.isAtivo());
        plano.setProximaExecucao(dto.getProximaExecucao());

        if (dto.getEquipamentoId() != null) {
            Equipamento equip = equipamentoRepository.findById(dto.getEquipamentoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado"));
            plano.setEquipamento(equip);
        }

        PlanoManutencao salvo = repository.save(plano);
        return new ResponseEntity<>(convertToDTO(salvo), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoManutencaoDTO> update(@PathVariable Long id, @RequestBody PlanoManutencaoDTO dto) {
        return repository.findById(id)
                .map(existente -> {
                    existente.setNome(dto.getNome());
                    existente.setPeriodicidadeDias(dto.getPeriodicidadeDias());
                    existente.setAtivo(dto.isAtivo());
                    existente.setProximaExecucao(dto.getProximaExecucao());

                    if (dto.getEquipamentoId() != null) {
                        Equipamento equip = equipamentoRepository.findById(dto.getEquipamentoId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado"));
                        existente.setEquipamento(equip);
                    }

                    return ResponseEntity.ok(convertToDTO(repository.save(existente)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private PlanoManutencaoDTO convertToDTO(PlanoManutencao plano) {
        PlanoManutencaoDTO dto = new PlanoManutencaoDTO();
        dto.setId(plano.getId());
        dto.setNome(plano.getNome());
        dto.setPeriodicidadeDias(plano.getPeriodicidadeDias());
        dto.setAtivo(plano.isAtivo());
        dto.setProximaExecucao(plano.getProximaExecucao());

        if (plano.getEquipamento() != null) {
            dto.setEquipamentoId(plano.getEquipamento().getId());
            dto.setNomeEquipamento(plano.getEquipamento().getNome());
            dto.setCodigoEquipamento(plano.getEquipamento().getCodigo());
        }
        return dto;
    }
}
