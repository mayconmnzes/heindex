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
import java.util.*;
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

        peca.setEstoqueAtual(peca.getEstoqueAtual() - dto.getQuantidade());
        pecaReposicaoRepository.save(peca);

        MovimentacaoEstoque movimento = new MovimentacaoEstoque();
        movimento.setPeca(peca);
        movimento.setQuantidade(dto.getQuantidade());
        movimento.setTipoMovimentacao("SAIDA_AVULSA");
        movimento.setTipo("SAIDA_AVULSA");
        movimento.setDataMovimentacao(LocalDateTime.now());
        movimento.setObservacao("Uso corretivo/avulso no equipamento: " + equip.getNome());
        movimento.setEquipamento(equip);

        movimentacaoEstoqueRepository.save(movimento);

        return ResponseEntity.ok("Baixa realizada com sucesso!");
    }

    /**
     * Compatibilidade: Recupera movimentações por observação (query param).
     * Se o parâmetro 'nome' for omitido ou vazio, retorna movimentos do tipo SAIDA e SAIDA_AVULSA.
     */
    @GetMapping("/historico-equipamento")
    public List<MovimentacaoEstoqueDTO> getHistoricoPorNomeEquipamento(@RequestParam(name = "nome", required = false) String nome) {
        List<MovimentacaoEstoque> movimentos;
        if (nome == null || nome.trim().isEmpty()) {
            movimentos = movimentacaoEstoqueRepository.findByTipoMovimentacaoInOrderByDataMovimentacaoDesc(
                    List.of("SAIDA", "SAIDA_AVULSA")
            );
        } else {
            movimentos = movimentacaoEstoqueRepository.findByObservacaoContainingOrderByDataMovimentacaoDesc(nome);
        }
        return movimentos.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/historico-equipamento/{equipamentoId}")
    public List<MovimentacaoEstoqueDTO> getHistoricoPorEquipamentoId(@PathVariable Long equipamentoId) {
        List<MovimentacaoEstoque> porEquipamento = movimentacaoEstoqueRepository.findByEquipamentoIdOrderByDataMovimentacaoDesc(equipamentoId);

        Optional<Equipamento> equipOpt = equipamentoRepository.findById(equipamentoId);
        List<MovimentacaoEstoque> porObservacao = List.of();
        if (equipOpt.isPresent()) {
            String nome = equipOpt.get().getNome();
            if (nome != null && !nome.trim().isEmpty()) {
                porObservacao = movimentacaoEstoqueRepository.findByObservacaoContainingOrderByDataMovimentacaoDesc(nome);
            }
        }

        Map<Long, MovimentacaoEstoque> mapa = new LinkedHashMap<>();
        for (MovimentacaoEstoque m : porEquipamento) {
            if (m != null && m.getId() != null) mapa.put(m.getId(), m);
        }
        for (MovimentacaoEstoque m : porObservacao) {
            if (m != null && m.getId() != null && !mapa.containsKey(m.getId())) mapa.put(m.getId(), m);
        }

        List<MovimentacaoEstoque> unidos = new ArrayList<>(mapa.values());
        unidos.sort(Comparator.comparing(MovimentacaoEstoque::getDataMovimentacao, Comparator.nullsLast(Comparator.reverseOrder())));

        return unidos.stream().map(this::toDto).collect(Collectors.toList());
    }

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
        movimento.setTipo("SAIDA");
        movimento.setDataMovimentacao(LocalDateTime.now());

        movimento.setOrdemServico(os);
        if (os.getEquipamento() != null) movimento.setEquipamento(os.getEquipamento());

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

    // Helper
    private MovimentacaoEstoqueDTO toDto(MovimentacaoEstoque mov) {
        MovimentacaoEstoqueDTO dto = new MovimentacaoEstoqueDTO();
        dto.setId(mov.getId());
        dto.setPecaId(mov.getPeca() != null ? mov.getPeca().getId() : null);
        dto.setQuantidade(mov.getQuantidade());
        dto.setTipoMovimentacao(mov.getTipoMovimentacao() != null ? mov.getTipoMovimentacao() : mov.getTipo());
        dto.setTipo(mov.getTipo());
        dto.setDataMovimentacao(mov.getDataMovimentacao());
        dto.setNomePeca(mov.getPeca() != null ? mov.getPeca().getNome() : null);
        dto.setNomeEquipamento(mov.getEquipamento() != null ? mov.getEquipamento().getNome() : null);
        dto.setEquipamentoId(mov.getEquipamento() != null ? mov.getEquipamento().getId() : null);
        dto.setLoginUsuario(mov.getUsuario() != null ? mov.getUsuario().getLogin() : null);
        dto.setObservacao(mov.getObservacao());
        return dto;
    }
}