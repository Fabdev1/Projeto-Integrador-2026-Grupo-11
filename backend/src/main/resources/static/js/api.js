/**
 * Cliente HTTP da API de chamados.
 *
 * Único ponto do frontend que sabe o endereço do backend e o formato de erro
 * que ele devolve. As telas chamam os métodos daqui e tratam só duas coisas:
 * o dado que voltou, ou um ErroApi com mensagem já pronta para exibir.
 */

// Endereço da API. Usa rota relativa se servido na mesma porta (8080), ou URL absoluta se servido em outra porta.
const API_BASE = window.location.port === "8080" ? "/api" : "http://localhost:8080/api";

class ErroApi extends Error {
  constructor(mensagem, status, campos) {
    super(mensagem);
    this.name = "ErroApi";
    this.status = status;
    this.campos = campos || null;
  }

  /** true quando o backend sequer respondeu (desligado, porta errada, CORS). */
  get semConexao() {
    return this.status === 0;
  }
}

async function requisitar(caminho, opcoes = {}) {
  let resposta;

  try {
    resposta = await fetch(API_BASE + caminho, {
      headers: { "Content-Type": "application/json" },
      ...opcoes,
    });
  } catch {
    throw new ErroApi(
      `A API não respondeu em ${API_BASE}. Suba o backend e recarregue a página.`,
      0
    );
  }

  if (resposta.status === 204) return null;

  const texto = await resposta.text();
  let corpo = null;

  if (texto) {
    try {
      corpo = JSON.parse(texto);
    } catch {
      corpo = null;
    }
  }

  if (!resposta.ok) {
    const mensagem =
      (corpo && (corpo.mensagem || corpo.erro)) ||
      `A API respondeu ${resposta.status}.`;
    throw new ErroApi(mensagem, resposta.status, corpo && corpo.campos);
  }

  return corpo;
}

const corpoJson = (dados) => ({ body: JSON.stringify(dados) });

const Api = {
  ErroApi,
  base: API_BASE,

  // ---------------------------------------------------------------- apoio
  listarCategorias: () => requisitar("/categorias"),
  listarUsuarios: (perfil) =>
    requisitar("/usuarios" + (perfil ? `?perfil=${perfil}` : "")),

  // -------------------------------------------------------------- chamados
  listarChamados(filtros = {}) {
    const params = new URLSearchParams();
    if (filtros.status) params.set("status", filtros.status);
    if (filtros.solicitanteId) params.set("solicitanteId", filtros.solicitanteId);
    if (filtros.tecnicoId) params.set("tecnicoId", filtros.tecnicoId);
    const query = params.toString();
    return requisitar("/chamados" + (query ? `?${query}` : ""));
  },

  resumo: () => requisitar("/chamados/resumo"),
  detalhes: (id) => requisitar(`/chamados/${id}/detalhes`),

  abrirChamado: (dados) =>
    requisitar("/chamados", { method: "POST", ...corpoJson(dados) }),

  alterarStatus: (id, dados) =>
    requisitar(`/chamados/${id}/status`, { method: "PUT", ...corpoJson(dados) }),

  atribuirTecnico: (id, tecnicoId) =>
    requisitar(`/chamados/${id}/tecnico/${tecnicoId}`, { method: "PUT" }),

  comentar: (id, dados) =>
    requisitar(`/chamados/${id}/comentarios`, { method: "POST", ...corpoJson(dados) }),

  avaliar: (id, dados) =>
    requisitar(`/chamados/${id}/avaliacao`, { method: "POST", ...corpoJson(dados) }),

  // ---------------------------------------------------------- notificações
  listarNotificacoes: (usuarioId) =>
    requisitar(`/notificacoes?usuarioId=${usuarioId}`),

  marcarNotificacaoLida: (id) =>
    requisitar(`/notificacoes/${id}/lida`, { method: "PUT" }),
};

window.Api = Api;
