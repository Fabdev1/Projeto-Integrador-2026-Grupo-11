# Ambiente de desenvolvimento

Objetivo desta página: qualquer pessoa do grupo (ou o professor) sobe o projeto do zero em menos de 10 minutos, sem depender da máquina de ninguém.

---

## 1. Tecnologias escolhidas e o motivo de cada uma

| Camada | Escolha | Versão | Por que |
|---|---|---|---|
| Banco | PostgreSQL | 16 | modelo da 1ª etapa é relacional, com 8 tabelas e chaves estrangeiras; `UNIQUE` em `avaliacoes.chamado_id` garante o 1:1 no próprio banco, e não só no código |
| Backend | Java + Spring Boot | 21 (LTS) + 3.3.4 | Spring Data JPA mapeia o DDL existente sem reescrever o modelo; Bean Validation devolve erro 400 com o nome do campo; é a stack vista no curso |
| Acesso a dados | Spring Data JPA / Hibernate | do parent 3.3.4 | repositórios por interface, transações declarativas com `@Transactional` |
| Frontend | HTML, CSS e JavaScript sem framework | ES2020 | a interface tem 4 telas e um modal; React, Angular ou Vue trariam build, bundler e node_modules para um ganho que não aparece na demonstração; a `fetch` nativa resolve as 9 chamadas do sistema |
| Servidor estático | qualquer um (Live Server, `npx serve`, `python -m http.server`) | | evita `file://`, que gera origem `null` e atrapalha a leitura de erro no console |
| Controle de versão | Git e GitHub | | branch por integrante e merge via pull request |

Decisão consciente sobre o frontend: manter JavaScript puro custa mais linhas de manipulação do DOM, mas o projeto roda abrindo um servidor estático, sem instalar nada. Para uma prova de conceito avaliada em vídeo de 1 minuto, isso vale mais que a ergonomia de um framework.

## 2. Pré-requisitos

- JDK 21 (`java -version` deve mostrar 21)
- Maven 3.9+ (ou o Maven embutido no IntelliJ IDEA)
- Docker Desktop **ou** PostgreSQL 16 instalado localmente
- Git
- Node.js (opcional, só para `npx serve`)

## 3. Subindo o banco

### Opção A, com Docker (recomendada)

Na raiz do repositório:

```bash
docker compose up -d
```

O `docker-compose.yml` sobe o PostgreSQL 16 na porta 5432 com o banco `chamados_db` já criado, e executa automaticamente, na primeira subida, nesta ordem:

1. `script_tabelas_postgresql.sql` (estrutura, índices e seed de categorias e usuários)
2. `banco/script_dados_demo.sql` (chamados de exemplo para a demonstração)

Conferir se subiu:

```bash
docker compose logs -f postgres
docker compose exec postgres psql -U postgres -d chamados_db -c "\dt"
```

O resultado esperado é a lista com as 8 tabelas.

Para recomeçar do zero (apaga os dados):

```bash
docker compose down -v && docker compose up -d
```

### Opção B, PostgreSQL instalado na máquina

```sql
CREATE DATABASE chamados_db;
```

Depois execute, na Query Tool do pgAdmin ou no DBeaver, conectado ao `chamados_db`:

1. `script_tabelas_postgresql.sql`
2. `banco/script_dados_demo.sql`

## 4. Subindo o backend

```bash
cd backend
mvn spring-boot:run
```

Pelo IntelliJ: abra a pasta `backend` como projeto Maven e rode a classe `ChamadosApplication`.

A API responde em `http://localhost:8080`. Teste rápido:

```bash
curl http://localhost:8080/api/categorias
curl http://localhost:8080/api/chamados/resumo
```

Se as credenciais do seu PostgreSQL forem diferentes, não edite o `application.properties` (isso gera conflito no Git). Use variáveis de ambiente, que o arquivo já lê:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/chamados_db
export DB_USER=postgres
export DB_PASSWORD=suasenha
mvn spring-boot:run
```

## 5. Subindo o frontend

```bash
cd frontend
npx serve -l 5500
```

Alternativas: extensão Live Server do VS Code (botão "Go Live"), ou `python -m http.server 5500`.

Acesse `http://localhost:5500`.

O endereço da API fica em `frontend/js/api.js`, na constante `API_BASE`. O padrão é `http://localhost:8080/api`.

## 6. Ordem de subida

```
docker compose up -d   ->   mvn spring-boot:run   ->   npx serve
     (banco)                    (API :8080)          (interface :5500)
```

A interface avisa na própria tela quando a API não responde, com o endereço que ela tentou alcançar. Não é preciso abrir o console para descobrir que o backend está fora.

## 7. Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `Connection refused` ao subir o backend | banco não está de pé | `docker compose ps` e confira o container `chamados-postgres` |
| `port 5432 already allocated` | já existe um PostgreSQL local na 5432 | pare o serviço local, ou mude a porta publicada no `docker-compose.yml` para `5433:5432` e ajuste `DB_URL` |
| Interface abre mas fica vazia com aviso de conexão | backend fora do ar ou `API_BASE` errado | suba o backend e confira a constante em `js/api.js` |
| Erro de CORS no console | frontend aberto direto do arquivo (`file://`) | sirva a pasta por HTTP, conforme a seção 5 |
| Tabelas não aparecem | scripts não rodaram na primeira subida do volume | `docker compose down -v && docker compose up -d` |
| Lombok não compila no IntelliJ | processamento de anotações desligado | Settings > Build > Compiler > Annotation Processors > Enable |

## 8. Padrão de trabalho no Git

- `main`: código estável, apenas por pull request.
- Uma branch por integrante, nomeada com o nome da pessoa.
- Commits no formato `tipo: descrição no imperativo` (`feat: registrar historico na troca de status`).
- Cada integrante envia os próprios commits, para que o histórico de colaboração exigido pelo enunciado fique visível na aba Insights > Contributors.
