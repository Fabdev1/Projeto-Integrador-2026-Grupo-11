package com.chamados.controller;

import com.chamados.domain.enums.StatusChamado;
import com.chamados.dto.AlterarStatusRequestDTO;
import com.chamados.dto.ChamadoDetalheResponseDTO;
import com.chamados.dto.ChamadoRequestDTO;
import com.chamados.dto.ChamadoResponseDTO;
import com.chamados.dto.HistoricoStatusResponseDTO;
import com.chamados.dto.ResumoChamadosDTO;
import com.chamados.service.ChamadoService;
import com.chamados.service.HistoricoStatusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados")
@CrossOrigin(origins = "*")
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final HistoricoStatusService historicoStatusService;

    public ChamadoController(ChamadoService chamadoService,
                             HistoricoStatusService historicoStatusService) {
        this.chamadoService = chamadoService;
        this.historicoStatusService = historicoStatusService;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criarChamado(@Valid @RequestBody ChamadoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.criarChamado(dto));
    }

    /** Filtros opcionais e combinaveis: ?status=ABERTO&solicitanteId=1&tecnicoId=2 */
    @GetMapping
    public ResponseEntity<List<ChamadoResponseDTO>> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Long solicitanteId,
            @RequestParam(required = false) Long tecnicoId) {
        return ResponseEntity.ok(chamadoService.listarComFiltros(status, solicitanteId, tecnicoId));
    }

    /** Contadores do painel, sem baixar a lista inteira. */
    @GetMapping("/resumo")
    public ResponseEntity<ResumoChamadosDTO> resumo() {
        return ResponseEntity.ok(chamadoService.resumo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id));
    }

    @GetMapping("/{id}/detalhes")
    public ResponseEntity<ChamadoDetalheResponseDTO> detalhes(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.buscarDetalhes(id));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponseDTO>> historico(@PathVariable Long id) {
        return ResponseEntity.ok(historicoStatusService.listarPorChamado(id));
    }

    /**
     * O corpo traz statusNovo, usuarioId e um comentario opcional.
     * O autor e obrigatorio porque a mudanca gera uma linha de auditoria.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ChamadoResponseDTO> alterarStatus(@PathVariable Long id,
                                                            @Valid @RequestBody AlterarStatusRequestDTO dto) {
        return ResponseEntity.ok(chamadoService.alterarStatus(id, dto));
    }

    @PutMapping("/{id}/tecnico/{tecnicoId}")
    public ResponseEntity<ChamadoResponseDTO> atribuirTecnico(@PathVariable Long id,
                                                              @PathVariable Long tecnicoId) {
        return ResponseEntity.ok(chamadoService.atribuirTecnico(id, tecnicoId));
    }
}
