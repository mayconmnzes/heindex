// Código Completo
package br.com.heimdex.controller;

import br.com.heimdex.model.Area;
import br.com.heimdex.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/areas") // Define o endereço base para todos os "botões" deste controller
public class AreaController {

    // 1. Injeção de Dependência:
    // @Autowired é como se o Spring conectasse o "cabo" do nosso AreaRepository
    // aqui automaticamente, nos dando acesso aos métodos do banco de dados.
    @Autowired
    private AreaRepository areaRepository;

    // 2. Endpoint para CRIAR uma nova área (CREATE)
    // Método HTTP POST -> Usado para criar novos dados.
    // @RequestBody: Pega o JSON enviado pelo frontend e converte em um objeto Area.
    @PostMapping // Confirme que é @PostMapping
    public ResponseEntity<Area> criarArea(@RequestBody Area area) { // Confirme o @RequestBody
        Area novaArea = areaRepository.save(area);
        return new ResponseEntity<>(novaArea, HttpStatus.CREATED);
    }

    // 3. Endpoint para BUSCAR TODAS as áreas (READ)
    // Método HTTP GET -> Usado para ler dados.
    @GetMapping
    public List<Area> listarAreas() {
        return areaRepository.findAll();
    }

    // 4. Endpoint para BUSCAR UMA área por ID (READ)
    // {id} é uma variável na URL.
    // @PathVariable: Pega o valor de {id} da URL e joga na variável Long id.
    @GetMapping("/{id}")
    public ResponseEntity<Area> buscarAreaPorId(@PathVariable Long id) {
        Optional<Area> area = areaRepository.findById(id);
        // Se a área existir, retorna a área com status 200 OK.
        // Se não, retorna um status 404 Not Found.
        return area.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 5. Endpoint para ATUALIZAR uma área (UPDATE)
    // Método HTTP PUT -> Usado para atualizar dados existentes.
    @PutMapping("/{id}")
    public ResponseEntity<Area> atualizarArea(@PathVariable Long id, @RequestBody Area detalhesArea) {
        return areaRepository.findById(id)
                .map(areaExistente -> {
                    areaExistente.setNome(detalhesArea.getNome());
                    Area areaAtualizada = areaRepository.save(areaExistente);
                    return ResponseEntity.ok(areaAtualizada);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 6. Endpoint para DELETAR uma área (DELETE)
    // Método HTTP DELETE -> Usado para remover dados.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarArea(@PathVariable Long id) {
        return areaRepository.findById(id)
                .map(area -> {
                    areaRepository.delete(area);
                    return ResponseEntity.ok().build(); // Retorna 200 OK sem corpo
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}