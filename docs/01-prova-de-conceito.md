# Revisita do projeto e definição da prova de conceito

Projeto Integrador, 4º semestre TADS/TSI, Segunda Etapa. Grupo 11.
Produto: **HelpDesk, sistema de chamados de suporte técnico interno.**

---

## 1. O que a primeira etapa entregou

A primeira etapa fechou a ideação: visão de produto, personas e os fluxos associados. O artefato técnico que saiu dela foi a modelagem relacional em PostgreSQL, com 8 tabelas e os relacionamentos entre elas:

| Tabela | Papel na modelagem | Relacionamento principal |
|---|---|---|
| `usuarios` | solicitante, técnico e administrador na mesma tabela, separados por `perfil` | 1:N com quase todas as demais |
| `categorias` | tipos de atendimento (Hardware, Software, Redes, Acessos) | 1:N com `chamados` |
| `chamados` | entidade central da operação | N:1 com categoria, solicitante e técnico |
| `historico_status` | auditoria de cada transição de status | N:1 com `chamados` e `usuarios` |
| `comentarios` | conversa entre solicitante e técnico dentro do chamado | N:1 com `chamados` e `usuarios` |
| `anexos` | arquivos enviados junto ao chamado | N:1 com `chamados` e `usuarios` |
| `notificacoes` | avisos internos para o usuário | N:1 com `chamados` e `usuarios` |
| `avaliacoes` | nota de 1 a 5 depois do encerramento | 1:1 com `chamados` |

## 2. O que a revisita mudou, e por quê

A revisita não foi um carimbo no que já existia. Três decisões saíram dela.

### 2.1 A modelagem tem 8 tabelas, mas a prova de conceito não precisa exercitar as 8

Anexos exigem armazenamento de arquivo, política de tamanho, varredura e um endpoint de download. Isso consome o tempo da etapa sem provar nada que já não esteja provado pelas outras tabelas: continua sendo um `INSERT` filho de `chamados`. A tabela permanece no banco e o repositório JPA foi criado, mas o upload ficou fora do recorte, e essa exclusão está registrada aqui de propósito.

### 2.2 O campo `status` do chamado, sozinho, não sustenta a promessa do produto

A persona do solicitante quer saber em que pé está o pedido dela. Um campo `status` responde "onde está agora" e perde "como chegou até aqui". A tabela `historico_status` já existia na modelagem da primeira etapa, mas nada no sistema a alimentava: a alteração de status gravava apenas o novo valor em `chamados`.

Correção aplicada nesta etapa: toda transição de status passa a gravar uma linha em `historico_status` com status anterior, status novo, autor e data, dentro da mesma transação da alteração. Sem isso, a tabela seria decoração no diagrama.

### 2.3 A alteração de status não registrava quem alterou

A coluna `alterado_por` é `NOT NULL` no script DDL. A assinatura original do serviço recebia apenas `(id, status)`, o que tornaria impossível gravar o histórico sem inventar um autor. O contrato do endpoint de status mudou para receber o autor da mudança no corpo da requisição. É uma mudança incompatível com a versão anterior do endpoint, e está documentada em `docs/03-contrato-da-api.md`.

### 2.4 O formulário pedia dois campos que o banco não guarda

A tela de abertura tinha "Local do incidente" e um seletor de arquivo para anexo. Nenhum dos dois tem coluna correspondente em `chamados`, e o anexo depende do recurso que ficou fora do recorte. Mantê-los na tela seria prometer ao usuário um dado que o sistema descarta no envio.

Os dois campos saíram do formulário. O local do incidente, quando for reintroduzido, vira coluna própria em `chamados` e entra também no DDL, e não texto colado na descrição.

## 3. A prova de conceito escolhida

> **Percurso do chamado, da abertura ao fechamento com nota, com dois perfis operando o mesmo registro.**

Escolhido porque é o único percurso que atravessa todas as camadas de uma vez (formulário, API REST, regra de negócio, transação, banco relacional com chave estrangeira e restrição de unicidade) e porque é o percurso que a persona do solicitante executa. Se ele funciona ponta a ponta, o produto existe. Se ele não funciona, nenhum outro recurso salva a entrega.

Passo a passo do que a demonstração precisa executar:

1. Solicitante abre o chamado escolhendo categoria e prioridade (`POST /api/chamados`).
2. O sistema grava o chamado com status `ABERTO`, cria a primeira linha do histórico e notifica os técnicos.
3. Técnico assume o chamado (`PUT /api/chamados/{id}/tecnico/{tecnicoId}`), o status vira `EM_ANDAMENTO` e o solicitante é notificado.
4. Técnico e solicitante trocam mensagens no chamado (`POST /api/chamados/{id}/comentarios`).
5. Técnico marca como `RESOLVIDO` com uma justificativa (`PUT /api/chamados/{id}/status`).
6. Solicitante avalia o atendimento com nota de 1 a 5 (`POST /api/chamados/{id}/avaliacao`), e o chamado vai para `FECHADO`.
7. A tela de detalhes mostra a linha do tempo completa do que aconteceu.

### Dentro do recorte

- Cadastro e listagem de categorias e usuários (leitura, alimentada pelo seed do banco).
- Abertura, listagem, filtro e consulta de chamados.
- Atribuição de técnico.
- Transição de status com gravação de auditoria.
- Comentários por chamado.
- Avaliação 1:1 por chamado, com regra de negócio.
- Notificações internas geradas pelo sistema e marcação de leitura.
- Painel com contadores lidos do banco.

### Fora do recorte, com motivo

| Item | Por que ficou de fora |
|---|---|
| Login com senha e sessão | autenticação não é o que a prova de conceito precisa provar; o usuário ativo é escolhido em um seletor na barra superior, o que também facilita demonstrar os dois perfis no vídeo de 1 minuto |
| Upload de anexos | custo de infraestrutura de arquivo sem ganho de prova (ver 2.1) |
| Envio de e-mail | a notificação é gravada na tabela `notificacoes` e exibida na interface; disparo externo depende de serviço de terceiro |
| Painel administrativo com relatórios | o perfil `ADMIN` existe no modelo mas não tem tela nesta etapa |
| Prazos de atendimento (SLA) | exigiria novas colunas e regras de contagem que não estavam na modelagem da primeira etapa |

## 4. Critérios de aceite da prova de conceito

A demonstração é considerada aprovada quando, com o banco vazio de chamados:

1. Abrir um chamado pela interface gera uma linha em `chamados` e uma em `historico_status`.
2. Recarregar a página mantém o chamado (o dado veio do PostgreSQL, não da memória do navegador).
3. Atribuir técnico muda o status para `EM_ANDAMENTO` e gera a segunda linha de histórico.
4. Um comentário aparece para os dois perfis na tela de detalhes.
5. Avaliar duas vezes o mesmo chamado retorna erro 409, e não uma exceção não tratada.
6. Avaliar um chamado ainda em andamento retorna erro 409 com mensagem legível.
7. Os contadores do painel batem com uma consulta `SELECT status, COUNT(*) FROM chamados GROUP BY status`.

## 5. Onde cada item da rubrica foi atendido

| Rubrica | Onde está |
|---|---|
| Revisitar o projeto e definir a prova de conceito (1,0) | este documento, seções 2 e 3 |
| Preparação do ambiente de desenvolvimento (2,0) | `docs/02-ambiente-de-desenvolvimento.md`, `docker-compose.yml`, `banco/script_dados_demo.sql` |
| Desenvolvimento do frontend (2,0) | `frontend/`, consumindo a API real |
| Desenvolvimento do backend com repositório de dados (2,0) | `backend/`, PostgreSQL via Spring Data JPA |
| Vídeo de até 1 minuto (1,0) | `docs/04-roteiro-video.md` |
| GitHub com todos os itens (2,0) | `README.md`, histórico de commits por integrante |
