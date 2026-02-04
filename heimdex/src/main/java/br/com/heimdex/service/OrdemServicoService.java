package br.com.heimdex.service;

import br.com.heimdex.dto.ChecklistResponseDTO;
import br.com.heimdex.dto.FinalizarOsRequestDTO;
import br.com.heimdex.dto.ItemChecklistDTO;
import br.com.heimdex.dto.OrdemServicoRequestDTO;
import br.com.heimdex.dto.OrdemServicoResponseDTO;
import br.com.heimdex.dto.ResultadoChecklistResponseDTO;
import br.com.heimdex.model.Checklist;
import br.com.heimdex.model.FotoOS;
import br.com.heimdex.model.ItemChecklist;
import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.model.ResultadoChecklistItem;
import br.com.heimdex.model.Usuario;
import br.com.heimdex.model.enums.ItemStatus;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.ChecklistRepository;
import br.com.heimdex.repository.EquipamentoRepository;
import br.com.heimdex.repository.FotoOSRepository;
import br.com.heimdex.repository.OrdemServicoRepository;
import br.com.heimdex.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChecklistRepository checklistRepository;
    private final FotoOSRepository fotoOSRepository;

    public OrdemServicoService(OrdemServicoRepository repository,
                               EquipamentoRepository equipamentoRepository,
                               UsuarioRepository usuarioRepository,
                               ChecklistRepository checklistRepository,
                               FotoOSRepository fotoOSRepository) {
        this.repository = repository;
        this.equipamentoRepository = equipamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.checklistRepository = checklistRepository;
        this.fotoOSRepository = fotoOSRepository;
    }

    public List<OrdemServicoResponseDTO> listarTodas() {
        try {
            return repository.findAllWithDetails().stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Throwable ignored) {
            return repository.findAll().stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
        }
    }

    public OrdemServicoResponseDTO buscarPorId(Long id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço não encontrada"));
        return convertToResponseDTO(os);
    }

    @Transactional
    public OrdemServico criarOrdemServico(OrdemServicoRequestDTO dto) {
        var equip = equipamentoRepository.findById(dto.equipamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Equipamento não encontrado"));

        List<Usuario> tecnicos = usuarioRepository.findAllById(
                dto.tecnicosIds() == null ? Collections.emptyList() : dto.tecnicosIds()
        );

        Checklist checklist = null;
        if (dto.checklistId() != null) {
            checklist = checklistRepository.findById(dto.checklistId()).orElse(null);
        } else if (equip != null) {
            checklist = equip.getChecklist();
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
                // garante que status não fique nulo (coluna NOT NULL no DB)
                try {
                    resultado.setStatus(ItemStatus.PENDENTE);
                } catch (Throwable ignored) {}
                try {
                    resultado.setObservacao(item.getDescricao());
                } catch (Throwable ignored) {}
                os.addResultado(resultado);
            }
        }

        return repository.save(os);
    }

    @Transactional
    public void iniciarOrdemServico(Long id) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OS não encontrada"));
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        os.setDataInicioExecucao(LocalDateTime.now());
        repository.save(os);
    }

    @Transactional
    public void finalizarOrdemServico(Long id, FinalizarOsRequestDTO dto) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordem de Serviço não encontrada"));

        if (dto.getObservacoesTecnico() != null) {
            os.setObservacoesTecnico(dto.getObservacoesTecnico());
        }

        if (dto.getResultados() != null) {
            for (ResultadoChecklistResponseDTO rDto : dto.getResultados()) {
                Long templateId = rDto.getItemTemplateId();
                if (templateId == null) continue;

                Optional<ResultadoChecklistItem> exist = os.getResultados().stream()
                        .filter(rr -> rr.getItemTemplate() != null && templateId.equals(rr.getItemTemplate().getId()))
                        .findFirst();

                if (exist.isPresent()) {
                    ResultadoChecklistItem item = exist.get();
                    if (rDto.getStatus() != null) {
                        try { item.setStatus(rDto.getStatus()); } catch (Throwable ignored) {}
                    }
                    item.setObservacao(rDto.getObservacao());
                } else {
                    ItemChecklist itemTemplate = null;
                    if (os.getChecklist() != null && os.getChecklist().getItens() != null) {
                        for (ItemChecklist it : os.getChecklist().getItens()) {
                            if (templateId.equals(it.getId())) { itemTemplate = it; break; }
                        }
                    }
                    ResultadoChecklistItem novo = new ResultadoChecklistItem();
                    novo.setItemTemplate(itemTemplate);
                    // se status vier nulo, garante valor default PENDENTE
                    try {
                        novo.setStatus(rDto.getStatus() != null ? rDto.getStatus() : ItemStatus.PENDENTE);
                    } catch (Throwable ignored) {}
                    novo.setObservacao(rDto.getObservacao());
                    os.addResultado(novo);
                }
            }
        }

        os.setDataFimExecucao(LocalDateTime.now());
        os.setStatus(StatusOrdemServico.AGUARDANDO_VALIDACAO);
        repository.save(os);
    }

    @Transactional
    public void validarOrdemServico(Long id, Long liderId, String observacoesLider) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordem de Serviço não encontrada"));

        Usuario lider = null;
        if (liderId != null) {
            lider = usuarioRepository.findById(liderId).orElse(null);
        }

        if (observacoesLider != null) os.setObservacoesLider(observacoesLider);

        os.setStatus(StatusOrdemServico.CONCLUIDA);

        // marca validação e líder (tenta via reflection se setters não existirem)
        try {
            Method setDataValidacao = os.getClass().getMethod("setDataValidacao", LocalDateTime.class);
            setDataValidacao.invoke(os, LocalDateTime.now());
        } catch (Throwable ignored) { }

        if (lider != null) {
            try {
                Method setLider = os.getClass().getMethod("setLiderValidador", Usuario.class);
                setLider.invoke(os, lider);
            } catch (Throwable ignored) { }
        }

        // Se for PREVENTIVA, atualiza a dataUltimaPreventiva do equipamento para 'agora'
        try {
            if (os.getTipoManutencao() != null && "PREVENTIVA".equalsIgnoreCase(os.getTipoManutencao().trim())) {
                var equipamento = os.getEquipamento();
                if (equipamento != null) {
                    try {
                        Method m = equipamento.getClass().getMethod("setDataUltimaPreventiva", java.time.LocalDateTime.class);
                        m.invoke(equipamento, LocalDateTime.now());
                        equipamentoRepository.save(equipamento);
                    } catch (NoSuchMethodException nsme) {
                        try {
                            Method setField = equipamento.getClass().getMethod("setDataUltimaPreventiva", java.time.LocalDate.class);
                            setField.invoke(equipamento, java.time.LocalDate.now());
                            equipamentoRepository.save(equipamento);
                        } catch (Throwable ignored) { }
                    } catch (Throwable ignored) { }
                }
            }
        } catch (Throwable ignored) { }

        repository.save(os);
    }

    @Transactional
    public void reprovarOrdemServico(Long id, Long liderId, String observacoesLider) {
        OrdemServico os = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordem de Serviço não encontrada"));

        Usuario lider = null;
        if (liderId != null) {
            lider = usuarioRepository.findById(liderId).orElse(null);
        }

        if (observacoesLider != null) os.setObservacoesLider(observacoesLider);

        os.setStatus(StatusOrdemServico.PENDENTE_DE_CORRECAO);

        try {
            Method setDataValidacao = os.getClass().getMethod("setDataValidacao", LocalDateTime.class);
            setDataValidacao.invoke(os, LocalDateTime.now());
        } catch (Throwable ignored) { }

        if (lider != null) {
            try {
                Method setLider = os.getClass().getMethod("setLiderValidador", Usuario.class);
                setLider.invoke(os, lider);
            } catch (Throwable ignored) { }
        }

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
        dto.setDataFimExecucao(os.getDataFimExecucao());

        dto.setObservacoesLider(os.getObservacoesLider());
        dto.setObservacoesTecnico(os.getObservacoesTecnico());

        if (os.getEquipamento() != null) {
            dto.setNomeEquipamento(os.getEquipamento().getNome());
            dto.setCodigoEquipamento(os.getEquipamento().getCodigo());
        }

        if (os.getTecnicosExecutores() != null && !os.getTecnicosExecutores().isEmpty()) {
            dto.setNomeTecnico(os.getTecnicosExecutores().iterator().next().getNomeCompleto());
        }

        try {
            Method getLider = os.getClass().getMethod("getLiderValidador");
            Object liderObj = getLider.invoke(os);
            if (liderObj instanceof Usuario) {
                dto.setNomeLider(((Usuario) liderObj).getNomeCompleto());
            }
        } catch (Throwable ignored) { }

        Checklist checklistToUse = os.getChecklist();
        if (checklistToUse == null && os.getEquipamento() != null) {
            checklistToUse = os.getEquipamento().getChecklist();
        }

        if (checklistToUse != null) {
            ChecklistResponseDTO chDto = new ChecklistResponseDTO();
            chDto.setId(checklistToUse.getId());
            chDto.setNome(checklistToUse.getNome());

            if (checklistToUse.getItens() != null) {
                List<ItemChecklistDTO> itensDto = checklistToUse.getItens().stream().map(item -> {
                    ItemChecklistDTO iDto = new ItemChecklistDTO();
                    iDto.setId(item.getId());
                    iDto.setDescricao(item.getDescricao());
                    return iDto;
                }).collect(Collectors.toList());
                chDto.setItens(itensDto);
            }
            dto.setChecklist(chDto);
        }

        if (os.getResultados() != null && !os.getResultados().isEmpty()) {
            List<ResultadoChecklistResponseDTO> resultados = os.getResultados().stream().map(r -> {
                ResultadoChecklistResponseDTO rr = new ResultadoChecklistResponseDTO();
                rr.setItemTemplateId(r.getItemTemplate() != null ? r.getItemTemplate().getId() : null);
                rr.setStatus(r.getStatus());
                rr.setObservacao(r.getObservacao());
                return rr;
            }).collect(Collectors.toList());
            dto.setResultados(resultados);
        }

        if (os.getId() != null && fotoOSRepository != null) {
            try {
                List<FotoOS> fotos = fotoOSRepository.findByOrdemServicoId(os.getId());
                if (fotos != null && !fotos.isEmpty()) {
                    List<String> urls = fotos.stream().map(FotoOS::getUrl).collect(Collectors.toList());
                    dto.setFotosEvidencia(urls);
                }
            } catch (Throwable ignored) { }
        }

        return dto;
    }
}