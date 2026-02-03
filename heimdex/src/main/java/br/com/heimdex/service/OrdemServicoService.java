package br.com.heimdex.service;

import br.com.heimdex.dto.OrdemServicoRequestDTO;
import br.com.heimdex.dto.OrdemServicoResponseDTO;
import br.com.heimdex.model.*;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChecklistRepository checklistRepository;

    // --- NOVO: Método que o Controller precisa para o Dashboard ---
    public List<OrdemServicoResponseDTO> listarTodas() {
    // Trocamos o findAll() pelo novo método otimizado
        return repository.findAllWithDetails().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- NOVO: Método que o Controller precisa para buscar por ID ---
    public OrdemServicoResponseDTO buscarPorId(Long id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço não encontrada"));
        return convertToResponseDTO(os);
    }

    @Transactional
    public OrdemServico criarOrdemServico(OrdemServicoRequestDTO dto) {
        // 1. Busca Equipamento
        Equipamento equip = equipamentoRepository.findById(dto.equipamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Equipamento não encontrado"));

        // 2. Busca Técnicos (Múltiplos)
        List<Usuario> tecnicos = usuarioRepository.findAllById(dto.tecnicosIds());
        if (tecnicos.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um técnico válido.");
        }

        // 3. Busca Checklist
        Checklist checklist = null;
        if (dto.checklistId() != null) {
            checklist = checklistRepository.findById(dto.checklistId()).orElse(null);
        }

        // 4. Instancia a OS
        OrdemServico os = new OrdemServico();
        os.setEquipamento(equip);
        os.setTecnicosExecutores(new HashSet<>(tecnicos));
        os.setChecklist(checklist);
        os.setTipoManutencao(dto.tipoManutencao());
        os.setDataAgendamento(dto.dataAgendamento());
        os.setStatus(StatusOrdemServico.AGENDADA);

        // 5. Integração Automática do Checklist
        if (checklist != null && checklist.getItens() != null) {
            for (ItemChecklist item : checklist.getItens()) {
                ResultadoChecklistItem resultado = new ResultadoChecklistItem();
                resultado.setItemTemplate(item); 
                resultado.setDescricao(item.getDescricao()); 
                resultado.setConforme(null); 
                os.addResultado(resultado);
            }
        }

        return repository.save(os);
    }

    // --- NOVO: Conversor Interno para transformar Model em DTO (Evita erro 500) ---
    public OrdemServicoResponseDTO convertToResponseDTO(OrdemServico os) {
        if (os == null) return null;
        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO();
        dto.setId(os.getId());
        dto.setStatus(os.getStatus());
        dto.setTipoManutencao(os.getTipoManutencao());
        dto.setDataAgendamento(os.getDataAgendamento());
        
        if (os.getEquipamento() != null) {
            dto.setNomeEquipamento(os.getEquipamento().getNome());
            dto.setCodigoEquipamento(os.getEquipamento().getCodigo());
        }
        
        // Se houver técnicos, pega o nome do primeiro para exibir na lista simplificada
        if (os.getTecnicosExecutores() != null && !os.getTecnicosExecutores().isEmpty()) {
            dto.setNomeTecnico(os.getTecnicosExecutores().iterator().next().getNomeCompleto());
        }
        
        return dto;
    }
}
