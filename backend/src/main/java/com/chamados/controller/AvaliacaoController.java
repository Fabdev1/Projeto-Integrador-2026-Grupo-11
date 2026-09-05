package com.chamados.controller;

import com.chamados.dto.AvaliacaoRequestDTO;
import com.chamados.dto.AvaliacaoResponseDTO;
import com.chamados.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/avaliacoes")
@CrossOrigin(origins = "*")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> criar(@Valid @RequestBody AvaliacaoRequestDTO dto) {
        AvaliacaoResponseDTO response = avaliacaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{chamadoId}")
    public ResponseEntity<AvaliacaoResponseDTO> buscarPorChamado(@PathVariable Long chamadoId) {
        return ResponseEntity.ok(avaliacaoService.buscarPorChamado(chamadoId));
    }
}
