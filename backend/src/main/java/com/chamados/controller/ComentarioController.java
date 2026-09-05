package com.chamados.controller;

import com.chamados.dto.ComentarioRequestDTO;
import com.chamados.dto.ComentarioResponseDTO;
import com.chamados.service.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@CrossOrigin(origins = "*")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> criar(@Valid @RequestBody ComentarioRequestDTO dto) {
        ComentarioResponseDTO response = comentarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponseDTO>> listarPorChamado(@RequestParam Long chamadoId) {
        return ResponseEntity.ok(comentarioService.listarPorChamado(chamadoId));
    }
}
