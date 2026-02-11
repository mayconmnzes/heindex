package br.com.heimdex.controller;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.PecaReposicaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PecaReposicaoRepository pecaReposicaoRepository;

    private PecaReposicao testPeca;

    @BeforeEach
    void setUp() {
        // Limpa todas as peças antes de cada teste
        pecaReposicaoRepository.deleteAll();
        
        // Cria uma peça de teste
        testPeca = new PecaReposicao();
        testPeca.setNome("Spindle");
        testPeca.setCodigoControle(null); // NULL como no exemplo do problema
        testPeca.setCodigoRequisicao("61760-000");
        testPeca.setEstoqueAtual(10);
        testPeca.setEstoqueMinimo(2);
        testPeca = pecaReposicaoRepository.save(testPeca);
    }

    @Test
    @WithMockUser
    void testLookupByNumericId() throws Exception {
        // Buscar pela ID numérica: "19"
        mockMvc.perform(get("/api/pecas/lookup")
                .param("code", String.valueOf(testPeca.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPeca.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Spindle")));
    }

    @Test
    @WithMockUser
    void testLookupByIdWithPrefix() throws Exception {
        // Buscar com prefixo: "ID-19"
        mockMvc.perform(get("/api/pecas/lookup")
                .param("code", "ID-" + testPeca.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPeca.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Spindle")));
    }

    @Test
    @WithMockUser
    void testLookupByPecaPrefix() throws Exception {
        // Buscar com prefixo alternativo: "PECA-19"
        mockMvc.perform(get("/api/pecas/lookup")
                .param("code", "PECA-" + testPeca.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPeca.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Spindle")));
    }

    @Test
    @WithMockUser
    void testLookupByCodigoRequisicao() throws Exception {
        // Buscar por codigo_requisicao: "61760-000"
        mockMvc.perform(get("/api/pecas/lookup")
                .param("code", "61760-000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPeca.getId().intValue())))
                .andExpect(jsonPath("$.codigoRequisicao", is("61760-000")));
    }

    @Test
    @WithMockUser
    void testLookupByNome() throws Exception {
        // Buscar por nome parcial: "Spindle"
        mockMvc.perform(get("/api/pecas/lookup")
                .param("code", "Spindle")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(testPeca.getId().intValue())))
                .andExpect(jsonPath("$[0].nome", is("Spindle")));
    }

    @Test
    @WithMockUser
    void testLookupNotFound() throws Exception {
        // Buscar por código inexistente
        mockMvc.perform(get("/api/pecas/lookup")
                .param("code", "CODIGO-INEXISTENTE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("não encontrada")));
    }

    @Test
    @WithMockUser
    void testLookupWithEmptyCode() throws Exception {
        // Buscar com código vazio
        mockMvc.perform(get("/api/pecas/lookup")
                .param("code", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testLookupPriorityIdOverName() throws Exception {
        // Criar uma segunda peça cujo ID numérico será o mesmo que usaremos para busca
        // Primeiro salvamos para obter o ID
        PecaReposicao peca2 = new PecaReposicao();
        peca2.setNome("Temporary");
        peca2.setCodigoControle("CTRL-TEMP");
        peca2.setEstoqueAtual(5);
        peca2.setEstoqueMinimo(1);
        peca2 = pecaReposicaoRepository.save(peca2);
        Long id2 = peca2.getId();
        
        // Agora atualizamos o nome da primeira peça para ser igual ao ID da segunda
        // Isso cria um conflito onde o código poderia ser tanto ID quanto nome
        testPeca.setNome(id2.toString());
        pecaReposicaoRepository.save(testPeca);
        
        // Buscar pelo número que é tanto um ID válido quanto um nome
        // A busca deve priorizar ID e retornar peca2 (cujo ID é o número buscado)
        mockMvc.perform(get("/api/pecas/lookup")
                .param("code", id2.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id2.intValue())))
                .andExpect(jsonPath("$.nome", is("Temporary")));
    }
}
