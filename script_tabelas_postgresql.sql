-- ============================================================================
-- SCRIPT DDL DE CRIAÇÃO DAS TABELAS - SISTEMA DE CHAMADOS TÉCNICOS
-- Banco de Dados: PostgreSQL
-- ============================================================================

-- 1. CRIAÇÃO DA TABELA USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    perfil VARCHAR(50) NOT NULL CHECK (perfil IN ('SOLICITANTE', 'TECNICO', 'ADMIN')),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CRIAÇÃO DA TABELA CATEGORIAS
CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) UNIQUE NOT NULL
);

-- 3. CRIAÇÃO DA TABELA CHAMADOS
CREATE TABLE IF NOT EXISTS chamados (
    id BIGSERIAL PRIMARY KEY,
    categoria_id BIGINT NOT NULL,
    solicitante_id BIGINT NOT NULL,
    tecnico_id BIGINT, -- Pode ser NULL até um técnico ser atribuído
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTO' CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'RESOLVIDO', 'FECHADO', 'CANCELADO')),
    prioridade VARCHAR(50) NOT NULL DEFAULT 'MEDIA' CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'URGENTE')),
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos (Chaves Estrangeiras)
    CONSTRAINT fk_chamado_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE RESTRICT,
    CONSTRAINT fk_chamado_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_chamado_tecnico FOREIGN KEY (tecnico_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- 4. CRIAÇÃO DA TABELA HISTORICO_STATUS
CREATE TABLE IF NOT EXISTS historico_status (
    id BIGSERIAL PRIMARY KEY,
    chamado_id BIGINT NOT NULL,
    alterado_por BIGINT NOT NULL,
    status_anterior VARCHAR(50),
    status_novo VARCHAR(50) NOT NULL,
    data_alteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    CONSTRAINT fk_historico_chamado FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE,
    CONSTRAINT fk_historico_usuario FOREIGN KEY (alterado_por) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- 5. CRIAÇÃO DA TABELA COMENTARIOS
CREATE TABLE IF NOT EXISTS comentarios (
    id BIGSERIAL PRIMARY KEY,
    chamado_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    mensagem TEXT NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    CONSTRAINT fk_comentario_chamado FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE,
    CONSTRAINT fk_comentario_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- 6. CRIAÇÃO DA TABELA ANEXOS
CREATE TABLE IF NOT EXISTS anexos (
    id BIGSERIAL PRIMARY KEY,
    chamado_id BIGINT NOT NULL,
    enviado_por BIGINT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_arquivo VARCHAR(500),
    data_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    CONSTRAINT fk_anexo_chamado FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE,
    CONSTRAINT fk_anexo_usuario FOREIGN KEY (enviado_por) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- 7. CRIAÇÃO DA TABELA NOTIFICACOES
CREATE TABLE IF NOT EXISTS notificacoes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    chamado_id BIGINT NOT NULL,
    mensagem TEXT,
    lida BOOLEAN DEFAULT FALSE NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    CONSTRAINT fk_notificacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_notificacao_chamado FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE
);

-- 8. CRIAÇÃO DA TABELA AVALIACOES (Relacionamento 1:1 com CHAMADOS)
CREATE TABLE IF NOT EXISTS avaliacoes (
    id BIGSERIAL PRIMARY KEY,
    chamado_id BIGINT UNIQUE NOT NULL, -- Restrição UNIQUE garante que cada chamado tenha no máximo 1 avaliação
    usuario_id BIGINT NOT NULL,
    nota INTEGER NOT NULL CHECK (nota >= 1 AND nota <= 5),
    comentario TEXT,
    data_avaliacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    CONSTRAINT fk_avaliacao_chamado FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- ============================================================================
-- ÍNDICES RECOMENDADOS PARA PERFORMANCE
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_chamados_solicitante ON chamados(solicitante_id);
CREATE INDEX IF NOT EXISTS idx_chamados_tecnico ON chamados(tecnico_id);
CREATE INDEX IF NOT EXISTS idx_chamados_categoria ON chamados(categoria_id);
CREATE INDEX IF NOT EXISTS idx_chamados_status ON chamados(status);
CREATE INDEX IF NOT EXISTS idx_historico_chamado ON historico_status(chamado_id);
CREATE INDEX IF NOT EXISTS idx_comentarios_chamado ON comentarios(chamado_id);
CREATE INDEX IF NOT EXISTS idx_anexos_chamado ON anexos(chamado_id);
CREATE INDEX IF NOT EXISTS idx_notificacoes_usuario ON notificacoes(usuario_id);

-- Inserir Categorias Padrão
INSERT INTO categorias (nome) VALUES 
('Hardware'),
('Software / Sistemas'),
('Redes / Conectividade'),
('Acessos e Permissões')
ON CONFLICT (nome) DO NOTHING;

-- Inserir Usuários Iniciais (Solicitante, Técnico e Admin)
INSERT INTO usuarios (nome, email, perfil) VALUES
('João Solicitante', 'joao.solicitante@empresa.com', 'SOLICITANTE'),
('Maria Técnica', 'maria.tecnica@empresa.com', 'TECNICO'),
('Carlos Administrador', 'carlos.admin@empresa.com', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
