package br.com.heimdex.migration;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.service.PecaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("migration")
public class FillCodigoControleRunner implements CommandLineRunner {

    private final PecaReposicaoRepository repo;
    private final PecaService service;
    private final Logger log = LoggerFactory.getLogger(FillCodigoControleRunner.class);

    public FillCodigoControleRunner(PecaReposicaoRepository repo, PecaService service) {
        this.repo = repo;
        this.service = service;
    }

    private boolean isInvalid(String s) {
        if (s == null) return true;
        String t = s.trim();
        if (t.isEmpty()) return true;
        if (t.equalsIgnoreCase("null") || t.equalsIgnoreCase("undefined")) return true;
        return false;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // EXECUTE APENAS UMA VEZ: depois de rodar, remova ou comente esta classe (@Component).
        List<PecaReposicao> lista = repo.findAll(Sort.by("id")).stream()
                .filter(p -> isInvalid(p.getCodigoControle()))
                .collect(Collectors.toList());

        log.info("FillCodigoControleRunner: {} registros inválidos a atualizar", lista.size());

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