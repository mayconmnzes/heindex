package br.com.heimdex.controller;

import br.com.heimdex.dto.LoginRequestDTO;
import br.com.heimdex.dto.UsuarioDTO;
import br.com.heimdex.model.Usuario;
import br.com.heimdex.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Endpoint de AUTENTICAÇÃO
    @PostMapping("/authenticate")
    public ResponseEntity<UsuarioDTO> authenticateUser(@RequestBody LoginRequestDTO loginRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByMatricula(loginRequest.getMatricula());

        if (usuarioOpt.isPresent() && passwordEncoder.matches(loginRequest.getSenha(), usuarioOpt.get().getSenha())) {
            Usuario usuario = usuarioOpt.get();
            
            // Cria o DTO com os dados seguros
            UsuarioDTO usuarioDTO = new UsuarioDTO();
            usuarioDTO.setId(usuario.getId());
            usuarioDTO.setNomeCompleto(usuario.getNomeCompleto());
            usuarioDTO.setMatricula(usuario.getMatricula());
            usuarioDTO.setPerfil(usuario.getPerfil());
            
            return ResponseEntity.ok(usuarioDTO);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // CREATE (POST)
    @PostMapping
    @Transactional
    public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario usuario) {
        // Encripta a senha antes de salvar
        if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha é obrigatória para novos usuários.");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario novoUsuario = usuarioRepository.save(usuario);
        return new ResponseEntity<>(novoUsuario, HttpStatus.CREATED);
    }
    
    // READ ALL (GET)
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
    
    // UPDATE (PUT) - NOVO ENDPOINT
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetails) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {
                    // Atualiza dados simples
                    usuarioExistente.setNomeCompleto(usuarioDetails.getNomeCompleto());
                    usuarioExistente.setMatricula(usuarioDetails.getMatricula());
                    usuarioExistente.setPerfil(usuarioDetails.getPerfil());

                    // Só atualiza a senha se um novo valor (não vazio) for fornecido
                    if (usuarioDetails.getSenha() != null && !usuarioDetails.getSenha().isEmpty()) {
                         usuarioExistente.setSenha(passwordEncoder.encode(usuarioDetails.getSenha()));
                    }

                    Usuario usuarioAtualizado = usuarioRepository.save(usuarioExistente);
                    return ResponseEntity.ok(usuarioAtualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE (DELETE) - NOVO ENDPOINT
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}