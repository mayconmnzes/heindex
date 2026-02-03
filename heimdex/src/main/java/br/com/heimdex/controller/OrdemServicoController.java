package br.com.heimdex.controller;

import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.service.OrdemServicoService;
import br.com.heimdex.dto.OrdemServicoRequestDTO;
import br.com.heimdex.dto.OrdemServicoResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService service;

    @PostMapping
    public ResponseEntity<OrdemServicoResponseDTO> criar(@RequestBody OrdemServicoRequestDTO dto) {
        // Criamos a OS e já retornamos o DTO convertido
        OrdemServico os = service.criarOrdemServico(dto);
        return ResponseEntity.ok(service.convertToResponseDTO(os));
    }

    @GetMapping
    public List<OrdemServicoResponseDTO> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // Este método é usado pelo EquipamentoController para listar o histórico
    public OrdemServicoResponseDTO convertToResponseDTO(OrdemServico os) {
        return service.convertToResponseDTO(os);
    }
}
