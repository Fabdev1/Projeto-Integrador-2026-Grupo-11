# 📌 Projeto Integrador - Banco de Dados (PostgreSQL)

Este repositório contém a **modelagem inicial do banco de dados relacional (PostgreSQL)** criada para o Sistema de Chamados Técnicos.

---

## 🗄️ Estrutura das Tabelas Criadas (PostgreSQL)

O script DDL inclui a criação das 8 tabelas do banco de dados:

1. **`usuarios`**: Tabela de cadastro dos usuários (Solicitante, Técnico e Admin).
2. **`categorias`**: Tabela com as categorias de atendimento (Hardware, Software, Redes, etc.).
3. **`chamados`**: Tabela principal de solicitações de suporte.
4. **`historico_status`**: Tabela de auditoria para acompanhamento das mudanças de status.
5. **`comentarios`**: Tabela de mensagens e interações nos chamados.
6. **`anexos`**: Tabela de registros de arquivos anexados.
7. **`notificacoes`**: Tabela de avisos e alertas do sistema.
8. **`avaliacoes`**: Tabela de avaliação de atendimento (1 a 5).

---

## 📋 Como Executar o Script SQL

1. No seu gerenciador do PostgreSQL (**pgAdmin**, **DBeaver** ou **Navicat**), crie o banco de dados:
   ```sql
   CREATE DATABASE chamados_db;
   ```
2. Abra a Query Tool no banco `chamados_db` e execute o arquivo:
   📄 [`script_tabelas_postgresql.sql`](./script_tabelas_postgresql.sql)

---

### ✒️ Autoria / Contribuição
Modelagem do Banco de Dados PostgreSQL e Script SQL desenvolvidos por:  
**Tainá Monique**
