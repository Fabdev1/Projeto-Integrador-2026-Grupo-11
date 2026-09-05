# HelpDesk, Sistema de Chamados Técnicos

Projeto Integrador, 4º semestre TADS/TSI, Senac. **Grupo 11, Segunda Etapa.**
Prof. Gustavo Calixto.

Prova de conceito do percurso completo de um chamado de suporte: abertura pelo solicitante, atribuição de técnico, conversa entre as partes, resolução com auditoria de cada transição e avaliação do atendimento.

---

## Integrantes

| Nome | Usuário no GitHub | O que entregou nesta etapa |
|---|---|---|
| Tainá Monique | | Modelagem PostgreSQL, script DDL, entidades JPA e estrutura base do backend |
| Helen | | Interface do HelpDesk: telas, layout e estilos |
| Agatha Amorim | | Integração frontend e backend, tratamento de erros, auditoria de status, conversa, avaliação, ambiente Docker e documentação |
| Fabricio Lima | | Testes de Segurança da Informação, análise da aplicação para identificação de vulnerabilidades, validação de autenticação e autorização, controle de acesso, proteção de dados e tratamento de requisições indevidas.
 |

> Complete os nomes e os usuários do GitHub antes de enviar. A rubrica exige o README preenchido com os integrantes, e todos precisam aparecer na aba Insights > Contributors.

## Vídeo de apresentação



---

## Como rodar

Três passos, na ordem. O detalhamento está em [`docs/02-ambiente-de-desenvolvimento.md`](docs/02-ambiente-de-desenvolvimento.md).

### 1. Banco

```bash
docker compose up -d
```

Sobe o PostgreSQL 16 na porta 5432 e carrega, na primeira subida, o `script_tabelas_postgresql.sql` (8 tabelas, índices, seed) e o `banco/script_dados_demo.sql` (chamados de exemplo).

Sem Docker: crie o banco `chamados_db` e execute os dois scripts nessa ordem.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

API em `http://localhost:8080`. Teste: `curl http://localhost:8080/api/categorias`.

Credenciais diferentes das padrão? Use variáveis de ambiente, sem editar o `application.properties`:

```bash
export DB_USER=postgres DB_PASSWORD=suasenha
```

### 3. Frontend

```bash
cd frontend
npx serve -l 5500
```

Acesse `http://localhost:5500`. Também funciona com a extensão Live Server do VS Code.

> Sirva a pasta por HTTP. Abrir o `index.html` direto do disco gera origem `null` e atrapalha a leitura dos erros no console.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Banco | PostgreSQL 16 |
| Backend | Java 21, Spring Boot 3.3.4, Spring Data JPA, Bean Validation, Lombok |
| Frontend | HTML, CSS e JavaScript, sem framework, usando `fetch` |
| Ambiente | Docker Compose |
| Build | Maven |

---

## Estrutura

```
.
├── docker-compose.yml               PostgreSQL com carga automática dos scripts
├── script_tabelas_postgresql.sql    DDL das 8 tabelas, índices e seed
├── banco/
│   └── script_dados_demo.sql        chamados de exemplo para a demonstração
├── backend/
│   └── src/main/java/com/chamados/
│       ├── config/                  liberação de CORS
│       ├── controller/              6 controllers REST
│       ├── domain/entity/           8 entidades JPA
│       ├── domain/enums/            PerfilUsuario, StatusChamado, PrioridadeChamado
│       ├── dto/                     objetos de entrada e saída da API
│       ├── exception/               404, 409 e 400 com o campo que falhou
│       ├── repository/              8 repositórios JPA
│       └── service/                 regras de negócio
├── frontend/
│   ├── index.html
│   ├── style.css                    identidade visual das telas
│   ├── style-integracao.css         componentes acrescentados na integração
│   └── js/
│       ├── api.js                   cliente HTTP e tratamento de erro
│       ├── dominio.js               tradução entre enums da API e rótulos da tela
│       └── app.js                   telas, modal, conversa e avaliação
└── docs/
    ├── 01-prova-de-conceito.md      revisita do projeto e recorte da PoC
    ├── 02-ambiente-de-desenvolvimento.md
    ├── 03-contrato-da-api.md
    ├── 04-roteiro-video.md
    └── api/chamados.http            roteiro de teste da API
```

---

## O que o sistema faz

- Abertura de chamado com categoria e prioridade vindas do banco.
- Painel com contadores calculados por consulta, não por contagem na tela.
- Fila de atendimento para o técnico e lista pessoal para o solicitante.
- Atribuição de técnico, com o status indo de `ABERTO` para `EM_ANDAMENTO`.
- Conversa entre solicitante e técnico dentro do chamado.
- Transição de status validada por uma máquina de estados, com auditoria de quem mudou, quando e de qual status para qual.
- Avaliação de 1 a 5 pelo solicitante, uma por chamado, encerrando o atendimento.
- Notificações internas geradas pelo sistema a cada evento.
- Erros com mensagem legível na interface, inclusive quando o backend está fora do ar.

O upload de anexos e o login com senha ficaram fora do recorte desta etapa. O motivo de cada exclusão está em [`docs/01-prova-de-conceito.md`](docs/01-prova-de-conceito.md).

## API

Referência completa em [`docs/03-contrato-da-api.md`](docs/03-contrato-da-api.md). Para testar sem a interface, use [`docs/api/chamados.http`](docs/api/chamados.http).

```
GET    /api/categorias
GET    /api/usuarios?perfil=TECNICO
POST   /api/chamados
GET    /api/chamados?status=ABERTO&solicitanteId=1
GET    /api/chamados/resumo
GET    /api/chamados/{id}/detalhes
PUT    /api/chamados/{id}/status
PUT    /api/chamados/{id}/tecnico/{tecnicoId}
POST   /api/chamados/{id}/comentarios
POST   /api/chamados/{id}/avaliacao
GET    /api/notificacoes?usuarioId=1
```

## Modelo de dados

As 8 tabelas da primeira etapa, com os relacionamentos preservados:

```
usuarios ──1:N──> chamados <──N:1── categorias
   │                  │
   │                  ├──1:N──> historico_status
   │                  ├──1:N──> comentarios
   │                  ├──1:N──> anexos
   │                  ├──1:N──> notificacoes
   │                  └──1:1──> avaliacoes
   └──1:N──> (autor em historico_status, comentarios, anexos, notificacoes, avaliacoes)
```

A tabela `usuarios` guarda os três perfis (`SOLICITANTE`, `TECNICO`, `ADMIN`), e um chamado aponta duas vezes para ela: uma como solicitante, outra como técnico.

## Como conferir a entrega

Com o banco recém-carregado, os critérios de aceite estão listados em [`docs/01-prova-de-conceito.md`](docs/01-prova-de-conceito.md), seção 4. O caminho mais rápido:

```bash
curl http://localhost:8080/api/chamados/resumo
docker compose exec postgres psql -U postgres -d chamados_db \
  -c "SELECT status, COUNT(*) FROM chamados GROUP BY status;"
```

Os dois números precisam bater.
