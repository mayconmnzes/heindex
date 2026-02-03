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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    @Autowired private ChecklistRepository checklistRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private PecaReposicaoRepository pecaReposicaoRepository;

    // --- MÉTODOS DE CONVERSÃO ---

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
        
        // Seta o nome da área para o DTO
        if (checklist.getArea() != null) {
            dto.setAreaNome(checklist.getArea().getNome());
        }

        // CORREÇÃO CRUCIAL: Converte o Set<ItemChecklist> da Model para List<ItemChecklistDTO> do DTO
        if (checklist.getItens() != null) {
            List<ItemChecklistDTO> itensDTO = checklist.getItens().stream().map(item -> {
                ItemChecklistDTO itemDto = new ItemChecklistDTO();
                itemDto.setId(item.getId());
                itemDto.setDescricao(item.getDescricao());
                
                // Converte as peças sugeridas do item, se houver
                if (item.getPecasSugeridas() != null) {
                    itemDto.setPecasSugeridas(item.getPecasSugeridas().stream()
                            .map(this::convertPecaToDTO)
                            .collect(Collectors.toList()));
                }
                return itemDto;
            }).collect(Collectors.toList());
            
            dto.setItens(itensDTO);
        }
        
        return dto;
    }

    // --- CRUD ---

    @PostMapping
    @Transactional
    public ResponseEntity<?> criarChecklist(@RequestBody ChecklistRequestDTO dto) {
        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new RuntimeException("Área com ID " + dto.getAreaId() + " não encontrada."));

        Checklist novoChecklist = new Checklist();
        novoChecklist.setNome(dto.getNome());
        novoChecklist.setArea(area);

        // Criamos a coleção de itens
        java.util.List<ItemChecklist> listaItens = new java.util.ArrayList<>();
        
        if (dto.getItens() != null) {
            for (ChecklistRequestDTO.ItemChecklistRequestDTO itemDto : dto.getItens()) {
                ItemChecklist novoItem = new ItemChecklist();
                novoItem.setDescricao(itemDto.getDescricao());
                novoItem.setChecklist(novoChecklist);
                
                if (itemDto.getPecasSugeridasIds() != null && !itemDto.getPecasSugeridasIds().isEmpty()) {
                    java.util.List<PecaReposicao> pecasList = pecaReposicaoRepository.findAllById(itemDto.getPecasSugeridasIds());
                    // Garantimos que pecasSugeridas receba o tipo correto (Set ou List conforme sua Model)
                    novoItem.setPecasSugeridas(new java.util.HashSet<>(pecasList));
                }
                listaItens.add(novoItem);
            }
        }
        
        // ✅ CORREÇÃO FINAL: Se sua model Checklist exige Set, mude para: 
        // novoChecklist.setItens(new java.util.HashSet<>(listaItens));
        // Se ela exige List (como o erro sugere), use:
        novoChecklist.setItens(listaItens);

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
            
            // Limpa itens antigos e adiciona os novos para evitar duplicidade ou órfãos
            checklistExistente.getItens().clear(); 
            
            Set<ItemChecklist> novosItens = dto.getItens().stream().map(itemDto -> {
                ItemChecklist novoItem = new ItemChecklist();
                novoItem.setDescricao(itemDto.getDescricao());
                novoItem.setChecklist(checklistExistente); 
                
                if (itemDto.getPecasSugeridasIds() != null && !itemDto.getPecasSugeridasIds().isEmpty()) {
                    List<PecaReposicao> pecasList = pecaReposicaoRepository.findAllById(itemDto.getPecasSugeridasIds());
                    novoItem.setPecasSugeridas(new HashSet<>(pecasList));
                }
                
                return novoItem;
            }).collect(Collectors.toSet());

            checklistExistente.getItens().addAll(novosItens);
            
            Checklist checklistAtualizado = checklistRepository.save(checklistExistente);
            return ResponseEntity.ok(convertToResponseDTO(checklistAtualizado));
        }).orElse(ResponseEntity.notFound().build());
    }
    
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
        return checklistRepository.findById(id)
                .map(this::convertToResponseDTO)
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