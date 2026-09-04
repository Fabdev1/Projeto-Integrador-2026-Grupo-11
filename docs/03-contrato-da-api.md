# Contrato da API

Base: `http://localhost:8080/api`
Formato: JSON, UTF-8. Datas em ISO-8601 (`2026-09-04T14:32:10`).

---

## Endpoints

### Apoio

| Método | Rota | O que faz |
|---|---|---|
| GET | `/categorias` | lista as categorias em ordem alfabética; alimenta o select do formulário |
| GET | `/usuarios` | lista todos os usuários |
| GET | `/usuarios?perfil=TECNICO` | filtra por perfil (`SOLICITANTE`, `TECNICO`, `ADMIN`) |
| GET | `/usuarios/{id}` | busca um usuário |

### Chamados

| Método | Rota | O que faz |
|---|---|---|
| POST | `/chamados` | abre o chamado, grava o primeiro evento de histórico e notifica os técnicos |
| GET | `/chamados` | lista; aceita `status`, `solicitanteId` e `tecnicoId` combinados |
| GET | `/chamados/resumo` | contadores do painel, calculados no banco |
| GET | `/chamados/{id}` | busca um chamado |
| GET | `/chamados/{id}/detalhes` | chamado, histórico, conversa e avaliação em uma requisição |
| GET | `/chamados/{id}/historico` | só a linha do tempo |
| PUT | `/chamados/{id}/status` | muda o status, valida a transição e grava a auditoria |
| PUT | `/chamados/{id}/tecnico/{tecnicoId}` | atribui o técnico e move `ABERTO` para `EM_ANDAMENTO` |

### Conversa e avaliação

| Método | Rota | O que faz |
|---|---|---|
| GET | `/chamados/{id}/comentarios` | mensagens do chamado, da mais antiga para a mais nova |
| POST | `/chamados/{id}/comentarios` | registra a mensagem e notifica a outra parte |
| GET | `/chamados/{id}/avaliacao` | 404 quando ainda não houve avaliação |
| POST | `/chamados/{id}/avaliacao` | grava a nota e move o chamado para `FECHADO` |

### Notificações

| Método | Rota | O que faz |
|---|---|---|
| GET | `/notificacoes?usuarioId=1` | avisos do usuário, do mais recente ao mais antigo |
| GET | `/notificacoes/nao-lidas?usuarioId=1` | `{"total": 3}` |
| PUT | `/notificacoes/{id}/lida` | marca como lida |

---

## Mudança incompatível com a versão anterior

A troca de status era `PUT /api/chamados/{id}/status?status=RESOLVIDO`.

Passou a ser:

```http
PUT /api/chamados/7/status
Content-Type: application/json

{
  "statusNovo": "RESOLVIDO",
  "usuarioId": 2,
  "comentario": "Cabo de rede trocado e fila de impressão liberada."
}
```

Motivo: `historico_status.alterado_por` é `NOT NULL` no DDL. Sem o autor na requisição, a auditoria não teria como ser gravada. O `comentario` é opcional e, quando vem preenchido, vira uma mensagem na conversa do chamado, assinada por quem mudou o status.

## Máquina de estados

```
                 atribuir técnico            marcar resolvido
   ABERTO ──────────────────────> EM_ANDAMENTO ──────────────> RESOLVIDO
      │                                │                           │
      │ cancelar                       │ cancelar                  │ avaliar
      ▼                                ▼                           ▼
  CANCELADO                       CANCELADO                    FECHADO
                                                                   ▲
                                        reabrir (RESOLVIDO ────────┘
                                        volta para EM_ANDAMENTO)
```

Regras aplicadas em `ChamadoService.validarTransicao`:

- repetir o status atual devolve 409;
- `FECHADO` e `CANCELADO` são finais;
- `EM_ANDAMENTO` exige técnico atribuído;
- `RESOLVIDO` só vem de `EM_ANDAMENTO`.

## Formato de erro

Toda falha sai no mesmo formato, tratada em `GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-09-04T14:32:10.412",
  "status": 409,
  "erro": "Operação não permitida",
  "mensagem": "Este chamado já foi avaliado.",
  "caminho": "/api/chamados/7/avaliacao",
  "campos": null
}
```

Quando a falha é de validação, `campos` traz o problema por campo:

```json
{
  "status": 400,
  "erro": "Requisição inválida",
  "mensagem": "Confira os campos destacados.",
  "campos": {
    "titulo": "O título é obrigatório",
    "categoriaId": "A categoria é obrigatória"
  }
}
```

| Status | Quando aparece |
|---|---|
| 200 / 201 | sucesso |
| 400 | validação de campo ou parâmetro fora do enum |
| 404 | id que não existe |
| 409 | regra de negócio ou restrição do banco (avaliar duas vezes, transição inválida) |
| 500 | falha inesperada, com stack trace no log da aplicação |

## Regras que devolvem 409

| Situação | Mensagem |
|---|---|
| avaliar chamado que não está resolvido | "Só é possível avaliar um chamado resolvido. Status atual: ..." |
| avaliar o mesmo chamado duas vezes | "Este chamado já foi avaliado." |
| avaliar chamado de outra pessoa | "A avaliação é de quem abriu o chamado." |
| atribuir alguém com perfil `SOLICITANTE` | "... tem perfil SOLICITANTE e não pode assumir chamados." |
| comentar em chamado fechado ou cancelado | "Chamado fechado não aceita novos comentários." |
