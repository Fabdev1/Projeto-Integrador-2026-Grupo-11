# Como enviar esta parte para o repositório

Leia antes de copiar os arquivos. Alguns substituem arquivos existentes.

---

## Arquivos novos

Copiar direto, nenhum conflito.

```
docker-compose.yml
banco/script_dados_demo.sql

docs/01-prova-de-conceito.md
docs/02-ambiente-de-desenvolvimento.md
docs/03-contrato-da-api.md
docs/04-roteiro-video.md
docs/api/chamados.http

backend/src/main/java/com/chamados/exception/ErroResposta.java
backend/src/main/java/com/chamados/exception/GlobalExceptionHandler.java
backend/src/main/java/com/chamados/exception/RecursoNaoEncontradoException.java
backend/src/main/java/com/chamados/exception/RegraDeNegocioException.java

backend/src/main/java/com/chamados/repository/AnexoRepository.java
backend/src/main/java/com/chamados/repository/AvaliacaoRepository.java
backend/src/main/java/com/chamados/repository/ComentarioRepository.java
backend/src/main/java/com/chamados/repository/HistoricoStatusRepository.java
backend/src/main/java/com/chamados/repository/NotificacaoRepository.java

backend/src/main/java/com/chamados/dto/AlterarStatusRequestDTO.java
backend/src/main/java/com/chamados/dto/AvaliacaoRequestDTO.java
backend/src/main/java/com/chamados/dto/AvaliacaoResponseDTO.java
backend/src/main/java/com/chamados/dto/CategoriaResponseDTO.java
backend/src/main/java/com/chamados/dto/ChamadoDetalheResponseDTO.java
backend/src/main/java/com/chamados/dto/ComentarioRequestDTO.java
backend/src/main/java/com/chamados/dto/ComentarioResponseDTO.java
backend/src/main/java/com/chamados/dto/HistoricoStatusResponseDTO.java
backend/src/main/java/com/chamados/dto/NotificacaoResponseDTO.java
backend/src/main/java/com/chamados/dto/ResumoChamadosDTO.java
backend/src/main/java/com/chamados/dto/UsuarioResponseDTO.java

backend/src/main/java/com/chamados/service/AvaliacaoService.java
backend/src/main/java/com/chamados/service/CategoriaService.java
backend/src/main/java/com/chamados/service/ComentarioService.java
backend/src/main/java/com/chamados/service/HistoricoStatusService.java
backend/src/main/java/com/chamados/service/NotificacaoService.java
backend/src/main/java/com/chamados/service/UsuarioService.java

backend/src/main/java/com/chamados/controller/AvaliacaoController.java
backend/src/main/java/com/chamados/controller/CategoriaController.java
backend/src/main/java/com/chamados/controller/ComentarioController.java
backend/src/main/java/com/chamados/controller/NotificacaoController.java
backend/src/main/java/com/chamados/controller/UsuarioController.java

frontend/style-integracao.css
frontend/js/api.js
frontend/js/dominio.js
frontend/js/app.js
```

## Arquivos que substituem versões existentes

Avise as pessoas do grupo antes de sobrescrever.

| Arquivo | O que mudou | Quem escreveu a versão anterior |
|---|---|---|
| `backend/.../repository/ChamadoRepository.java` | acréscimo de `countByStatus`, `countByStatusIn` e duas consultas com fetch join. Nenhum método anterior foi removido. | Tainá |
| `backend/.../service/ChamadoService.java` | auditoria em toda transição, máquina de estados, notificações, filtros, detalhe e resumo. Injeção por construtor no lugar de `@Autowired` em campo. | Tainá |
| `backend/.../controller/ChamadoController.java` | novas rotas (`/resumo`, `/detalhes`, `/historico`, filtros) e a rota de status passando a receber corpo JSON. | Tainá |
| `backend/src/main/resources/application.properties` | credenciais lidas de variáveis de ambiente, datas em ISO-8601, `open-in-view=false`. | Tainá |
| `frontend/index.html` | selects alimentados pela API, seletor de perfil, modal de detalhes. Estrutura e classes CSS preservadas. | Helen |
| `README.md` | integrantes, instruções de execução, estrutura, API e modelo. | Tainá |

## Arquivo a apagar

```
frontend/script.js
```

Continha o array fixo com 6 chamados e foi substituído por `frontend/js/app.js`. Deixá-lo no repositório confunde quem for ler, porque nada mais o carrega.

---

## Sequência de commits

Um commit por assunto deixa o histórico de colaboração legível, que é o que a rubrica avalia na aba Insights.

```bash
git checkout main
git pull origin main

# Se main ainda não tem o trabalho da Tainá, parta da branch dela:
# git checkout taina-monique && git pull origin taina-monique

git checkout -b SEU-NOME

# 1. Documentação da revisita e da prova de conceito
git add docs/01-prova-de-conceito.md docs/04-roteiro-video.md
git commit -m "docs: registrar revisita do projeto e recorte da prova de conceito"

# 2. Ambiente reproduzível
git add docker-compose.yml docs/02-ambiente-de-desenvolvimento.md banco/script_dados_demo.sql \
        backend/src/main/resources/application.properties
git commit -m "chore: subir ambiente com Docker e carga automatica dos scripts"

# 3. Tratamento de erros
git add backend/src/main/java/com/chamados/exception/
git commit -m "feat: padronizar respostas de erro da API em 400, 404 e 409"

# 4. Repositórios que faltavam
git add backend/src/main/java/com/chamados/repository/
git commit -m "feat: completar os repositorios JPA das 8 tabelas do modelo"

# 5. Auditoria de status
git add backend/src/main/java/com/chamados/service/HistoricoStatusService.java \
        backend/src/main/java/com/chamados/service/ChamadoService.java \
        backend/src/main/java/com/chamados/dto/AlterarStatusRequestDTO.java \
        backend/src/main/java/com/chamados/dto/HistoricoStatusResponseDTO.java
git commit -m "feat: gravar historico_status em toda transicao do chamado"

# 6. Conversa, avaliação e notificações
git add backend/src/main/java/com/chamados/service/ \
        backend/src/main/java/com/chamados/dto/ \
        backend/src/main/java/com/chamados/controller/
git commit -m "feat: adicionar comentarios, avaliacao e notificacoes do chamado"

# 7. Integração da interface com a API
git add frontend/
git rm frontend/script.js
git commit -m "feat: consumir a API real no frontend no lugar dos dados fixos"

# 8. Documentação final
git add README.md docs/03-contrato-da-api.md docs/api/
git commit -m "docs: documentar contrato da API e atualizar o README do grupo"

git push -u origin SEU-NOME
```

Depois do push, abra o pull request para `main` e marque as pessoas do grupo para revisar. O enunciado pede o histórico de colaboração de cada membro, então evite que uma pessoa só suba o trabalho de todo mundo.

---

## Antes de considerar pronto

- [ ] `docker compose up -d` sobe o banco e as 8 tabelas aparecem em `\dt`
- [ ] `mvn spring-boot:run` sobe sem erro de compilação
- [ ] `curl http://localhost:8080/api/categorias` devolve as 4 categorias
- [ ] a interface abre em `http://localhost:5500` sem o aviso vermelho de conexão
- [ ] abrir um chamado, recarregar a página e ele continua na lista
- [ ] os 7 critérios de aceite da seção 4 de `docs/01-prova-de-conceito.md` passam
- [ ] README com os nomes de todos os integrantes preenchidos
- [ ] link do vídeo no README
- [ ] todos os integrantes aparecem em Insights > Contributors
