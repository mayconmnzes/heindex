package br.com.heimdex.migration;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.service.QrCodeService;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Component
@Profile("migration")
public class RegenerateQrRunner implements CommandLineRunner {

    private final PecaReposicaoRepository repo;
    private final QrCodeService qrService;
    private final Logger log = LoggerFactory.getLogger(RegenerateQrRunner.class);

    public RegenerateQrRunner(PecaReposicaoRepository repo, QrCodeService qrService) {
        this.repo = repo;
        this.qrService = qrService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // EXECUTE APENAS UMA VEZ: depois comente/remova @Component para não rodar em todos os starts.
        List<PecaReposicao> list = repo.findAll(Sort.by("id"));
        int count = 0;
        for (PecaReposicao p : list) {
            // preferir codigoControle; se nulo, usa codigoRequisicao (apenas fallback)
            String codigoParaQr = (p.getCodigoControle() != null && !p.getCodigoControle().isBlank())
                    ? p.getCodigoControle()
                    : p.getCodigoRequisicao();

            if (codigoParaQr == null || codigoParaQr.trim().isEmpty()) {
                log.warn("Ignorando peca id={} sem codigo para QR (codigoControle e codigoRequisicao nulos)", p.getId());
                continue;
            }
            try {
                String filename = codigoParaQr + ".png";
                log.info("Gerando QR para id={} texto={}", p.getId(), codigoParaQr);
                String url = qrService.generatePngForText(codigoParaQr, filename);
                p.setQrUrl(url); // salva em qr_url — NÃO sobrescreve fotoUrl
                repo.save(p);
                log.info("QR gerado para id={} codigo={} url={}", p.getId(), codigoParaQr, url);
                count++;
            } catch (IOException | WriterException e) {
                log.error("Erro gerando QR para id={} codigo={}: {}", p.getId(), codigoParaQr, e.getMessage());
            }
        }
        log.info("RegenerateQrRunner finalizado. QRs gerados: {}", count);
    }
}