package br.com.heimdex.service;

import br.com.heimdex.repository.PecaReposicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PecaService {

    @Autowired
    private PecaReposicaoRepository pecaRepo;

    /**
     * Gera próximo codigo no formato PECA-0001, PECA-0002 ...
     * Seguro: busca todos os codigo_controle que começam com "PECA-" e calcula o maior sufixo.
     */
    public synchronized String gerarProximoCodigoControle() {
        List<String> codigos = pecaRepo.findCodigoControleLike("PECA-%");
        int max = 0;
        for (String c : codigos) {
            if (c == null) continue;
            // extrai dígitos à direita, tolerante a formatos:
            String digits = c.replaceAll("^.*?(\\d+)$", "$1");
            try {
                int n = Integer.parseInt(digits);
                if (n > max) max = n;
            } catch (NumberFormatException ignored) {}
        }
        int next = max + 1;
        return String.format("PECA-%04d", next);
    }
    
}