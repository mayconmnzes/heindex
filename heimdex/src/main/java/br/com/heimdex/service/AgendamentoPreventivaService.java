// Código Completo
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

    // Roda todo dia à 1 da manhã (segundo minuto hora dia mes dia-semana)
    @Scheduled(cron = "0 0 1 * * ?")
    // @Scheduled(cron = "*/30 * * * * *") // Para testes rápidos a cada 30 seg
    @Transactional
    public void gerarSugestoesPreventivas() {
        log.info("Iniciando verificação de preventivas agendadas...");
        LocalDate hoje = LocalDate.now();
        int sugestoesGeradas = 0;

        // 1. Busca todos os equipamentos que TEM frequência e data da última preventiva
        List<Equipamento> equipamentosComPreventiva = equipamentoRepository.findAll().stream()
                .filter(e -> e.getFrequenciaPreventiva() != null && e.getDataUltimaPreventiva() != null)
                .toList();

        for (Equipamento equipamento : equipamentosComPreventiva) {
            LocalDate proximaPreventiva = equipamento.getDataUltimaPreventiva()
                    .plusDays(equipamento.getFrequenciaPreventiva().getDias());

            // 2. Verifica se a próxima preventiva está vencida ou vence hoje
            if (!proximaPreventiva.isAfter(hoje)) {

                // 3. Verifica se JÁ NÃO EXISTE uma OS de preventiva "ativa" (Sugestão, Agendada, Em Execução, Pendente)
                boolean osPendenteJaExiste = ordemServicoRepository.existsByEquipamentoIdAndTipoManutencaoAndStatusIn(
                        equipamento.getId(),
                        "PREVENTIVA",
                        List.of(StatusOrdemServico.SUGESTAO, StatusOrdemServico.AGENDADA, StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.PENDENTE_DE_CORRECAO)
                );

                if (!osPendenteJaExiste) {
                    log.info("Gerando sugestão de OS Preventiva para equipamento ID: {}", equipamento.getId());

                    // 4. Cria a nova OS como SUGESTAO
                    OrdemServico novaOs = new OrdemServico();
                    novaOs.setEquipamento(equipamento);
                    novaOs.setChecklist(equipamento.getChecklistPadrao()); // Associa checklist padrão
                    novaOs.setTipoManutencao("PREVENTIVA");
                    novaOs.setStatus(StatusOrdemServico.SUGESTAO);
                    // Define a data de agendamento sugerida
                    novaOs.setDataAgendamento(proximaPreventiva.isBefore(hoje) ? hoje.atStartOfDay() : proximaPreventiva.atStartOfDay());
                    // Técnico será definido posteriormente pelo planejador

                    ordemServicoRepository.save(novaOs);
                    sugestoesGeradas++;
                }
            }
        }
        log.info("Verificação de preventivas concluída. {} sugestões geradas.", sugestoesGeradas);
    }
}