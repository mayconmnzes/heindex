package br.com.heimdex.service;

import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.EquipamentoRepository;
import br.com.heimdex.repository.OrdemServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoPreventivaService {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoPreventivaService.class);

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    // Roda todo dia à 1 da manhã
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void gerarSugestoesPreventivas() {
        log.info("Iniciando verificação de preventivas agendadas...");
        
        // ✅ CORREÇÃO RADICAL: Busca todos os equipamentos sem filtrar por checklist
        // Já que o campo getChecklist() não existe no seu Model Equipamento
        List<Equipamento> todosEquipamentos = equipamentoRepository.findAll();

        int sugestoesGeradas = 0;
        for (Equipamento equipamento : todosEquipamentos) {
            
            // Verifica se já não existe uma OS ativa para evitar duplicados
            boolean osAtivaExiste = ordemServicoRepository.existsByEquipamentoIdAndTipoManutencaoAndStatusIn(
                    equipamento.getId(),
                    "PREVENTIVA",
                    List.of(StatusOrdemServico.SUGESTAO, StatusOrdemServico.AGENDADA, StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.PENDENTE_DE_CORRECAO)
            );

            if (!osAtivaExiste) {
                log.info("Gerando sugestão de OS Preventiva para equipamento: {}", equipamento.getNome());

                OrdemServico novaOs = new OrdemServico();
                novaOs.setEquipamento(equipamento);
                
                // ✅ IMPORTANTE: Deixamos o checklist como nulo aqui para não dar erro de compilação.
                // Você vinculará o checklist manualmente no Planejamento.jsx que já corrigimos.
                novaOs.setChecklist(null); 
                
                novaOs.setTipoManutencao("PREVENTIVA");
                novaOs.setStatus(StatusOrdemServico.SUGESTAO);
                novaOs.setDataAgendamento(LocalDateTime.now());

                ordemServicoRepository.save(novaOs);
                sugestoesGeradas++;
            }
        }
        log.info("Verificação concluída. {} sugestões geradas.", sugestoesGeradas);
    }
}