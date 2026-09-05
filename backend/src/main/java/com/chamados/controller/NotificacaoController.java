package com.chamados.controller;

import com.chamados.domain.entity.Notificacao;
import com.chamados.dto.NotificacaoResponseDTO;
import com.chamados.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listarPorUsuario(@RequestParam Long usuarioId) {
        List<NotificacaoResponseDTO> lista = notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId).stream()
                .map(NotificacaoResponseDTO::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponseDTO> marcarComoLida(@PathVariable Long id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada com ID: " + id));
        notificacao.setLida(true);
        notificacao = notificacaoRepository.save(notificacao);
        return ResponseEntity.ok(new NotificacaoResponseDTO(notificacao));
    }
}
