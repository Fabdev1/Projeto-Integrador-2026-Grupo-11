package com.chamados.controller;

import com.chamados.domain.enums.PerfilUsuario;
import com.chamados.dto.UsuarioResponseDTO;
import com.chamados.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar(@RequestParam(required = false) PerfilUsuario perfil) {
        List<UsuarioResponseDTO> lista = (perfil != null
                ? usuarioRepository.findByPerfil(perfil)
                : usuarioRepository.findAll()).stream()
                .map(UsuarioResponseDTO::new)
                .toList();
        return ResponseEntity.ok(lista);
    }
}
