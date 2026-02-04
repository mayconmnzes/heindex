package br.com.heimdex.service;

import br.com.heimdex.dto.*;
import br.com.heimdex.model.*;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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

    public List<OrdemServicoResponseDTO> listarTodas() {
        return repository.findAllWithDetails().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public OrdemServicoResponseDTO buscarPorId(Long id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço não encontrada"));
        return convertToResponseDTO(os);
    }

    @Transactional
    public OrdemServico criarOrdemServico(OrdemServicoRequestDTO dto) {
        Equipamento equip = equipamentoRepository.findById(dto.equipamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Equipamento não encontrado"));

        List<Usuario> tecnicos = usuarioRepository.findAllById(dto.tecnicosIds());
        
        Checklist checklist = null;
        if (dto.checklistId() != null) {
            checklist = checklistRepository.findById(dto.checklistId()).orElse(null);
        }

        OrdemServico os = new OrdemServico();
        os.setEquipamento(equip);
        os.setTecnicosExecutores(new HashSet<>(tecnicos));
        os.setChecklist(checklist);
        os.setTipoManutencao(dto.tipoManutencao());
        os.setDataAgendamento(dto.dataAgendamento());
        os.setStatus(StatusOrdemServico.AGENDADA);

        if (checklist != null && checklist.getItens() != null) {
            for (ItemChecklist item : checklist.getItens()) {
                ResultadoChecklistItem resultado = new ResultadoChecklistItem();
                resultado.setItemTemplate(item); 
                resultado.setDescricao(item.getDescricao()); 
                os.addResultado(resultado);
            }
        }

        return repository.save(os);
    }

    // ✅ NOVO MÉTODO: Resolve o erro de compilação do Controller
    @Transactional
    public void iniciarOrdemServico(Long id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OS não encontrada"));
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        os.setDataInicioExecucao(LocalDateTime.now());
        repository.save(os);
    }

    public OrdemServicoResponseDTO convertToResponseDTO(OrdemServico os) {
        if (os == null) return null;
        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO();
        dto.setId(os.getId());
        dto.setStatus(os.getStatus());
        dto.setTipoManutencao(os.getTipoManutencao());
        dto.setDataAgendamento(os.getDataAgendamento());
        dto.setDataInicioExecucao(os.getDataInicioExecucao());
        
        if (os.getEquipamento() != null) {
            dto.setNomeEquipamento(os.getEquipamento().getNome());
            dto.setCodigoEquipamento(os.getEquipamento().getCodigo());
        }
        
        if (os.getTecnicosExecutores() != null && !os.getTecnicosExecutores().isEmpty()) {
            dto.setNomeTecnico(os.getTecnicosExecutores().iterator().next().getNomeCompleto());
        }

        if (os.getChecklist() != null) {
            ChecklistResponseDTO chDto = new ChecklistResponseDTO();
            chDto.setId(os.getChecklist().getId());
            chDto.setNome(os.getChecklist().getNome());
            
            if (os.getChecklist().getItens() != null) {
                List<ItemChecklistDTO> itensDto = os.getChecklist().getItens().stream().map(item -> {
                    ItemChecklistDTO iDto = new ItemChecklistDTO();
                    iDto.setId(item.getId());
                    iDto.setDescricao(item.getDescricao());
                    return iDto;
                }).collect(Collectors.toList());
                chDto.setItens(itensDto);
            }
            dto.setChecklist(chDto);
        }
        return dto;
    }
}