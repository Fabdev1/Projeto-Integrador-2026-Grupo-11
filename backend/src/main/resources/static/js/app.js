/**
 * Aplicação do HelpDesk.
 *
 * A versão anterior mantinha os chamados em um array fixo no código. Aqui todo
 * dado vem do PostgreSQL pela API REST: recarregar a página mantém o que foi
 * criado, que é o critério de aceite número 2 da prova de conceito.
 */

const {
  STATUS,
  PRIORIDADE,
  PERFIL,
  CONCLUIDOS,
  rotuloStatus,
  seloStatus,
  seloPrioridade,
  codigo,
  data,
  dataHora,
  escapar,
  estrelas,
} = window.Dominio;

// ---------------------------------------------------------------- estado

const estado = {
  usuarios: [],
  categorias: [],
  usuarioAtual: null,
  chamados: [],
  resumo: { abertos: 0, emAndamento: 0, concluidos: 0, total: 0 },
  detalheAberto: null,
  notaSelecionada: 0,
};

const $ = (seletor) => document.querySelector(seletor);
const $$ = (seletor) => Array.from(document.querySelectorAll(seletor));

const ehTecnico = () =>
  estado.usuarioAtual && estado.usuarioAtual.perfil !== "SOLICITANTE";

// ------------------------------------------------------------ mensagens

function avisar(mensagem, tipo = "sucesso") {
  const toast = $("#toast");
  toast.textContent = mensagem;
  toast.classList.remove("toast-erro", "toast-sucesso");
  toast.classList.add(tipo === "erro" ? "toast-erro" : "toast-sucesso");
  toast.classList.add("show");
  clearTimeout(avisar.timer);
  avisar.timer = setTimeout(() => toast.classList.remove("show"), 3200);
}

function mostrarFalha(erro) {
  const banner = $("#avisoConexao");
  if (erro instanceof Api.ErroApi && erro.semConexao) {
    banner.textContent = erro.message;
    banner.hidden = false;
    return;
  }
  avisar(erro.message || "Não foi possível concluir a ação.", "erro");
}

function limparFalhaDeConexao() {
  $("#avisoConexao").hidden = true;
}

// -------------------------------------------------------------- navegação

const paginas = {
  dashboard: ["Dashboard", "Acompanhe suas solicitações de suporte."],
  novo: ["Novo chamado", "Registre uma nova solicitação de suporte."],
  chamados: ["Meus chamados", "Consulte e acompanhe suas solicitações."],
  historico: ["Histórico", "Veja os atendimentos já finalizados."],
};

function abrirPagina(id) {
  $$(".page").forEach((p) => p.classList.remove("active"));
  $$(".nav-item[data-page]").forEach((n) => n.classList.remove("active"));

  $("#" + id).classList.add("active");
  const nav = $(`.nav-item[data-page="${id}"]`);
  if (nav) nav.classList.add("active");

  $("#pageTitle").textContent = paginas[id][0];
  $("#pageSubtitle").textContent = paginas[id][1];
  $("#sidebar").classList.remove("open");
}

// ------------------------------------------------------------ carregamento

async function carregarBase() {
  const [usuarios, categorias] = await Promise.all([
    Api.listarUsuarios(),
    Api.listarCategorias(),
  ]);

  estado.usuarios = usuarios;
  estado.categorias = categorias;
  estado.usuarioAtual =
    usuarios.find((u) => u.perfil === "SOLICITANTE") || usuarios[0] || null;

  montarSeletorDeUsuario();
  montarSelectDeCategorias();
}

async function carregarChamados() {
  if (!estado.usuarioAtual) return;

  // Solicitante enxerga o que abriu. Técnico e administrador enxergam a fila.
  const filtros = ehTecnico()
    ? {}
    : { solicitanteId: estado.usuarioAtual.id };

  const [chamados, resumo] = await Promise.all([
    Api.listarChamados(filtros),
    Api.resumo(),
  ]);

  estado.chamados = chamados;
  estado.resumo = resumo;

  desenharContadores();
  desenharRecentes();
  desenharLista();
  desenharHistorico();
}

async function atualizarTudo() {
  try {
    await carregarChamados();
    limparFalhaDeConexao();
  } catch (erro) {
    mostrarFalha(erro);
  }
}

// ----------------------------------------------------------- seletor de uso

function montarSeletorDeUsuario() {
  const select = $("#usuarioAtual");
  select.innerHTML = estado.usuarios
    .map(
      (u) =>
        `<option value="${u.id}">${escapar(u.nome)} (${PERFIL[u.perfil] || u.perfil})</option>`
    )
    .join("");

  if (estado.usuarioAtual) select.value = estado.usuarioAtual.id;
  desenharIdentidade();
}

function desenharIdentidade() {
  const u = estado.usuarioAtual;
  if (!u) return;

  const iniciais = u.nome
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join("");

  $("#avatarUsuario").textContent = iniciais;
  $("#perfilUsuario").textContent = PERFIL[u.perfil] || u.perfil;
  $("#tituloLista").textContent = ehTecnico() ? "Fila de atendimento" : "Meus chamados";
  $(".nav-item[data-page='chamados'] span").textContent = ehTecnico()
    ? "Fila de atendimento"
    : "Meus chamados";
}

function montarSelectDeCategorias() {
  $("#categoria").innerHTML =
    '<option value="">Selecione</option>' +
    estado.categorias
      .map((c) => `<option value="${c.id}">${escapar(c.nome)}</option>`)
      .join("");
}

// -------------------------------------------------------------- dashboard

function desenharContadores() {
  $("#countAbertos").textContent = estado.resumo.abertos;
  $("#countAtendimento").textContent = estado.resumo.emAndamento;
  $("#countConcluidos").textContent = estado.resumo.concluidos;
}

function desenharRecentes() {
  const corpo = $("#recentTickets");

  if (!estado.chamados.length) {
    corpo.innerHTML =
      '<tr><td colspan="6" class="vazio">Nenhum chamado registrado. Abra o primeiro em "Novo chamado".</td></tr>';
    return;
  }

  corpo.innerHTML = estado.chamados
    .slice(0, 5)
    .map(
      (c) => `
      <tr data-id="${c.id}" class="linha-clicavel">
        <td><strong>${codigo(c.id)}</strong></td>
        <td>${escapar(c.titulo)}</td>
        <td>${escapar(c.nomeCategoria)}</td>
        <td>${seloPrioridade(c.prioridade)}</td>
        <td>${seloStatus(c.status)}</td>
        <td>${data(c.dataCriacao)}</td>
      </tr>`
    )
    .join("");

  corpo.querySelectorAll("tr[data-id]").forEach((linha) => {
    linha.addEventListener("click", () => abrirDetalhes(Number(linha.dataset.id)));
  });
}

// ---------------------------------------------------------------- lista

function desenharLista() {
  const busca = $("#searchInput").value.trim().toLowerCase();
  const filtro = $("#statusFilter").value;

  const filtrados = estado.chamados.filter((c) => {
    const texto = `${codigo(c.id)} ${c.titulo} ${c.nomeCategoria}`.toLowerCase();
    const casaTexto = texto.includes(busca);
    const casaStatus = filtro === "Todos" || c.status === filtro;
    return casaTexto && casaStatus;
  });

  const lista = $("#ticketList");

  if (!filtrados.length) {
    lista.innerHTML = estado.chamados.length
      ? '<div class="empty">Nenhum chamado com esse filtro. Limpe a busca para ver todos.</div>'
      : '<div class="empty">Nenhum chamado por aqui ainda.</div>';
    return;
  }

  lista.innerHTML = filtrados
    .map(
      (c) => `
      <article class="ticket-card">
        <div>
          <h4>${escapar(c.titulo)}</h4>
          <p>${codigo(c.id)} · ${data(c.dataCriacao)} · ${escapar(c.nomeSolicitante)}</p>
        </div>
        <div>
          <span class="meta-label">Categoria</span>
          <strong>${escapar(c.nomeCategoria)}</strong>
        </div>
        <div>
          <span class="meta-label">Técnico</span>
          <strong>${c.nomeTecnico ? escapar(c.nomeTecnico) : "A definir"}</strong>
        </div>
        <div>
          <span class="meta-label">Prioridade</span>
          ${seloPrioridade(c.prioridade)}
        </div>
        <div>
          <span class="meta-label">Status</span>
          ${seloStatus(c.status)}
        </div>
        <button class="secondary-btn" data-detalhe="${c.id}">Ver detalhes</button>
      </article>`
    )
    .join("");

  lista.querySelectorAll("[data-detalhe]").forEach((botao) => {
    botao.addEventListener("click", () => abrirDetalhes(Number(botao.dataset.detalhe)));
  });
}

// ------------------------------------------------------------- histórico

function desenharHistorico() {
  const encerrados = estado.chamados.filter((c) => CONCLUIDOS.includes(c.status));
  const alvo = $("#historyList");

  if (!encerrados.length) {
    alvo.innerHTML =
      '<div class="empty">Os atendimentos finalizados aparecem aqui.</div>';
    return;
  }

  alvo.innerHTML = encerrados
    .map(
      (c) => `
      <div class="timeline-item">
        <span class="timeline-dot"></span>
        <h4>${codigo(c.id)} · ${escapar(c.titulo)}</h4>
        <p>${data(c.dataAtualizacao || c.dataCriacao)} · ${escapar(c.nomeCategoria)} · ${rotuloStatus(c.status)}</p>
        <button class="text-btn" data-detalhe-hist="${c.id}">Ver linha do tempo</button>
      </div>`
    )
    .join("");

  alvo.querySelectorAll("[data-detalhe-hist]").forEach((botao) => {
    botao.addEventListener("click", () =>
      abrirDetalhes(Number(botao.dataset.detalheHist))
    );
  });
}

// ---------------------------------------------------------- novo chamado

async function enviarChamado(evento) {
  evento.preventDefault();

  const botao = $("#btnEnviarChamado");
  botao.disabled = true;
  botao.textContent = "Enviando...";

  try {
    const criado = await Api.abrirChamado({
      titulo: $("#titulo").value.trim(),
      descricao: $("#descricao").value.trim(),
      categoriaId: Number($("#categoria").value),
      solicitanteId: estado.usuarioAtual.id,
      prioridade: $("#prioridade").value,
    });

    $("#ticketForm").reset();
    await atualizarTudo();
    abrirPagina("dashboard");
    avisar(`Chamado ${codigo(criado.id)} aberto.`);
  } catch (erro) {
    mostrarFalha(erro);
  } finally {
    botao.disabled = false;
    botao.textContent = "Enviar chamado";
  }
}

// -------------------------------------------------------------- detalhes

async function abrirDetalhes(id) {
  const modal = $("#modalDetalhe");
  modal.hidden = false;
  document.body.classList.add("sem-rolagem");
  $("#modalCorpo").innerHTML = '<p class="carregando">Carregando o chamado...</p>';

  try {
    estado.detalheAberto = await Api.detalhes(id);
    estado.notaSelecionada = 0;
    desenharDetalhes();
  } catch (erro) {
    $("#modalCorpo").innerHTML = `<p class="erro-inline">${escapar(erro.message)}</p>`;
  }
}

function fecharDetalhes() {
  $("#modalDetalhe").hidden = true;
  document.body.classList.remove("sem-rolagem");
  estado.detalheAberto = null;
}

function desenharDetalhes() {
  const { chamado, historico, comentarios, avaliacao } = estado.detalheAberto;

  $("#modalTitulo").textContent = `${codigo(chamado.id)} · ${chamado.titulo}`;
  $("#modalCorpo").innerHTML = `
    <div class="detalhe-meta">
      ${seloStatus(chamado.status)}
      ${seloPrioridade(chamado.prioridade)}
      <span class="meta-item"><span class="meta-label">Categoria</span><strong>${escapar(chamado.nomeCategoria)}</strong></span>
      <span class="meta-item"><span class="meta-label">Solicitante</span><strong>${escapar(chamado.nomeSolicitante)}</strong></span>
      <span class="meta-item"><span class="meta-label">Técnico</span><strong>${chamado.nomeTecnico ? escapar(chamado.nomeTecnico) : "A definir"}</strong></span>
      <span class="meta-item"><span class="meta-label">Aberto em</span><strong>${dataHora(chamado.dataCriacao)}</strong></span>
    </div>

    <section class="detalhe-secao">
      <h4>Descrição</h4>
      <p class="descricao">${escapar(chamado.descricao) || "Sem descrição."}</p>
    </section>

    ${blocoAcoes(chamado)}

    <section class="detalhe-secao">
      <h4>Linha do tempo</h4>
      ${blocoHistorico(historico)}
    </section>

    <section class="detalhe-secao">
      <h4>Conversa</h4>
      ${blocoConversa(comentarios)}
      ${blocoNovaMensagem(chamado)}
    </section>

    <section class="detalhe-secao">
      <h4>Avaliação</h4>
      ${blocoAvaliacao(chamado, avaliacao)}
    </section>
  `;

  ligarEventosDoModal(chamado);
}

function blocoHistorico(historico) {
  if (!historico.length) return '<p class="vazio">Sem eventos registrados.</p>';

  return `<ol class="linha-tempo">
    ${historico
      .map(
        (h) => `
      <li>
        <span class="ponto"></span>
        <div>
          <strong>${h.statusAnterior ? rotuloStatus(h.statusAnterior) + " para " : "Chamado aberto como "}${rotuloStatus(h.statusNovo)}</strong>
          <span>${dataHora(h.dataAlteracao)} · ${escapar(h.alteradoPorNome)}</span>
        </div>
      </li>`
      )
      .join("")}
  </ol>`;
}

function blocoConversa(comentarios) {
  if (!comentarios.length)
    return '<p class="vazio">Ninguém escreveu neste chamado ainda.</p>';

  return `<div class="conversa">
    ${comentarios
      .map(
        (c) => `
      <article class="mensagem ${c.autorPerfil === "SOLICITANTE" ? "de-solicitante" : "de-tecnico"}">
        <header>
          <strong>${escapar(c.autorNome)}</strong>
          <span>${PERFIL[c.autorPerfil] || c.autorPerfil} · ${dataHora(c.dataCriacao)}</span>
        </header>
        <p>${escapar(c.mensagem)}</p>
      </article>`
      )
      .join("")}
  </div>`;
}

function blocoNovaMensagem(chamado) {
  if (["FECHADO", "CANCELADO"].includes(chamado.status)) {
    return '<p class="vazio">Chamado encerrado. A conversa está fechada.</p>';
  }

  return `
    <div class="campo-mensagem">
      <textarea id="novaMensagem" rows="3" placeholder="Escreva para o outro lado do chamado..."></textarea>
      <button class="primary-btn" id="btnComentar">Enviar mensagem</button>
    </div>`;
}

function blocoAcoes(chamado) {
  const acoes = [];

  if (ehTecnico() && chamado.status === "ABERTO") {
    acoes.push('<button class="primary-btn" id="btnAssumir">Assumir chamado</button>');
  }

  if (ehTecnico() && chamado.status === "EM_ANDAMENTO") {
    acoes.push('<button class="primary-btn" id="btnResolver">Marcar como resolvido</button>');
  }

  if (
    ehTecnico() &&
    !["FECHADO", "CANCELADO"].includes(chamado.status)
  ) {
    acoes.push('<button class="secondary-btn" id="btnCancelar">Cancelar chamado</button>');
  }

  if (!acoes.length) return "";

  return `<div class="acoes-chamado">${acoes.join("")}</div>`;
}

function blocoAvaliacao(chamado, avaliacao) {
  if (avaliacao) {
    return `
      <div class="avaliacao-feita">
        <span class="estrelas">${estrelas(avaliacao.nota)}</span>
        <strong>Nota ${avaliacao.nota} de 5</strong>
        <p>${escapar(avaliacao.comentario) || "Sem comentário."}</p>
        <span class="meta-label">${escapar(avaliacao.autorNome)} · ${dataHora(avaliacao.dataAvaliacao)}</span>
      </div>`;
  }

  if (chamado.status !== "RESOLVIDO") {
    return '<p class="vazio">A avaliação abre quando o técnico marcar o chamado como resolvido.</p>';
  }

  const souOSolicitante =
    estado.usuarioAtual &&
    (chamado.solicitanteId
      ? chamado.solicitanteId === estado.usuarioAtual.id
      : chamado.nomeSolicitante === estado.usuarioAtual.nome);

  if (!souOSolicitante) {
    return '<p class="vazio">A avaliação é de quem abriu o chamado.</p>';
  }

  return `
    <div class="avaliar">
      <div class="estrelas-escolha" id="estrelasEscolha">
        ${[1, 2, 3, 4, 5]
          .map(
            (n) =>
              `<button type="button" class="estrela" data-nota="${n}" aria-label="Nota ${n}">☆</button>`
          )
          .join("")}
      </div>
      <textarea id="comentarioAvaliacao" rows="2" placeholder="Conte como foi o atendimento (opcional)"></textarea>
      <button class="primary-btn" id="btnAvaliar" disabled>Enviar avaliação</button>
    </div>`;
}

// ------------------------------------------------- eventos dentro do modal

function ligarEventosDoModal(chamado) {
  const comBotao = async (id, acao, textoOcupado) => {
    const botao = $("#" + id);
    if (!botao) return;

    botao.addEventListener("click", async () => {
      const original = botao.textContent;
      botao.disabled = true;
      botao.textContent = textoOcupado;
      try {
        await acao();
      } catch (erro) {
        mostrarFalha(erro);
        botao.disabled = false;
        botao.textContent = original;
      }
    });
  };

  comBotao(
    "btnAssumir",
    async () => {
      await Api.atribuirTecnico(chamado.id, estado.usuarioAtual.id);
      await recarregarDetalhe(chamado.id);
      avisar("Chamado assumido. Status em atendimento.");
    },
    "Assumindo..."
  );

  comBotao(
    "btnResolver",
    async () => {
      await Api.alterarStatus(chamado.id, {
        statusNovo: "RESOLVIDO",
        usuarioId: estado.usuarioAtual.id,
        comentario: "Atendimento concluído pelo técnico.",
      });
      await recarregarDetalhe(chamado.id);
      avisar("Chamado marcado como resolvido.");
    },
    "Salvando..."
  );

  comBotao(
    "btnCancelar",
    async () => {
      await Api.alterarStatus(chamado.id, {
        statusNovo: "CANCELADO",
        usuarioId: estado.usuarioAtual.id,
      });
      await recarregarDetalhe(chamado.id);
      avisar("Chamado cancelado.");
    },
    "Cancelando..."
  );

  comBotao(
    "btnComentar",
    async () => {
      const campo = $("#novaMensagem");
      const mensagem = campo.value.trim();
      if (!mensagem) throw new Error("Escreva a mensagem antes de enviar.");

      await Api.comentar(chamado.id, {
        usuarioId: estado.usuarioAtual.id,
        mensagem,
      });
      await recarregarDetalhe(chamado.id);
      avisar("Mensagem enviada.");
    },
    "Enviando..."
  );

  const escolha = $("#estrelasEscolha");
  if (escolha) {
    escolha.querySelectorAll(".estrela").forEach((botao) => {
      botao.addEventListener("click", () => {
        estado.notaSelecionada = Number(botao.dataset.nota);
        escolha.querySelectorAll(".estrela").forEach((b) => {
          const marcada = Number(b.dataset.nota) <= estado.notaSelecionada;
          b.textContent = marcada ? "★" : "☆";
          b.classList.toggle("ativa", marcada);
        });
        $("#btnAvaliar").disabled = false;
      });
    });
  }

  comBotao(
    "btnAvaliar",
    async () => {
      await Api.avaliar(chamado.id, {
        usuarioId: estado.usuarioAtual.id,
        nota: estado.notaSelecionada,
        comentario: $("#comentarioAvaliacao").value.trim() || null,
      });
      await recarregarDetalhe(chamado.id);
      avisar("Obrigado pela avaliação. Chamado encerrado.");
    },
    "Enviando..."
  );
}

async function recarregarDetalhe(id) {
  estado.detalheAberto = await Api.detalhes(id);
  estado.notaSelecionada = 0;
  desenharDetalhes();
  await carregarChamados();
}

// --------------------------------------------------------------- eventos

function ligarEventosGerais() {
  $$("[data-page]").forEach((b) =>
    b.addEventListener("click", () => abrirPagina(b.dataset.page))
  );
  $$("[data-go]").forEach((b) =>
    b.addEventListener("click", () => abrirPagina(b.dataset.go))
  );

  $("#menuBtn").addEventListener("click", () =>
    $("#sidebar").classList.toggle("open")
  );

  $("#searchInput").addEventListener("input", desenharLista);
  $("#statusFilter").addEventListener("change", desenharLista);
  $("#ticketForm").addEventListener("submit", enviarChamado);

  $("#usuarioAtual").addEventListener("change", async (evento) => {
    const id = Number(evento.target.value);
    estado.usuarioAtual = estado.usuarios.find((u) => u.id === id);
    desenharIdentidade();
    await atualizarTudo();
  });

  $("#fecharModal").addEventListener("click", fecharDetalhes);
  $("#modalDetalhe").addEventListener("click", (evento) => {
    if (evento.target.id === "modalDetalhe") fecharDetalhes();
  });
  document.addEventListener("keydown", (evento) => {
    if (evento.key === "Escape" && !$("#modalDetalhe").hidden) fecharDetalhes();
  });
}

// ---------------------------------------------------------------- partida

async function iniciar() {
  ligarEventosGerais();

  try {
    await carregarBase();
    await carregarChamados();
    limparFalhaDeConexao();
  } catch (erro) {
    mostrarFalha(erro);
  }
}

iniciar();
