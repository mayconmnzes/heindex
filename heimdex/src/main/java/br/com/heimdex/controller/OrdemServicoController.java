package br.com.heimdex.controller;

import br.com.heimdex.dto.*;
import br.com.heimdex.model.FotoOS;
import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.repository.FotoOSRepository;
import br.com.heimdex.repository.OrdemServicoRepository;
import br.com.heimdex.service.OrdemServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService service;

    @Autowired
    private OrdemServicoRepository repository;

    @Autowired
    private FotoOSRepository fotoOSRepository;

    // Agora aceita filtro opcional equipamentoId para histórico
    @GetMapping
    public List<OrdemServicoResponseDTO> listarTodas(@RequestParam(required = false) Long equipamentoId) {
        if (equipamentoId != null) {
            // busca por equipamento específico (histórico)
            return repository.findByEquipamentoIdOrderByDataAgendamentoDesc(equipamentoId).stream()
                    .map(service::convertToResponseDTO)
                    .collect(Collectors.toList());
        }
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

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<Void> iniciar(@PathVariable Long id) {
        service.iniciarOrdemServico(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/fotos")
    public ResponseEntity<String> anexarFotoNaOs(@PathVariable Long id, @RequestBody String fotoUrlBody) {
        OrdemServico os = repository.findById(id).orElse(null);
        if (os == null) return ResponseEntity.notFound().build();

        String fotoUrl = fotoUrlBody;
        if (fotoUrl != null && fotoUrl.startsWith("\"") && fotoUrl.endsWith("\"") && fotoUrl.length() >= 2) {
            fotoUrl = fotoUrl.substring(1, fotoUrl.length() - 1);
        }

        FotoOS foto = new FotoOS();
        foto.setOrdemServico(os);
        foto.setUrl(fotoUrl);
        fotoOSRepository.save(foto);

        return ResponseEntity.ok(fotoUrl);
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<String> finalizar(@PathVariable Long id, @RequestBody FinalizarOsRequestDTO dto) {
        try {
            service.finalizarOrdemServico(id, dto);
            return ResponseEntity.ok("Finalizada");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao finalizar");
        }
    }

    @PostMapping("/{id}/validar")
    public ResponseEntity<OrdemServicoResponseDTO> validar(@PathVariable Long id, @RequestBody LiderActionRequestDTO dto) {
        try {
            service.validarOrdemServico(id, dto.getLiderId(), dto.getObservacoesLider());
            // Retorna a OS atualizada para o frontend (contendo dataValidacao, fotosEvidencia, etc.)
            OrdemServicoResponseDTO updated = service.buscarPorId(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{id}/reprovar")
    public ResponseEntity<OrdemServicoResponseDTO> reprovar(@PathVariable Long id, @RequestBody LiderActionRequestDTO dto) {
        try {
            service.reprovarOrdemServico(id, dto.getLiderId(), dto.getObservacoesLider());
            OrdemServicoResponseDTO updated = service.buscarPorId(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
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