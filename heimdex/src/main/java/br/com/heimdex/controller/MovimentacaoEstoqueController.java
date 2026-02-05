package br.com.heimdex.controller;

import br.com.heimdex.dto.MovimentacaoEstoqueDTO;
import br.com.heimdex.model.*;
import br.com.heimdex.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estoque")
public class MovimentacaoEstoqueController {

    @Autowired private PecaReposicaoRepository pecaReposicaoRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    @Autowired private OrdemServicoRepository osRepository;
    @Autowired private PecaBaixadaOSRepository pecaBaixadaOSRepository;
    @Autowired private EquipamentoRepository equipamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    /**
     * Realiza a baixa de uma peça sem OS, vinculando-a a um equipamento específico.
     * Salva o nome do equipamento na observação para consulta posterior no histórico.
     */
    @PostMapping("/saida-avulsa/{equipamentoId}")
    @Transactional
    public ResponseEntity<?> saidaEstoqueAvulsa(@PathVariable Long equipamentoId, @RequestBody MovimentacaoEstoqueDTO dto) {

        Equipamento equip = equipamentoRepository.findById(equipamentoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado."));

        PecaReposicao peca = pecaReposicaoRepository.findById(dto.getPecaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peça não encontrada."));

        if (peca.getEstoqueAtual() < dto.getQuantidade()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estoque insuficiente.");
        }

        // 1. Atualiza estoque físico da peça
        peca.setEstoqueAtual(peca.getEstoqueAtual() - dto.getQuantidade());
        pecaReposicaoRepository.save(peca);

        // 2. Registra a movimentação no histórico
        MovimentacaoEstoque movimento = new MovimentacaoEstoque();
        movimento.setPeca(peca);
        movimento.setQuantidade(dto.getQuantidade());
        movimento.setTipoMovimentacao("SAIDA_AVULSA");
        movimento.setDataMovimentacao(LocalDateTime.now());
        movimento.setObservacao("Uso corretivo/avulso no equipamento: " + equip.getNome());

        // IMPORTANTE: vincula também o equipamento ao movimento (para buscas por equipamentoId)
        movimento.setEquipamento(equip);

        movimentacaoEstoqueRepository.save(movimento);

        return ResponseEntity.ok("Baixa realizada com sucesso!");
    }

    /**
     * Recupera todas as movimentações vinculadas ao equipamento pelo ID (mais robusto).
     */
    @GetMapping("/historico-equipamento/{equipamentoId}")
    public List<MovimentacaoEstoqueDTO> getHistoricoPorEquipamentoId(@PathVariable Long equipamentoId) {
        return movimentacaoEstoqueRepository.findByEquipamentoIdOrderByDataMovimentacaoDesc(equipamentoId)
                .stream()
                .map(mov -> {
                    MovimentacaoEstoqueDTO dto = new MovimentacaoEstoqueDTO();
                    dto.setId(mov.getId());
                    dto.setPecaId(mov.getPeca() != null ? mov.getPeca().getId() : null);
                    dto.setQuantidade(mov.getQuantidade());
                    dto.setTipoMovimentacao(mov.getTipoMovimentacao());
                    dto.setDataMovimentacao(mov.getDataMovimentacao());
                    dto.setNomePeca(mov.getPeca() != null ? mov.getPeca().getNome() : "N/A");
                    dto.setNomeEquipamento(mov.getEquipamento() != null ? mov.getEquipamento().getNome() : "Geral");
                    dto.setLoginUsuario(mov.getUsuario() != null ? mov.getUsuario().getLogin() : "Sistema");
                    dto.setObservacao(mov.getObservacao());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Baixa via Ordem de Serviço (utilizada pelo App Mobile).
     */
    @PostMapping("/saida/{osId}")
    @Transactional
    public ResponseEntity<?> saidaEstoqueVinculada(@PathVariable Long osId, @RequestBody MovimentacaoEstoqueDTO dto) {

        OrdemServico os = osRepository.findById(osId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS não encontrada."));

        PecaReposicao peca = pecaReposicaoRepository.findById(dto.getPecaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peça não encontrada."));

        if (peca.getEstoqueAtual() < dto.getQuantidade()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estoque insuficiente.");
        }

        peca.setEstoqueAtual(peca.getEstoqueAtual() - dto.getQuantidade());
        pecaReposicaoRepository.save(peca);

        MovimentacaoEstoque movimento = new MovimentacaoEstoque();
        movimento.setPeca(peca);
        movimento.setQuantidade(dto.getQuantidade());
        movimento.setTipoMovimentacao("SAIDA");
        movimento.setDataMovimentacao(LocalDateTime.now());

        // vincula a OS e equipamento (se disponível) ao movimento
        movimento.setOrdemServico(os);
        if (os.getEquipamento() != null) {
            movimento.setEquipamento(os.getEquipamento());
        }

        // opcional: Observação indicando qual OS gerou a saída
        movimento.setObservacao("Baixa vinculada à OS #" + osId);

        MovimentacaoEstoque movimentoSalvo = movimentacaoEstoqueRepository.save(movimento);

        PecaBaixadaOS pecaConsumida = new PecaBaixadaOS();
        pecaConsumida.setOrdemServico(os);
        pecaConsumida.setPeca(peca);
        pecaConsumida.setQuantidadeBaixada(dto.getQuantidade());
        pecaConsumida.setMovimentacao(movimentoSalvo);
        pecaBaixadaOSRepository.save(pecaConsumida);

        return ResponseEntity.ok(osId);
    }
}