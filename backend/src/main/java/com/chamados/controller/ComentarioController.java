package com.chamados.controller;

import com.chamados.dto.ComentarioRequestDTO;
import com.chamados.dto.ComentarioResponseDTO;
import com.chamados.service.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados/{chamadoId}/comentarios")
@CrossOrigin(origins = "*")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponseDTO>> listar(@PathVariable Long chamadoId) {
        return ResponseEntity.ok(comentarioService.listarPorChamado(chamadoId));
    }

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> comentar(@PathVariable Long chamadoId,
                                                          @Valid @RequestBody ComentarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioService.comentar(chamadoId, dto));
    }
}
