// Código Completo
package br.com.heimdex.controller;

import br.com.heimdex.dto.ConsumoPorEquipamentoDTO;
import br.com.heimdex.dto.CurvaAbcDTO;
import br.com.heimdex.dto.PecaReposicaoResponseDTO;
import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.ModeloEquipamento; // NOVO IMPORT
import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.OrdemServicoRepository;
import br.com.heimdex.repository.PecaBaixadaOSRepository;
import br.com.heimdex.repository.PecaReposicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired private PecaReposicaoRepository pecaReposicaoRepository;
    @Autowired private PecaBaixadaOSRepository pecaBaixadaOSRepository;
    @Autowired private OrdemServicoRepository ordemServicoRepository;

    // --- ENDPOINTS DE KPI (sem alteração) ---
    @GetMapping("/kpi/mttr")
    public ResponseEntity<Double> getMttrUltimos30Dias() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);
        List<OrdemServico> ossCorretivas = ordemServicoRepository
            .findAllByStatusAndTipoManutencaoAndDataFimExecucaoIsNotNullAndDataInicioExecucaoIsNotNullAndDataFimExecucaoBetween(
                StatusOrdemServico.CONCLUIDA, "CORRETIVA", start, end
            );
        OptionalDouble avgMinutes = ossCorretivas.stream()
            .mapToLong(os -> Duration.between(os.getDataInicioExecucao(), os.getDataFimExecucao()).toMinutes())
            .average();
        double avgHours = avgMinutes.isPresent() ? avgMinutes.getAsDouble() / 60.0 : 0.0;
        return ResponseEntity.ok(avgHours);
    }
    @GetMapping("/kpi/os-abertas-mes")
    public ResponseEntity<Long> getOsAbertasMesAtual() {
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime endOfMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59);
        long count = ordemServicoRepository.countByDataAgendamentoBetween(startOfMonth, endOfMonth);
        return ResponseEntity.ok(count);
    }
    // --- FIM DOS ENDPOINTS DE KPI ---


    // --- Endpoint para Consumo de Peças por Equipamento (ATUALIZADO) ---
    @GetMapping("/consumo-por-equipamento")
    public ResponseEntity<List<ConsumoPorEquipamentoDTO>> gerarConsumoPorEquipamento() {
        // 1. Busca os dados brutos (agora incluindo o Modelo)
        List<PecaBaixadaOSRepository.ConsumoEquipamentoPeca> consumoBruto = pecaBaixadaOSRepository.findConsumoTotalPorEquipamentoEPeca();

        // 2. Agrupa pelo Equipamento (Instância/Tag)
        Map<Equipamento, List<PecaBaixadaOSRepository.ConsumoEquipamentoPeca>> consumoAgrupado = consumoBruto.stream()
                .collect(Collectors.groupingBy(PecaBaixadaOSRepository.ConsumoEquipamentoPeca::equipamento));

        // 3. Mapeia para o DTO de resposta
        List<ConsumoPorEquipamentoDTO> resultadoFinal = consumoAgrupado.entrySet().stream()
            .map(entry -> {
                Equipamento equipamento = entry.getKey();
                List<PecaBaixadaOSRepository.ConsumoEquipamentoPeca> consumoDoEquipamento = entry.getValue();

                // Pega o modelo (deve ser o mesmo para todos os itens, pegamos o primeiro)
                ModeloEquipamento modelo = consumoDoEquipamento.isEmpty() ? null : consumoDoEquipamento.get(0).modelo();

                ConsumoPorEquipamentoDTO dto = new ConsumoPorEquipamentoDTO();
                dto.setEquipamentoId(equipamento.getId());
                dto.setNomeEquipamento(equipamento.getNome()); // Nome (Tag)
                dto.setCodigoEquipamento(equipamento.getCodigo());

                // --- ALTERAÇÃO: Adiciona dados do Modelo ---
                if (modelo != null) {
                    dto.setNomeModelo(modelo.getNome());
                    dto.setFabricanteModelo(modelo.getFabricante());
                }
                // --- FIM ALTERAÇÃO ---

                List<ConsumoPorEquipamentoDTO.PecaConsumidaInfo> pecasInfoList = consumoDoEquipamento.stream()
                    .map(consumo -> {
                        ConsumoPorEquipamentoDTO.PecaConsumidaInfo pecaInfo = new ConsumoPorEquipamentoDTO.PecaConsumidaInfo();
                        pecaInfo.setPecaId(consumo.peca().getId());
                        pecaInfo.setNomePeca(consumo.peca().getNome());
                        pecaInfo.setCodigoControle(consumo.peca().getCodigoControle());
                        pecaInfo.setTotalConsumido(consumo.total());
                        return pecaInfo;
                    })
                    .collect(Collectors.toList());

                dto.setPecasConsumidas(pecasInfoList);
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(resultadoFinal);
    }

    // --- Endpoint Curva ABC (sem alteração) ---
    @GetMapping("/curva-abc")
    public ResponseEntity<List<CurvaAbcDTO>> gerarCurvaAbc() {
        // ... (lógica inalterada) ...
        List<PecaBaixadaOSRepository.ConsumoPeca> consumoTotal = pecaBaixadaOSRepository.findConsumoTotalPorPeca();
        long grandTotal = consumoTotal.stream().mapToLong(PecaBaixadaOSRepository.ConsumoPeca::total).sum();
        if (grandTotal == 0) return ResponseEntity.ok(new ArrayList<>());
        List<CurvaAbcDTO> curvaAbc = new ArrayList<>();
        double percentualAcumulado = 0.0;
        for (PecaBaixadaOSRepository.ConsumoPeca consumo : consumoTotal) {
            PecaReposicao peca = consumo.peca();
            long totalConsumido = consumo.total();
            double percentualTotal = (double) totalConsumido / grandTotal * 100.0;
            percentualAcumulado += percentualTotal;
            String classificacao = (percentualAcumulado <= 80.0) ? "A" : (percentualAcumulado <= 95.0) ? "B" : "C";
            curvaAbc.add(new CurvaAbcDTO(
                peca.getId(), peca.getNome(), peca.getCodigoControle(),
                totalConsumido, percentualTotal, percentualAcumulado, classificacao
            ));
        }
        return ResponseEntity.ok(curvaAbc);
    }

    // --- Endpoint Estoque Baixo (sem alteração na lógica, mas usa DTO atualizado) ---
    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<PecaReposicaoResponseDTO>> listarPecasAbaixoMinimo() {
        List<PecaReposicaoResponseDTO> pecasBaixas = pecaReposicaoRepository.findAll().stream()
                .filter(peca -> peca.getEstoqueAtual() <= peca.getEstoqueMinimo())
                .map(this::convertToResponseDTO) // AGORA USA O MÉTODO ATUALIZADO
                .collect(Collectors.toList());
        return ResponseEntity.ok(pecasBaixas);
    }

    // --- MÉTODO DE CONVERSÃO ATUALIZADO ---
    // Converte PecaReposicao para DTO, usando os dados do Modelo
    private PecaReposicaoResponseDTO convertToResponseDTO(PecaReposicao peca) {
        PecaReposicaoResponseDTO dto = new PecaReposicaoResponseDTO();
        dto.setId(peca.getId());
        dto.setCodigoControle(peca.getCodigoControle());
        dto.setNome(peca.getNome());
        dto.setEstoqueAtual(peca.getEstoqueAtual());
        dto.setEstoqueMinimo(peca.getEstoqueMinimo());
        dto.setCodigoRequisicao(peca.getCodigoRequisicao());
        dto.setDescricaoTecnica(peca.getDescricaoTecnica());
        dto.setAplicacao(peca.getAplicacao());
        dto.setLocalizacaoPrateleira(peca.getLocalizacaoPrateleira());
        dto.setFotoUrl(peca.getFotoUrl());

        // --- ALTERAÇÃO: Pega informações do Modelo associado ---
        if (peca.getModeloEquipamento() != null) {
            dto.setModeloEquipamentoId(peca.getModeloEquipamento().getId());
            // VERIFIQUE AQUI SE NÃO HÁ ERRO DE DIGITAÇÃO:
            dto.setNomeModeloEquipamento(peca.getModeloEquipamento().getNome());
            // VERIFIQUE AQUI SE NÃO HÁ ERRO DE DIGITAÇÃO:
            dto.setFabricanteModeloEquipamento(peca.getModeloEquipamento().getFabricante());
        }
        // --- FIM DA ALTERAÇÃO ---

        return dto;
    }
}