package br.com.heimdex.controller;

import br.com.heimdex.dto.ChecklistRequestDTO;
import br.com.heimdex.dto.ChecklistResponseDTO;
import br.com.heimdex.dto.ItemChecklistDTO;
import br.com.heimdex.dto.PecaReposicaoResponseDTO;
import br.com.heimdex.model.Area;
import br.com.heimdex.model.Checklist;
import br.com.heimdex.model.ItemChecklist;
import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.AreaRepository;
import br.com.heimdex.repository.ChecklistRepository;
import br.com.heimdex.repository.PecaReposicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet; // MUDANÇA: Import para HashSet
import java.util.List;
import java.util.Optional;
import java.util.Set;     // MUDANÇA: Import para Set
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    @Autowired private ChecklistRepository checklistRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private PecaReposicaoRepository pecaReposicaoRepository;

    // --- MÉTODOS DE CONVERSÃO (SEM ALTERAÇÃO, JÁ USAM STREAM QUE FUNCIONA COM SET) ---
    private PecaReposicaoResponseDTO convertPecaToDTO(PecaReposicao peca) {
        if (peca == null) return null;
        PecaReposicaoResponseDTO dto = new PecaReposicaoResponseDTO();
        dto.setId(peca.getId());
        dto.setNome(peca.getNome());
        dto.setCodigoControle(peca.getCodigoControle());
        return dto;
    }

    private ChecklistResponseDTO convertToResponseDTO(Checklist checklist) {
        ChecklistResponseDTO dto = new ChecklistResponseDTO();
        dto.setId(checklist.getId());
        dto.setNome(checklist.getNome());
        
        if (checklist.getArea() != null) {
            dto.setAreaNome(checklist.getArea().getNome());
        }

        if (checklist.getItens() != null) {
            List<ItemChecklistDTO> itensDto = checklist.getItens().stream().map(item -> {
                ItemChecklistDTO itemDto = new ItemChecklistDTO();
                itemDto.setId(item.getId());
                itemDto.setDescricao(item.getDescricao());
                
                if (item.getPecasSugeridas() != null) {
                    itemDto.setPecasSugeridas(item.getPecasSugeridas().stream()
                        .map(this::convertPecaToDTO)
                        .collect(Collectors.toList()));
                }
                
                return itemDto;
            }).collect(Collectors.toList());
            dto.setItens(itensDto);
        }

        return dto;
    }

    // --- CRUD ATUALIZADO PARA USAR SET ---
    @PostMapping
    @Transactional
    public ResponseEntity<?> criarChecklist(@RequestBody ChecklistRequestDTO dto) {
        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new RuntimeException("Área com ID " + dto.getAreaId() + " não encontrada."));

        Checklist novoChecklist = new Checklist();
        novoChecklist.setNome(dto.getNome());
        novoChecklist.setArea(area);

        // MUDANÇA: Usa Set em vez de List
        Set<ItemChecklist> itens = new HashSet<>();
        for (ChecklistRequestDTO.ItemChecklistRequestDTO itemDto : dto.getItens()) {
            ItemChecklist novoItem = new ItemChecklist();
            novoItem.setDescricao(itemDto.getDescricao());
            novoItem.setChecklist(novoChecklist);
            
            if (itemDto.getPecasSugeridasIds() != null && !itemDto.getPecasSugeridasIds().isEmpty()) {
                List<PecaReposicao> pecasList = pecaReposicaoRepository.findAllById(itemDto.getPecasSugeridasIds());
                // MUDANÇA CRÍTICA: Converte a List para um Set antes de atribuir
                novoItem.setPecasSugeridas(new HashSet<>(pecasList));
            }
            
            itens.add(novoItem);
        }
        novoChecklist.setItens(itens);

        Checklist checklistSalvo = checklistRepository.save(novoChecklist);
        return new ResponseEntity<>(convertToResponseDTO(checklistSalvo), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizarChecklist(@PathVariable Long id, @RequestBody ChecklistRequestDTO dto) {
        return checklistRepository.findById(id).map(checklistExistente -> {
            Area area = areaRepository.findById(dto.getAreaId())
                    .orElseThrow(() -> new RuntimeException("Área com ID " + dto.getAreaId() + " não encontrada."));
            
            checklistExistente.setNome(dto.getNome());
            checklistExistente.setArea(area);
            
            checklistExistente.getItens().clear(); 
            
            // MUDANÇA: Coleta os itens diretamente para um Set
            Set<ItemChecklist> novosItens = dto.getItens().stream().map(itemDto -> {
                ItemChecklist novoItem = new ItemChecklist();
                novoItem.setDescricao(itemDto.getDescricao());
                novoItem.setChecklist(checklistExistente); 
                
                if (itemDto.getPecasSugeridasIds() != null && !itemDto.getPecasSugeridasIds().isEmpty()) {
                    List<PecaReposicao> pecasList = pecaReposicaoRepository.findAllById(itemDto.getPecasSugeridasIds());
                    // MUDANÇA CRÍTICA: Converte a List para um Set
                    novoItem.setPecasSugeridas(new HashSet<>(pecasList));
                }
                
                return novoItem;
            }).collect(Collectors.toSet()); // MUDANÇA: usa toSet()

            checklistExistente.getItens().addAll(novosItens);
            
            Checklist checklistAtualizado = checklistRepository.save(checklistExistente);
            return ResponseEntity.ok(convertToResponseDTO(checklistAtualizado));
        }).orElse(ResponseEntity.notFound().build());
    }
    
    // --- MÉTODOS DE LEITURA (EXISTENTES) ---
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletarChecklist(@PathVariable Long id) {
        if (!checklistRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        checklistRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<ChecklistResponseDTO> listarChecklists() {
        return checklistRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChecklistResponseDTO> buscarChecklistPorId(@PathVariable Long id) {
        Optional<Checklist> checklist = checklistRepository.findById(id);
        return checklist.map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/by-area/{areaId}")
    public List<ChecklistResponseDTO> listarChecklistsPorArea(@PathVariable Long areaId) {
        return checklistRepository.findByAreaId(areaId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}