package br.com.heimdex.migration;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.service.PecaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FillCodigoControleRunner implements CommandLineRunner {

    private final PecaReposicaoRepository repo;
    private final PecaService service;
    private final Logger log = LoggerFactory.getLogger(FillCodigoControleRunner.class);

    public FillCodigoControleRunner(PecaReposicaoRepository repo, PecaService service) {
        this.repo = repo;
        this.service = service;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // EXECUTE APENAS UMA VEZ: depois de rodar, remova ou comente esta classe.
        List<PecaReposicao> lista = repo.findAll(Sort.by("id")).stream()
                .filter(p -> p.getCodigoControle() == null || p.getCodigoControle().trim().isEmpty())
                .collect(Collectors.toList());

        log.info("FillCodigoControleRunner: {} registros a atualizar", lista.size());

        int updated = 0;
        for (PecaReposicao p : lista) {
            String novo = service.gerarProximoCodigoControle();
            p.setCodigoControle(novo);
            repo.save(p);
            log.info("Atualizado id={} -> codigo_controle={}", p.getId(), novo);
            updated++;
        }
        log.info("FillCodigoControleRunner: finalizado. {} registros atualizados.", updated);
    }
}