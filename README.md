# 📌 Projeto Integrador - Backend & Banco de Dados (PostgreSQL + Spring Boot)

Este repositório contém a **modelagem do banco de dados relacional (PostgreSQL)** e a **estrutura base do backend em Java 21 / Spring Boot**, desenvolvidos como parte da 2ª Etapa do Projeto Integrador (Senac).

---

## 🛠️ Tecnologias & Configurações Utilizadas

- **Linguagem Backend**: Java 21 (LTS)
- **Framework Backend**: Spring Boot 3.3.x (Spring Data JPA, Spring Web, Spring Validation, Lombok)
- **Banco de Dados**: PostgreSQL 15+ (`chamados_db`)
- **Arquitetura**: Padrão em Camadas (Layered Architecture / REST API)
- **Gerenciador de Dependências**: Apache Maven

---

## 🗄️ 1. Banco de Dados PostgreSQL (`script_tabelas_postgresql.sql`)

Script DDL completo com a criação das **8 tabelas do sistema**, chaves primárias, chaves estrangeiras, restrições e dados de teste:

1. **`usuarios`**: Cadastro de Solicitantes, Técnicos e Administradores.
2. **`categorias`**: Categorias de atendimento (Hardware, Software, Redes, etc.).
3. **`chamados`**: Tabela principal de solicitações de suporte.
4. **`historico_status`**: Registro de auditoria das mudanças de status.
5. **`comentarios`**: Mensagens e interações registradas no chamado.
6. **`anexos`**: Registros de arquivos anexados aos chamados.
7. **`notificacoes`**: Avisos e alertas para os usuários.
8. **`avaliacoes`**: Nota de satisfação (1 a 5) após o encerramento do chamado (**1:1** via `UNIQUE`).

---

## ⚙️ 2. Estrutura e Desenvolvimento do Backend Java (`backend/`)

A aplicação backend foi organizada na pasta `backend/` seguindo a **Arquitetura em Camadas**:

```
backend/src/main/java/com/chamados/
│
├── config/        <-- Liberação de CORS (CorsConfig.java)
├── controller/    <-- Controllers REST (ChamadoController.java - rota /api/chamados)
├── domain/        
│   ├── entity/    <-- 8 Entidades JPA (Usuario, Chamado, Categoria, etc.)
│   └── enums/     <-- Enums (PerfilUsuario, StatusChamado, PrioridadeChamado)
├── dto/           <-- DTOs de Entrada e Saída (ChamadoRequestDTO, ChamadoResponseDTO)
├── repository/    <-- 8 Repositórios JPA (ChamadoRepository, UsuarioRepository, etc.)
└── service/       <-- Regras de Negócio (ChamadoService.java)
```

### Funcionalidades do Backend Desenvolvidas:
- ✅ **Mapeamento de Entidades JPA**: Todas as 8 tabelas mapeadas com anotações `@Entity`, `@ManyToOne`, `@OneToOne` e `@Enumerated`.
- ✅ **Abertura de Chamados**: API REST para registro de novas solicitações.
- ✅ **Listagem e Busca**: Consulta de chamados por ID e listagem geral.
- ✅ **Atribuição de Técnico**: Vinculação automática de técnicos aos chamados.
- ✅ **Alteração de Status**: Transição de status do chamado (`ABERTO`, `EM_ANDAMENTO`, `RESOLVIDO`).
- ✅ **Configuração de CORS**: Acesso liberado para a integração com o Frontend.

---

## 📋 Como Executar o Projeto Localmente

1. **Carregar o Banco de Dados (PostgreSQL)**:
   - Crie o banco `chamados_db` no PostgreSQL.
   - Execute o arquivo [`script_tabelas_postgresql.sql`](./script_tabelas_postgresql.sql).

2. **Iniciar o Servidor Spring Boot**:
   - Abra a pasta `backend` no **IntelliJ IDEA**.
   - Confira as credenciais do PostgreSQL em `backend/src/main/resources/application.properties`.
   - Execute a classe `ChamadosApplication.java`.
   - A API estará disponível em: `http://localhost:8080/api/chamados`.

---

### ✒️ Autoria & Contribuição
Modelagem do Banco de Dados PostgreSQL, Script SQL DDL e Estruturação do Backend Spring Boot desenvolvidos por:  
**Tainá Monique**
