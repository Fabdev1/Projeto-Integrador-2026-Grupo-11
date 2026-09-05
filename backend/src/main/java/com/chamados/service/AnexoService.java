package com.chamados.service;

import com.chamados.domain.entity.Anexo;
import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.Usuario;
import com.chamados.dto.AnexoResponseDTO;
import com.chamados.repository.AnexoRepository;
import com.chamados.repository.ChamadoRepository;
import com.chamados.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnexoService {

    @Autowired
    private AnexoRepository anexoRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public AnexoResponseDTO enviar(Long chamadoId, Long enviadoPorId, MultipartFile arquivo) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado com ID: " + chamadoId));

        Usuario enviadoPor = usuarioRepository.findById(enviadoPorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + enviadoPorId));

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio");
        }

        String nomeOriginal = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename() : "arquivo";
        String nomeArmazenado = UUID.randomUUID() + "_" + nomeOriginal;

        try {
            Path pasta = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(pasta);
            Path destino = pasta.resolve(nomeArmazenado).normalize();
            if (!destino.startsWith(pasta)) {
                throw new IllegalArgumentException("Nome de arquivo inválido");
            }
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo: " + e.getMessage(), e);
        }

        Anexo anexo = Anexo.builder()
                .chamado(chamado)
                .enviadoPor(enviadoPor)
                .nomeArquivo(nomeOriginal)
                .caminhoArquivo(nomeArmazenado)
                .build();

        anexo = anexoRepository.save(anexo);
        return new AnexoResponseDTO(anexo);
    }

    @Transactional(readOnly = true)
    public List<AnexoResponseDTO> listarPorChamado(Long chamadoId) {
        return anexoRepository.findByChamadoIdOrderByDataEnvioAsc(chamadoId).stream()
                .map(AnexoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource carregarArquivo(Long anexoId) {
        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado com ID: " + anexoId));

        try {
            Path caminho = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(anexo.getCaminhoArquivo());
            Resource resource = new UrlResource(caminho.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Arquivo não encontrado no armazenamento: " + anexo.getNomeArquivo());
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Falha ao carregar o arquivo: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public String nomeOriginal(Long anexoId) {
        return anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado com ID: " + anexoId))
                .getNomeArquivo();
    }
}
