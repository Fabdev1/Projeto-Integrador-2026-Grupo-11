# Roteiro do vídeo (60 segundos)

A rubrica corta ponto quando o vídeo diverge do que foi desenvolvido. O roteiro abaixo mostra só o que existe no código, na ordem da prova de conceito.

## Antes de gravar

1. `docker compose down -v && docker compose up -d` (banco limpo com os dados de demonstração).
2. `cd backend && mvn spring-boot:run` (espere aparecer "Started ChamadosApplication").
3. `cd frontend && npx serve -l 5500`.
4. Abra `http://localhost:5500` e deixe o pgAdmin ou o DBeaver aberto em outra janela, na tabela `historico_status`.
5. Feche abas e notificações do sistema. Grave a tela inteira em 1080p.

## Divisão do tempo

| Tempo | Tela | O que dizer |
|---|---|---|
| 0:00 a 0:08 | Painel com os contadores | "HelpDesk, sistema de chamados do Grupo 11. Os contadores vêm de uma consulta no PostgreSQL, não de dados fixos na tela." |
| 0:08 a 0:20 | Novo chamado, preencher e enviar | "O solicitante abre o chamado. Categoria e prioridade vêm da API. O chamado nasce com status Aberto." |
| 0:20 a 0:26 | Recarregar a página com F5 | "Recarrego a página e o chamado continua lá: o dado está no banco." |
| 0:26 a 0:36 | Trocar o usuário para a técnica, abrir o chamado, Assumir | "Trocando para o perfil da técnica, ela assume o chamado e o status vira Em Atendimento." |
| 0:36 a 0:44 | Escrever uma mensagem e marcar como resolvido | "Os dois lados conversam dentro do chamado. A técnica marca como resolvido." |
| 0:44 a 0:52 | Voltar ao solicitante e avaliar com 5 estrelas | "O solicitante avalia. A avaliação encerra o chamado, que passa para Concluído." |
| 0:52 a 1:00 | Linha do tempo no modal, depois a tabela `historico_status` no pgAdmin | "Cada transição gerou uma linha de auditoria no banco. Esse era o objetivo da prova de conceito." |

## O que não mostrar

- Código-fonte rolando na tela: consome tempo e não é o que a rubrica pede.
- Terminal subindo o Maven: mostre o sistema já no ar.
- Telas de Configurações e Sair: são decorativas nesta etapa.

## Publicação

- MP4, até 100 MB, direto no repositório em `docs/video/`, ou link não listado no YouTube colado no README.
- Se subir no GitHub, confira depois do push que o arquivo abre pelo navegador.
