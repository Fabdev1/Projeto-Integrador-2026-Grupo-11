package com.chamados.controller;

import com.chamados.domain.enums.StatusChamado;
import com.chamados.dto.ChamadoRequestDTO;
import com.chamados.dto.ChamadoResponseDTO;
import com.chamados.dto.HistoricoStatusResponseDTO;
import com.chamados.repository.HistoricoStatusRepository;
import com.chamados.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @Autowired
    private HistoricoStatusRepository historicoStatusRepository;

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criarChamado(@Valid @RequestBody ChamadoRequestDTO dto) {
        ChamadoResponseDTO response = chamadoService.criarChamado(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ChamadoResponseDTO>> listarTodos(
            @RequestParam(required = false) Long solicitanteId,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ChamadoResponseDTO> pagina = chamadoService.listarTodos(solicitanteId, pageable);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(@PathVariable Long id) {
        ChamadoResponseDTO response = chamadoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponseDTO>> historico(@PathVariable Long id) {
        List<HistoricoStatusResponseDTO> lista = historicoStatusRepository.findByChamadoIdOrderByDataAlteracaoAsc(id).stream()
                .map(HistoricoStatusResponseDTO::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ChamadoResponseDTO> alterarStatus(@PathVariable Long id, @RequestParam StatusChamado status, @RequestParam Long alteradoPorId) {
        ChamadoResponseDTO response = chamadoService.alterarStatus(id, status, alteradoPorId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/tecnico/{tecnicoId}")
    public ResponseEntity<ChamadoResponseDTO> atribuirTecnico(@PathVariable Long id, @PathVariable Long tecnicoId) {
        ChamadoResponseDTO response = chamadoService.atribuirTecnico(id, tecnicoId);
        return ResponseEntity.ok(response);
    }
}
