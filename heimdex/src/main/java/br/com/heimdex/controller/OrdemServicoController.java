package br.com.heimdex.controller;

import br.com.heimdex.dto.*;
import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.repository.OrdemServicoRepository;
import br.com.heimdex.service.OrdemServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService service;

    @Autowired
    private OrdemServicoRepository repository;

    @GetMapping
    public List<OrdemServicoResponseDTO> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public OrdemServicoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponseDTO> criar(@RequestBody OrdemServicoRequestDTO dto) {
        OrdemServico novaOs = service.criarOrdemServico(dto);
        return ResponseEntity.ok(service.convertToResponseDTO(novaOs));
    }

    // ✅ CORREÇÃO: Endpoint para iniciar a OS
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<Void> iniciar(@PathVariable Long id) {
        service.iniciarOrdemServico(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/checklist")
    public ResponseEntity<ChecklistResponseDTO> getChecklistDaOS(@PathVariable Long id) {
        OrdemServicoResponseDTO osDto = service.buscarPorId(id);
        if (osDto != null && osDto.getChecklist() != null) {
            return ResponseEntity.ok(osDto.getChecklist());
        }
        return ResponseEntity.notFound().build();
    }
}