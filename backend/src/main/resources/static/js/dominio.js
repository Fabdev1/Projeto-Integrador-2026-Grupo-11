/**
 * Tradução entre o vocabulário do banco e o vocabulário da tela.
 *
 * O backend trabalha com os enums do modelo (ABERTO, EM_ANDAMENTO, URGENTE).
 * A interface já tinha um vocabulário próprio, com classes CSS derivadas do
 * rótulo ("Em Atendimento" vira .status-em-atendimento). Este arquivo é a
 * ponte, para que nem o backend adote nome de tela nem o CSS existente precise
 * ser reescrito.
 */

(function () {
const STATUS = {
  ABERTO: { rotulo: "Aberto", classe: "status-aberto" },
  EM_ANDAMENTO: { rotulo: "Em Atendimento", classe: "status-em-atendimento" },
  RESOLVIDO: { rotulo: "Resolvido", classe: "status-resolvido" },
  FECHADO: { rotulo: "Concluído", classe: "status-concluido" },
  CANCELADO: { rotulo: "Cancelado", classe: "status-cancelado" },
};

const PRIORIDADE = {
  BAIXA: { rotulo: "Baixa", classe: "priority-baixa" },
  MEDIA: { rotulo: "Média", classe: "priority-media" },
  ALTA: { rotulo: "Alta", classe: "priority-alta" },
  URGENTE: { rotulo: "Crítica", classe: "priority-critica" },
};

const PERFIL = {
  SOLICITANTE: "Solicitante",
  TECNICO: "Técnico",
  ADMIN: "Administrador",
};

/** Status que a tela trata como atendimento encerrado. */
const CONCLUIDOS = ["RESOLVIDO", "FECHADO"];

function rotuloStatus(status) {
  return (STATUS[status] || { rotulo: status }).rotulo;
}

function rotuloPrioridade(prioridade) {
  return (PRIORIDADE[prioridade] || { rotulo: prioridade }).rotulo;
}

function selo(mapa, chave) {
  const item = mapa[chave] || { rotulo: chave, classe: "" };
  return `<span class="badge ${item.classe}">${item.rotulo}</span>`;
}

const seloStatus = (status) => selo(STATUS, status);
const seloPrioridade = (prioridade) => selo(PRIORIDADE, prioridade);

/** "#CH-0007", para manter a identidade visual da primeira versão da tela. */
function codigo(id) {
  return "#CH-" + String(id).padStart(4, "0");
}

function data(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleDateString("pt-BR");
}

function dataHora(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

/** Evita que texto vindo do banco seja interpretado como HTML. */
function escapar(texto) {
  if (texto === null || texto === undefined) return "";
  return String(texto)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function estrelas(nota) {
  return "★".repeat(nota) + "☆".repeat(5 - nota);
}

window.Dominio = {
  STATUS,
  PRIORIDADE,
  PERFIL,
  CONCLUIDOS,
  rotuloStatus,
  rotuloPrioridade,
  seloStatus,
  seloPrioridade,
  codigo,
  data,
  dataHora,
  escapar,
  estrelas,
};
})();
