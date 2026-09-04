package com.chamados.service;

import com.chamados.domain.enums.PerfilUsuario;
import com.chamados.dto.UsuarioResponseDTO;
import com.chamados.exception.RecursoNaoEncontradoException;
import com.chamados.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar(PerfilUsuario perfil) {
        var usuarios = (perfil == null)
                ? usuarioRepository.findAll(Sort.by("nome"))
                : usuarioRepository.findByPerfil(perfil);
        return usuarios.stream().map(UsuarioResponseDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioResponseDTO::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
    }
}
