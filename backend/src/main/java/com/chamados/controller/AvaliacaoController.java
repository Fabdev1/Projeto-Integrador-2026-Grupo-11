package com.chamados.controller;

import com.chamados.dto.AvaliacaoRequestDTO;
import com.chamados.dto.AvaliacaoResponseDTO;
import com.chamados.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chamados/{chamadoId}/avaliacao")
@CrossOrigin(origins = "*")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public ResponseEntity<AvaliacaoResponseDTO> buscar(@PathVariable Long chamadoId) {
        return ResponseEntity.ok(avaliacaoService.buscarPorChamado(chamadoId));
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> avaliar(@PathVariable Long chamadoId,
                                                        @Valid @RequestBody AvaliacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.avaliar(chamadoId, dto));
    }
}
