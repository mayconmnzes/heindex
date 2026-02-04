package br.com.heimdex.service;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.repository.ModeloEquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PecaReposicaoService {

    @Autowired private PecaReposicaoRepository pecaRepository;
    @Autowired private ModeloEquipamentoRepository modeloRepository;

    @Transactional
    public PecaReposicao salvarComMultiplosModelos(PecaReposicao peca, List<Long> modelosIds) {
        // 1. Busca todos os modelos reais no banco
        if (modelosIds != null && !modelosIds.isEmpty()) {
            List<ModeloEquipamento> modelosReais = modeloRepository.findAllById(modelosIds);
            peca.setModelosEquipamentos(modelosReais);
            
            // Define o primeiro como principal para compatibilidade com códigos antigos
            if (!modelosReais.isEmpty()) {
                peca.setModeloEquipamento(modelosReais.get(0));
            }
        }
        
        // 2. Salva a peça
        return pecaRepository.save(peca);
    }
}