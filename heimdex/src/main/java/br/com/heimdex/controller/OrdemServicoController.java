package br.com.heimdex.controller;

import br.com.heimdex.dto.*;
import br.com.heimdex.model.*;
import br.com.heimdex.model.enums.ItemStatus;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    @Autowired private OrdemServicoRepository osRepository;
    @Autowired private EquipamentoRepository equipamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ChecklistRepository checklistRepository;
    @Autowired private ResultadoChecklistItemRepository resultadoRepository;
    @Autowired private PecaReposicaoRepository pecaReposicaoRepository;
    @Autowired private PecaBaixadaOSRepository pecaBaixadaOSRepository; // Adicionado para histórico

    @PostMapping
    @Transactional
    public ResponseEntity<?> criarOrdemServico(@RequestBody OrdemServicoRequestDTO dto) {
        Equipamento equipamento = equipamentoRepository.findById(dto.getEquipamentoId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipamento não encontrado."));
        
        Usuario tecnico = null;
        if (dto.getTecnicoId() != null) {
             tecnico = usuarioRepository.findById(dto.getTecnicoId())
                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Técnico não encontrado."));
        }

        Checklist checklist = null;
        if (dto.getChecklistId() != null) {
            checklist = checklistRepository.findById(dto.getChecklistId()).orElse(null);
            if (checklist == null) {
                return ResponseEntity.badRequest().body("Checklist com ID " + dto.getChecklistId() + " não encontrado.");
            }
        }
        
        if (dto.getDataAgendamento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data de agendamento é obrigatória.");
        }

        OrdemServico novaOs = new OrdemServico();
        novaOs.setEquipamento(equipamento);
        novaOs.setTecnicoExecutor(tecnico);
        novaOs.setChecklist(checklist);
        novaOs.setTipoManutencao(dto.getTipoManutencao());
        novaOs.setDataAgendamento(dto.getDataAgendamento());
        novaOs.setStatus(tecnico != null ? StatusOrdemServico.AGENDADA : StatusOrdemServico.SUGESTAO);

        OrdemServico osSalva = osRepository.save(novaOs);
        return new ResponseEntity<>(convertToResponseDTO(osSalva), HttpStatus.CREATED);
    }

    // CORREÇÃO APLICADA: Agora aceita equipamentoId para o histórico individual
    @GetMapping
    public List<OrdemServicoResponseDTO> listarOrdensServico(
            @RequestParam(required = false) StatusOrdemServico status,
            @RequestParam(required = false) Long equipamentoId) {
        
        List<OrdemServico> ordens;

        // PRIORIDADE: Se vier equipamentoId, filtra apenas por ele
        if (equipamentoId != null) {
            System.out.println("DEBUG: Filtrando OS para o Equipamento ID: " + equipamentoId);
            ordens = osRepository.findByEquipamentoIdOrderByDataAgendamentoDesc(equipamentoId);
        } else if (status != null) {
            ordens = osRepository.findByStatus(status);
        } else {
            ordens = osRepository.findAllByOrderByDataAgendamentoAsc();
        }
        
        return ordens.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoResponseDTO> buscarOsPorId(@PathVariable Long id) {
        return osRepository.findOsByIdWithDetails(id)
                .map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/iniciar")
    @Transactional
    public ResponseEntity<?> iniciarOs(@PathVariable Long id) {
        return osRepository.findById(id).map(os -> {
            if (os.getStatus() != StatusOrdemServico.AGENDADA && os.getStatus() != StatusOrdemServico.PENDENTE_DE_CORRECAO) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A OS não está Agendada ou Pendente de Correção.");
            }
            if (os.getTecnicoExecutor() == null) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Técnico não atribuído.");
            }
            os.setStatus(StatusOrdemServico.EM_EXECUCAO);
            os.setDataInicioExecucao(LocalDateTime.now());
            return ResponseEntity.ok(convertToResponseDTO(osRepository.save(os)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/confirmar-agendamento")
    @Transactional
    public ResponseEntity<OrdemServicoResponseDTO> confirmarAgendamento(
            @PathVariable Long id,
            @RequestBody @Valid AgendamentoConfirmacaoDTO dto) {

        return osRepository.findById(id).map(os -> {
            if (os.getStatus() != StatusOrdemServico.SUGESTAO) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta OS não é uma sugestão pendente.");
            }
            Usuario tecnico = usuarioRepository.findById(dto.getTecnicoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Técnico não encontrado."));

            os.setTecnicoExecutor(tecnico);
            os.setDataAgendamento(dto.getDataAgendamento());
            os.setStatus(StatusOrdemServico.AGENDADA);

            OrdemServico osConfirmada = osRepository.save(os);
            return ResponseEntity.ok(convertToResponseDTO(osConfirmada));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/finalizar")
    @Transactional
    public ResponseEntity<?> finalizarOs(@PathVariable Long id, @RequestBody FinalizacaoOsDTO dto) {
         return osRepository.findById(id).map(os -> {
            if (os.getStatus() != StatusOrdemServico.EM_EXECUCAO && os.getStatus() != StatusOrdemServico.PENDENTE_DE_CORRECAO) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A OS não está em execução ou pendente.");
            }

            resultadoRepository.deleteByOrdemServicoId(os.getId());
            if (os.getResultados() != null) {
                os.getResultados().clear();
            } else {
                os.setResultados(new java.util.HashSet<>());
            }

            Set<ItemChecklist> checklistItens = os.getChecklist() != null ? os.getChecklist().getItens() : null;
            if (checklistItens != null && !checklistItens.isEmpty()) {
                 if (dto.getResultados() == null) {
                     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resultados do checklist são obrigatórios.");
                 }

                List<ResultadoChecklistItem> novosResultados = dto.getResultados().stream().map(resultadoDto -> {
                    ResultadoChecklistItem resultado = new ResultadoChecklistItem();
                    ItemChecklist itemTemplate = checklistItens.stream()
                        .filter(item -> item.getId().equals(resultadoDto.getItemTemplateId()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Item de checklist inválido."));
                    resultado.setItemTemplate(itemTemplate);
                    resultado.setOrdemServico(os);
                    resultado.setStatus(resultadoDto.getStatus() != null ? resultadoDto.getStatus() : ItemStatus.PENDENTE);
                    resultado.setObservacao(resultadoDto.getObservacao());
                    return resultado;
                }).collect(Collectors.toList());
                 os.getResultados().addAll(novosResultados);
            }

            os.setStatus(StatusOrdemServico.AGUARDANDO_VALIDACAO);
            os.setDataFimExecucao(LocalDateTime.now());
            os.setObservacoesTecnico(dto.getObservacoesTecnico());

            OrdemServico osSalva = osRepository.save(os);
            return ResponseEntity.ok(convertToResponseDTO(osSalva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/validar")
    @Transactional
    public ResponseEntity<?> validarOs(@PathVariable Long id, @RequestBody LiderActionDTO dto) {
        return osRepository.findById(id).map(os -> {
            if (os.getStatus() != StatusOrdemServico.AGUARDANDO_VALIDACAO) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A OS não está aguardando validação.");
            }
            Usuario lider = usuarioRepository.findById(dto.getLiderId())
                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Líder não encontrado."));

            os.setStatus(StatusOrdemServico.CONCLUIDA);
            os.setDataValidacao(LocalDateTime.now());
            os.setLiderValidador(lider);
            os.setObservacoesLider(dto.getObservacoesLider());

            if ("PREVENTIVA".equals(os.getTipoManutencao())) {
                Equipamento equipamento = os.getEquipamento();
                if (equipamento != null && os.getDataFimExecucao() != null) {
                    equipamento.setDataUltimaPreventiva(os.getDataFimExecucao().toLocalDate());
                    equipamentoRepository.save(equipamento);
                }
            }
            return ResponseEntity.ok(convertToResponseDTO(osRepository.save(os)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reprovar")
    @Transactional
    public ResponseEntity<?> reprovarOs(@PathVariable Long id, @RequestBody LiderActionDTO dto) {
        return osRepository.findById(id).map(os -> {
             if (os.getStatus() != StatusOrdemServico.AGUARDANDO_VALIDACAO) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A OS não está aguardando validação.");
            }
             if (dto.getObservacoesLider() == null || dto.getObservacoesLider().trim().isEmpty()) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Observações do líder são obrigatórias para reprovar.");
             }
            Usuario lider = usuarioRepository.findById(dto.getLiderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Líder não encontrado."));

            os.setStatus(StatusOrdemServico.PENDENTE_DE_CORRECAO);
            os.setLiderValidador(lider);
            os.setObservacoesLider(dto.getObservacoesLider());
            os.setDataValidacao(null);
            return ResponseEntity.ok(convertToResponseDTO(osRepository.save(os)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/fotos")
    @Transactional
    public ResponseEntity<?> adicionarFotoEvidencia(@PathVariable Long id, @RequestBody String fotoUrl) {
        return osRepository.findById(id).map(os -> {
            FotoOS novaFoto = new FotoOS();
            novaFoto.setOrdemServico(os);
            String cleanUrl = fotoUrl != null ? fotoUrl.trim().replaceAll("^\"|\"$", "") : "";
            if (cleanUrl.isEmpty()) {
                 return ResponseEntity.badRequest().body("URL da foto não pode ser vazia.");
            }
            novaFoto.setUrl(cleanUrl);
            if (os.getFotosEvidencia() == null) {
                os.setFotosEvidencia(new java.util.HashSet<>());
            }
            os.getFotosEvidencia().add(novaFoto);
            osRepository.save(os);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private PecaReposicaoResponseDTO convertPecaToDTO(PecaReposicao peca) {
        if (peca == null) return null;
        PecaReposicaoResponseDTO dto = new PecaReposicaoResponseDTO();
        dto.setId(peca.getId());
        dto.setNome(peca.getNome());
        dto.setCodigoControle(peca.getCodigoControle());
        if (peca.getModeloEquipamento() != null) {
            dto.setModeloEquipamentoId(peca.getModeloEquipamento().getId());
            dto.setNomeModeloEquipamento(peca.getModeloEquipamento().getNome());
            dto.setFabricanteModeloEquipamento(peca.getModeloEquipamento().getFabricante());
        }
        return dto;
    }

    // CONVERSOR COMPLETO COM TODA A SUA LÓGICA DE CHECKLIST, FOTOS E RESULTADOS
    public OrdemServicoResponseDTO convertToResponseDTO(OrdemServico os) {
        if (os == null) return null;

        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO();
        dto.setId(os.getId());
        dto.setStatus(os.getStatus());
        dto.setTipoManutencao(os.getTipoManutencao());
        dto.setDataAgendamento(os.getDataAgendamento());
        dto.setDataInicioExecucao(os.getDataInicioExecucao());
        dto.setDataFimExecucao(os.getDataFimExecucao());
        dto.setDataValidacao(os.getDataValidacao());
        dto.setObservacoesTecnico(os.getObservacoesTecnico());
        dto.setObservacoesLider(os.getObservacoesLider());

        if (os.getEquipamento() != null) {
            dto.setNomeEquipamento(os.getEquipamento().getNome());
            dto.setCodigoEquipamento(os.getEquipamento().getCodigo());
        }
        if (os.getTecnicoExecutor() != null) {
            dto.setNomeTecnico(os.getTecnicoExecutor().getNomeCompleto());
        }
        if (os.getLiderValidador() != null) {
             dto.setNomeLider(os.getLiderValidador().getNomeCompleto());
        }

        if (os.getChecklist() != null) {
            ChecklistResponseDTO checklistDTO = new ChecklistResponseDTO();
            checklistDTO.setId(os.getChecklist().getId());
            checklistDTO.setNome(os.getChecklist().getNome());
            if (os.getChecklist().getArea() != null) {
                checklistDTO.setAreaNome(os.getChecklist().getArea().getNome());
            }
             Set<ItemChecklist> checklistItens = os.getChecklist().getItens() != null ? os.getChecklist().getItens() : Collections.emptySet();
             if (!checklistItens.isEmpty()) {
                List<ItemChecklistDTO> itensDto = checklistItens.stream().map(item -> {
                    ItemChecklistDTO itemDto = new ItemChecklistDTO();
                    itemDto.setId(item.getId());
                    itemDto.setDescricao(item.getDescricao());
                     Set<PecaReposicao> pecasSugeridas = item.getPecasSugeridas() != null ? item.getPecasSugeridas() : Collections.emptySet();
                     if (!pecasSugeridas.isEmpty()) {
                        itemDto.setPecasSugeridas(pecasSugeridas.stream()
                            .map(this::convertPecaToDTO)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                    } else {
                         itemDto.setPecasSugeridas(Collections.emptyList());
                    }
                    return itemDto;
                }).collect(Collectors.toList());
                checklistDTO.setItens(itensDto);
            } else {
                 checklistDTO.setItens(Collections.emptyList());
            }
            dto.setChecklist(checklistDTO);
        }

        Set<ResultadoChecklistItem> resultados = os.getResultados() != null ? os.getResultados() : Collections.emptySet();
        if (!resultados.isEmpty()) {
            dto.setResultados(resultados.stream().map(resultado -> {
                ResultadoChecklistResponseDTO resDto = new ResultadoChecklistResponseDTO();
                if (resultado.getItemTemplate() != null) {
                    resDto.setItemTemplateId(resultado.getItemTemplate().getId());
                }
                resDto.setStatus(resultado.getStatus());
                resDto.setObservacao(resultado.getObservacao());
                return resDto;
            }).collect(Collectors.toList()));
        } else {
             dto.setResultados(Collections.emptyList());
        }

        Set<FotoOS> fotosEvidencia = os.getFotosEvidencia() != null ? os.getFotosEvidencia() : Collections.emptySet();
        if (!fotosEvidencia.isEmpty()) {
            dto.setFotosEvidencia(fotosEvidencia.stream()
                .map(FotoOS::getUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        } else {
            dto.setFotosEvidencia(Collections.emptyList());
        }

        // POPULA PEÇAS CONSUMIDAS PARA O HISTÓRICO
        List<PecaBaixadaOS> pecasBaixadas = pecaBaixadaOSRepository.findByOrdemServicoId(os.getId());
        if (pecasBaixadas != null && !pecasBaixadas.isEmpty()) {
            dto.setPecasConsumidas(pecasBaixadas.stream()
                .filter(pb -> pb.getPeca() != null)
                .map(pb -> {
                    PecaConsumidaOsDTO pecaDto = new PecaConsumidaOsDTO();
                    pecaDto.setPecaId(pb.getPeca().getId());
                    pecaDto.setNomePeca(pb.getPeca().getNome());
                    pecaDto.setCodigoControle(pb.getPeca().getCodigoControle());
                    pecaDto.setQuantidadeBaixada(pb.getQuantidadeBaixada());
                    pecaDto.setDataBaixa(pb.getDataBaixa());
                    return pecaDto;
                })
                .collect(Collectors.toList()));
        } else {
            dto.setPecasConsumidas(Collections.emptyList());
        }

        return dto;
    }

    public static class AgendamentoConfirmacaoDTO {
         @NotNull(message = "ID do técnico é obrigatório")
         private Long tecnicoId;
         @NotNull(message = "Data de agendamento é obrigatória")
         private LocalDateTime dataAgendamento;

         public Long getTecnicoId() { return tecnicoId; }
         public void setTecnicoId(Long tecnicoId) { this.tecnicoId = tecnicoId; }
         public LocalDateTime getDataAgendamento() { return dataAgendamento; }
         public void setDataAgendamento(LocalDateTime dataAgendamento) { this.dataAgendamento = dataAgendamento; }
    }
}