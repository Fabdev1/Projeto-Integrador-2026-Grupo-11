package com.chamados.controller;

import com.chamados.dto.AnexoResponseDTO;
import com.chamados.service.AnexoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/anexos")
@CrossOrigin(origins = "*")
public class AnexoController {

    @Autowired
    private AnexoService anexoService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<AnexoResponseDTO> enviar(@RequestParam Long chamadoId,
                                                    @RequestParam Long enviadoPorId,
                                                    @RequestParam("arquivo") MultipartFile arquivo) {
        AnexoResponseDTO response = anexoService.enviar(chamadoId, enviadoPorId, arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AnexoResponseDTO>> listarPorChamado(@RequestParam Long chamadoId) {
        return ResponseEntity.ok(anexoService.listarPorChamado(chamadoId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = anexoService.carregarArquivo(id);
        String nomeOriginal = anexoService.nomeOriginal(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeOriginal + "\"")
                .body(resource);
    }
}
