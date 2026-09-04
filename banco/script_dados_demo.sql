-- ============================================================================
-- DADOS DE DEMONSTRACAO - SISTEMA DE CHAMADOS
-- Rodar DEPOIS de script_tabelas_postgresql.sql
-- Reexecutar e seguro: os inserts sao idempotentes por chave natural.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Usuarios adicionais (o script de estrutura ja cria Joao, Maria e Carlos)
-- ---------------------------------------------------------------------------
INSERT INTO usuarios (nome, email, perfil) VALUES
    ('Ana Ribeiro',    'ana.ribeiro@empresa.com',    'SOLICITANTE'),
    ('Bruno Teixeira', 'bruno.teixeira@empresa.com', 'SOLICITANTE'),
    ('Rafael Nunes',   'rafael.nunes@empresa.com',   'TECNICO')
ON CONFLICT (email) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Chamados de exemplo
-- Cobrem os quatro estados que o painel conta: ABERTO, EM_ANDAMENTO,
-- RESOLVIDO e FECHADO. O chamado FECHADO ja tem avaliacao.
-- ---------------------------------------------------------------------------
INSERT INTO chamados (categoria_id, solicitante_id, tecnico_id, status, prioridade, titulo, descricao, data_criacao)
SELECT
    (SELECT id FROM categorias WHERE nome = 'Hardware'),
    (SELECT id FROM usuarios  WHERE email = 'joao.solicitante@empresa.com'),
    (SELECT id FROM usuarios  WHERE email = 'maria.tecnica@empresa.com'),
    'EM_ANDAMENTO', 'ALTA',
    'Impressora do financeiro nao imprime',
    'A impressora aparece offline para todos os computadores do setor desde a manha de segunda. Ja reiniciamos o equipamento e o cabo de rede esta conectado.',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
WHERE NOT EXISTS (SELECT 1 FROM chamados WHERE titulo = 'Impressora do financeiro nao imprime');

INSERT INTO chamados (categoria_id, solicitante_id, status, prioridade, titulo, descricao, data_criacao)
SELECT
    (SELECT id FROM categorias WHERE nome = 'Acessos e Permissões'),
    (SELECT id FROM usuarios  WHERE email = 'ana.ribeiro@empresa.com'),
    'ABERTO', 'MEDIA',
    'Sem acesso ao sistema de CRM',
    'A tela de login devolve "usuario ou senha invalidos" mesmo apos a redefinicao de senha.',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM chamados WHERE titulo = 'Sem acesso ao sistema de CRM');

INSERT INTO chamados (categoria_id, solicitante_id, status, prioridade, titulo, descricao, data_criacao)
SELECT
    (SELECT id FROM categorias WHERE nome = 'Redes / Conectividade'),
    (SELECT id FROM usuarios  WHERE email = 'bruno.teixeira@empresa.com'),
    'ABERTO', 'ALTA',
    'Internet instavel na sala 204',
    'A conexao cai por cerca de um minuto, varias vezes ao dia, sempre durante as reunioes.',
    CURRENT_TIMESTAMP - INTERVAL '6 hours'
WHERE NOT EXISTS (SELECT 1 FROM chamados WHERE titulo = 'Internet instavel na sala 204');

INSERT INTO chamados (categoria_id, solicitante_id, tecnico_id, status, prioridade, titulo, descricao, data_criacao)
SELECT
    (SELECT id FROM categorias WHERE nome = 'Software / Sistemas'),
    (SELECT id FROM usuarios  WHERE email = 'joao.solicitante@empresa.com'),
    (SELECT id FROM usuarios  WHERE email = 'rafael.nunes@empresa.com'),
    'RESOLVIDO', 'BAIXA',
    'Instalacao do pacote Office na maquina nova',
    'Maquina recebida pelo setor de compras, sem o pacote Office instalado.',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
WHERE NOT EXISTS (SELECT 1 FROM chamados WHERE titulo = 'Instalacao do pacote Office na maquina nova');

INSERT INTO chamados (categoria_id, solicitante_id, tecnico_id, status, prioridade, titulo, descricao, data_criacao)
SELECT
    (SELECT id FROM categorias WHERE nome = 'Software / Sistemas'),
    (SELECT id FROM usuarios  WHERE email = 'ana.ribeiro@empresa.com'),
    (SELECT id FROM usuarios  WHERE email = 'maria.tecnica@empresa.com'),
    'FECHADO', 'MEDIA',
    'Erro ao iniciar o sistema interno',
    'A aplicacao fecha sozinha na tela de carregamento, sem mensagem de erro.',
    CURRENT_TIMESTAMP - INTERVAL '8 days'
WHERE NOT EXISTS (SELECT 1 FROM chamados WHERE titulo = 'Erro ao iniciar o sistema interno');

-- ---------------------------------------------------------------------------
-- Historico de status
-- Reproduz o rastro que a aplicacao passa a gravar sozinha a cada transicao.
-- ---------------------------------------------------------------------------
INSERT INTO historico_status (chamado_id, alterado_por, status_anterior, status_novo, data_alteracao)
SELECT c.id, c.solicitante_id, NULL, 'ABERTO', c.data_criacao
FROM chamados c
WHERE NOT EXISTS (
    SELECT 1 FROM historico_status h WHERE h.chamado_id = c.id AND h.status_novo = 'ABERTO'
);

INSERT INTO historico_status (chamado_id, alterado_por, status_anterior, status_novo, data_alteracao)
SELECT c.id, c.tecnico_id, 'ABERTO', 'EM_ANDAMENTO', c.data_criacao + INTERVAL '2 hours'
FROM chamados c
WHERE c.tecnico_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM historico_status h WHERE h.chamado_id = c.id AND h.status_novo = 'EM_ANDAMENTO'
);

INSERT INTO historico_status (chamado_id, alterado_por, status_anterior, status_novo, data_alteracao)
SELECT c.id, c.tecnico_id, 'EM_ANDAMENTO', 'RESOLVIDO', c.data_criacao + INTERVAL '1 day'
FROM chamados c
WHERE c.status IN ('RESOLVIDO', 'FECHADO')
  AND NOT EXISTS (
    SELECT 1 FROM historico_status h WHERE h.chamado_id = c.id AND h.status_novo = 'RESOLVIDO'
);

INSERT INTO historico_status (chamado_id, alterado_por, status_anterior, status_novo, data_alteracao)
SELECT c.id, c.solicitante_id, 'RESOLVIDO', 'FECHADO', c.data_criacao + INTERVAL '2 days'
FROM chamados c
WHERE c.status = 'FECHADO'
  AND NOT EXISTS (
    SELECT 1 FROM historico_status h WHERE h.chamado_id = c.id AND h.status_novo = 'FECHADO'
);

-- ---------------------------------------------------------------------------
-- Comentarios
-- ---------------------------------------------------------------------------
INSERT INTO comentarios (chamado_id, usuario_id, mensagem, data_criacao)
SELECT c.id, c.tecnico_id,
       'Verifiquei remotamente e a fila de impressao esta travada. Vou ate o setor hoje a tarde para trocar o cabo de rede.',
       c.data_criacao + INTERVAL '3 hours'
FROM chamados c
WHERE c.titulo = 'Impressora do financeiro nao imprime'
  AND NOT EXISTS (SELECT 1 FROM comentarios cm WHERE cm.chamado_id = c.id);

INSERT INTO comentarios (chamado_id, usuario_id, mensagem, data_criacao)
SELECT c.id, c.solicitante_id,
       'Combinado. O setor fica aberto ate as 18h.',
       c.data_criacao + INTERVAL '4 hours'
FROM chamados c
WHERE c.titulo = 'Impressora do financeiro nao imprime'
  AND (SELECT COUNT(*) FROM comentarios cm WHERE cm.chamado_id = c.id) = 1;

-- ---------------------------------------------------------------------------
-- Avaliacao do chamado ja fechado (relacionamento 1:1)
-- ---------------------------------------------------------------------------
INSERT INTO avaliacoes (chamado_id, usuario_id, nota, comentario, data_avaliacao)
SELECT c.id, c.solicitante_id, 5,
       'Resolvido no mesmo dia e com explicacao do que tinha acontecido.',
       c.data_criacao + INTERVAL '2 days'
FROM chamados c
WHERE c.titulo = 'Erro ao iniciar o sistema interno'
ON CONFLICT (chamado_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Notificacoes
-- ---------------------------------------------------------------------------
INSERT INTO notificacoes (usuario_id, chamado_id, mensagem, lida, data_criacao)
SELECT c.solicitante_id, c.id,
       'Seu chamado #' || c.id || ' foi assumido por ' || u.nome || '.',
       FALSE, c.data_criacao + INTERVAL '2 hours'
FROM chamados c
JOIN usuarios u ON u.id = c.tecnico_id
WHERE c.tecnico_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM notificacoes n WHERE n.chamado_id = c.id);

-- ---------------------------------------------------------------------------
-- Conferencia rapida
-- ---------------------------------------------------------------------------
-- SELECT status, COUNT(*) FROM chamados GROUP BY status ORDER BY status;
-- SELECT c.id, c.titulo, COUNT(h.id) AS eventos
--   FROM chamados c LEFT JOIN historico_status h ON h.chamado_id = c.id
--  GROUP BY c.id, c.titulo ORDER BY c.id;
