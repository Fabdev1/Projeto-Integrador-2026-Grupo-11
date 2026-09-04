package com.chamados.controller;

import com.chamados.domain.enums.StatusChamado;
import com.chamados.dto.ChamadoRequestDTO;
import com.chamados.dto.ChamadoResponseDTO;
import com.chamados.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados")
@CrossOrigin(origins = "*")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criarChamado(@Valid @RequestBody ChamadoRequestDTO dto) {
        ChamadoResponseDTO response = chamadoService.criarChamado(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChamadoResponseDTO>> listarTodos() {
        List<ChamadoResponseDTO> lista = chamadoService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(@PathVariable Long id) {
        ChamadoResponseDTO response = chamadoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ChamadoResponseDTO> alterarStatus(@PathVariable Long id, @RequestParam StatusChamado status) {
        ChamadoResponseDTO response = chamadoService.alterarStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/tecnico/{tecnicoId}")
    public ResponseEntity<ChamadoResponseDTO> atribuirTecnico(@PathVariable Long id, @PathVariable Long tecnicoId) {
        ChamadoResponseDTO response = chamadoService.atribuirTecnico(id, tecnicoId);
        return ResponseEntity.ok(response);
    }
}
