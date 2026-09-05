const API_BASE_URL = "http://localhost:8080/api";
const API_ORIGIN = API_BASE_URL.replace(/\/api$/, "");

const STATUS_LABELS = {
  ABERTO: "Aberto",
  EM_ANDAMENTO: "Em Atendimento",
  RESOLVIDO: "Concluído",
  FECHADO: "Fechado",
  CANCELADO: "Cancelado"
};

const PRIORIDADE_LABELS = {
  BAIXA: "Baixa",
  MEDIA: "Média",
  ALTA: "Alta",
  URGENTE: "Urgente"
};

let tickets = [];
let currentUser = null;

const pageInfo = {
  dashboard: ["Dashboard", "Acompanhe suas solicitações de suporte."],
  novo: ["Novo chamado", "Registre uma nova solicitação de suporte."],
  chamados: ["Meus chamados", "Consulte e acompanhe suas solicitações."],
  historico: ["Histórico", "Veja os atendimentos já finalizados."]
};

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text ?? "";
  return div.innerHTML;
}

function normalize(value) {
  return value
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\s+/g, "-");
}

function mapChamado(chamado) {
  return {
    id: `#CH-${chamado.id}`,
    rawId: chamado.id,
    titulo: chamado.titulo,
    categoria: chamado.nomeCategoria ?? "Sem categoria",
    prioridade: PRIORIDADE_LABELS[chamado.prioridade] ?? chamado.prioridade,
    status: STATUS_LABELS[chamado.status] ?? chamado.status,
    data: new Date(chamado.dataCriacao).toLocaleDateString("pt-BR"),
    descricao: chamado.descricao ?? ""
  };
}

function renderLoading() {
  document.getElementById("recentTickets").innerHTML =
    `<tr><td colspan="6" class="empty">Carregando chamados...</td></tr>`;
  document.getElementById("ticketList").innerHTML = `<div class="empty">Carregando chamados...</div>`;
  document.getElementById("historyList").innerHTML = `<div class="empty">Carregando histórico...</div>`;
  document.getElementById("countAbertos").textContent = "…";
  document.getElementById("countAtendimento").textContent = "…";
  document.getElementById("countConcluidos").textContent = "…";
}

async function fetchTickets() {
  renderLoading();

  const params = new URLSearchParams({ size: "100" });
  if (currentUser) params.set("solicitanteId", currentUser.id);
  const url = `${API_BASE_URL}/chamados?${params.toString()}`;

  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const pagina = await response.json();
    tickets = pagina.content.map(mapChamado).sort((a, b) => b.rawId - a.rawId);
  } catch (error) {
    console.error("Falha ao carregar chamados:", error);
    const message = `<div class="empty">Não foi possível conectar ao servidor. Verifique se o backend está rodando em ${API_BASE_URL}.</div>`;
    document.getElementById("ticketList").innerHTML = message;
    document.getElementById("recentTickets").innerHTML = `<tr><td colspan="6" class="empty">Servidor indisponível.</td></tr>`;
    document.getElementById("historyList").innerHTML = message;
    return;
  }

  renderRecent();
  renderTickets();
  renderHistory();
  updateCounters();
}

async function loadCategorias() {
  const select = document.getElementById("categoria");
  try {
    const response = await fetch(`${API_BASE_URL}/categorias`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const categorias = await response.json();
    categorias.forEach(categoria => {
      const option = document.createElement("option");
      option.value = categoria.id;
      option.textContent = categoria.nome;
      select.appendChild(option);
    });
  } catch (error) {
    console.error("Falha ao carregar categorias:", error);
    showToast("Não foi possível carregar as categorias do servidor.", "error");
  }
}

async function loadCurrentUser() {
  try {
    const response = await fetch(`${API_BASE_URL}/usuarios?perfil=SOLICITANTE`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const usuarios = await response.json();
    currentUser = usuarios[0] ?? null;
  } catch (error) {
    console.error("Falha ao carregar usuário solicitante:", error);
    showToast("Não foi possível identificar o usuário. Verifique se o backend está no ar.", "error");
  }

  const nameEl = document.querySelector(".user-text strong");
  if (nameEl) {
    nameEl.textContent = currentUser ? currentUser.nome : "Usuário indisponível";
  }
}

function showToast(message, type = "success") {
  const toast = document.getElementById("toast");
  toast.textContent = message;
  toast.classList.toggle("error", type === "error");
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 3000);
}

let currentModalTicket = null;

function openModal(ticket) {
  currentModalTicket = ticket;

  document.getElementById("modalTitle").textContent = ticket.titulo;
  document.getElementById("modalId").textContent = ticket.id;
  document.getElementById("modalCategoria").textContent = ticket.categoria;
  document.getElementById("modalPrioridade").innerHTML = priorityBadge(ticket.prioridade);
  document.getElementById("modalStatus").innerHTML = statusBadge(ticket.status);
  document.getElementById("modalData").textContent = ticket.data;
  document.getElementById("modalDescricao").textContent = ticket.descricao || "Sem descrição.";
  document.getElementById("modalOverlay").classList.add("open");

  loadComentarios(ticket.rawId);
  loadAnexos(ticket.rawId);

  const secaoAvaliacao = document.getElementById("modalAvaliacao");
  if (ticket.status === "Concluído") {
    secaoAvaliacao.hidden = false;
    loadAvaliacao(ticket.rawId);
  } else {
    secaoAvaliacao.hidden = true;
  }
}

function closeModal() {
  document.getElementById("modalOverlay").classList.remove("open");
  currentModalTicket = null;
}

async function loadComentarios(chamadoId) {
  const container = document.getElementById("modalComentarios");
  container.innerHTML = `<p class="empty">Carregando comentários...</p>`;
  try {
    const response = await fetch(`${API_BASE_URL}/comentarios?chamadoId=${chamadoId}`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const comentarios = await response.json();
    container.innerHTML = comentarios.length
      ? comentarios.map(c => `
          <div class="comentario-item">
            <strong>${escapeHtml(c.nomeUsuario)}</strong>
            <span class="comentario-data">${new Date(c.dataCriacao).toLocaleString("pt-BR")}</span>
            <p>${escapeHtml(c.mensagem)}</p>
          </div>
        `).join("")
      : `<p class="empty">Nenhum comentário ainda.</p>`;
  } catch (error) {
    console.error("Falha ao carregar comentários:", error);
    container.innerHTML = `<p class="empty">Não foi possível carregar os comentários.</p>`;
  }
}

async function loadAnexos(chamadoId) {
  const container = document.getElementById("modalAnexos");
  container.innerHTML = `<p class="empty">Carregando anexos...</p>`;
  try {
    const response = await fetch(`${API_BASE_URL}/anexos?chamadoId=${chamadoId}`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const anexos = await response.json();
    container.innerHTML = anexos.length
      ? anexos.map(a => `
          <a href="${API_ORIGIN}${a.urlDownload}" target="_blank" rel="noopener">
            📎 ${escapeHtml(a.nomeArquivo)}
          </a>
        `).join("")
      : `<p class="empty">Nenhum anexo enviado.</p>`;
  } catch (error) {
    console.error("Falha ao carregar anexos:", error);
    container.innerHTML = `<p class="empty">Não foi possível carregar os anexos.</p>`;
  }
}

async function loadAvaliacao(chamadoId) {
  const container = document.getElementById("modalAvaliacaoConteudo");
  container.innerHTML = `<p class="empty">Carregando...</p>`;
  try {
    const response = await fetch(`${API_BASE_URL}/avaliacoes/${chamadoId}`);
    if (!response.ok) throw new Error("not-found");
    const avaliacao = await response.json();
    container.innerHTML = `
      <div class="avaliacao-existente">
        <span class="stars">${"★".repeat(avaliacao.nota)}${"☆".repeat(5 - avaliacao.nota)}</span>
        ${avaliacao.comentario ? `<p>${escapeHtml(avaliacao.comentario)}</p>` : ""}
      </div>
    `;
  } catch {
    container.innerHTML = `
      <form id="avaliacaoForm" class="avaliacao-form">
        <div class="stars-input" id="starsInput">
          ${[1, 2, 3, 4, 5].map(n => `<button type="button" class="star-btn" data-nota="${n}">☆</button>`).join("")}
        </div>
        <input type="hidden" id="avaliacaoNota" value="">
        <textarea id="avaliacaoComentario" rows="2" placeholder="Comentário (opcional)"></textarea>
        <button type="submit" class="secondary-btn">Enviar avaliação</button>
      </form>
    `;
    bindAvaliacaoForm(chamadoId);
  }
}

function bindAvaliacaoForm(chamadoId) {
  const starsInput = document.getElementById("starsInput");
  const notaInput = document.getElementById("avaliacaoNota");

  starsInput.querySelectorAll(".star-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const nota = Number(btn.dataset.nota);
      notaInput.value = nota;
      starsInput.querySelectorAll(".star-btn").forEach(b => {
        b.textContent = Number(b.dataset.nota) <= nota ? "★" : "☆";
      });
    });
  });

  document.getElementById("avaliacaoForm").addEventListener("submit", async event => {
    event.preventDefault();
    const nota = Number(notaInput.value);
    if (!nota) {
      showToast("Selecione uma nota de 1 a 5.", "error");
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/avaliacoes`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          chamadoId,
          usuarioId: currentUser.id,
          nota,
          comentario: document.getElementById("avaliacaoComentario").value
        })
      });

      if (!response.ok) {
        const body = await response.json().catch(() => null);
        throw new Error(body?.message || `HTTP ${response.status}`);
      }

      showToast("Avaliação enviada. Obrigado!");
      await loadAvaliacao(chamadoId);
    } catch (error) {
      showToast(error.message || "Não foi possível enviar a avaliação.", "error");
    }
  });
}

function statusBadge(status) {
  return `<span class="badge status-${normalize(status)}">${status}</span>`;
}

function priorityBadge(priority) {
  return `<span class="badge priority-${normalize(priority)}">${priority}</span>`;
}

function showPage(pageId) {
  document.querySelectorAll(".page").forEach(page => page.classList.remove("active"));
  document.querySelectorAll(".nav-item[data-page]").forEach(item => item.classList.remove("active"));

  document.getElementById(pageId).classList.add("active");
  const nav = document.querySelector(`.nav-item[data-page="${pageId}"]`);
  if (nav) nav.classList.add("active");

  document.getElementById("pageTitle").textContent = pageInfo[pageId][0];
  document.getElementById("pageSubtitle").textContent = pageInfo[pageId][1];
  document.getElementById("sidebar").classList.remove("open");
}

function renderRecent() {
  const body = document.getElementById("recentTickets");
  body.innerHTML = tickets.slice(0, 5).map(ticket => `
    <tr>
      <td><strong>${ticket.id}</strong></td>
      <td>${escapeHtml(ticket.titulo)}</td>
      <td>${escapeHtml(ticket.categoria)}</td>
      <td>${priorityBadge(ticket.prioridade)}</td>
      <td>${statusBadge(ticket.status)}</td>
      <td>${ticket.data}</td>
    </tr>
  `).join("");
}

function renderTickets() {
  const query = document.getElementById("searchInput").value.toLowerCase();
  const filter = document.getElementById("statusFilter").value;

  const filtered = tickets.filter(ticket => {
    const matchesText = `${ticket.id} ${ticket.titulo} ${ticket.categoria}`.toLowerCase().includes(query);
    const matchesStatus = filter === "Todos" || ticket.status === filter;
    return matchesText && matchesStatus;
  });

  const list = document.getElementById("ticketList");

  if (!filtered.length) {
    list.innerHTML = `<div class="empty">Nenhum chamado encontrado.</div>`;
    return;
  }

  list.innerHTML = filtered.map(ticket => `
    <article class="ticket-card">
      <div>
        <h4>${escapeHtml(ticket.titulo)}</h4>
        <p>${ticket.id} • ${ticket.data}</p>
      </div>
      <div>
        <span class="meta-label">Categoria</span>
        <strong>${escapeHtml(ticket.categoria)}</strong>
      </div>
      <div>
        <span class="meta-label">Prioridade</span>
        ${priorityBadge(ticket.prioridade)}
      </div>
      <div>
        <span class="meta-label">Status</span>
        ${statusBadge(ticket.status)}
      </div>
      <button class="secondary-btn detalhes-btn" data-id="${ticket.rawId}">Detalhes</button>
    </article>
  `).join("");
}

function renderHistory() {
  const finished = tickets.filter(ticket => ticket.status === "Concluído");
  document.getElementById("historyList").innerHTML = finished.map(ticket => `
    <div class="timeline-item">
      <span class="timeline-dot"></span>
      <h4>${ticket.id} — ${escapeHtml(ticket.titulo)}</h4>
      <p>${ticket.data} • ${escapeHtml(ticket.categoria)} • Atendimento concluído</p>
    </div>
  `).join("");
}

function updateCounters() {
  document.getElementById("countAbertos").textContent =
    tickets.filter(t => t.status === "Aberto").length;
  document.getElementById("countAtendimento").textContent =
    tickets.filter(t => t.status === "Em Atendimento").length;
  document.getElementById("countConcluidos").textContent =
    tickets.filter(t => t.status === "Concluído").length;
}

document.querySelectorAll("[data-page]").forEach(button => {
  button.addEventListener("click", () => showPage(button.dataset.page));
});

document.querySelectorAll("[data-go]").forEach(button => {
  button.addEventListener("click", () => showPage(button.dataset.go));
});

document.getElementById("menuBtn").addEventListener("click", () => {
  document.getElementById("sidebar").classList.toggle("open");
});

document.getElementById("anexoBtn").addEventListener("click", () => {
  document.getElementById("anexo").click();
});

document.getElementById("anexo").addEventListener("change", event => {
  const arquivo = event.target.files[0];
  document.getElementById("anexoNome").textContent = arquivo ? arquivo.name : "Nenhum arquivo escolhido";
});

document.getElementById("ticketForm").addEventListener("reset", () => {
  document.getElementById("anexoNome").textContent = "Nenhum arquivo escolhido";
});

document.getElementById("searchInput").addEventListener("input", renderTickets);
document.getElementById("statusFilter").addEventListener("change", renderTickets);

document.getElementById("ticketList").addEventListener("click", event => {
  const button = event.target.closest(".detalhes-btn");
  if (!button) return;
  const ticket = tickets.find(t => t.rawId === Number(button.dataset.id));
  if (ticket) openModal(ticket);
});

document.getElementById("comentarioForm").addEventListener("submit", async event => {
  event.preventDefault();
  if (!currentModalTicket || !currentUser) return;

  const textarea = document.getElementById("comentarioTexto");
  const mensagem = textarea.value.trim();
  if (!mensagem) return;

  try {
    const response = await fetch(`${API_BASE_URL}/comentarios`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ chamadoId: currentModalTicket.rawId, usuarioId: currentUser.id, mensagem })
    });

    if (!response.ok) {
      const body = await response.json().catch(() => null);
      throw new Error(body?.message || `HTTP ${response.status}`);
    }

    textarea.value = "";
    await loadComentarios(currentModalTicket.rawId);
  } catch (error) {
    showToast(error.message || "Não foi possível enviar o comentário.", "error");
  }
});

document.getElementById("modalClose").addEventListener("click", closeModal);
document.getElementById("modalOverlay").addEventListener("click", event => {
  if (event.target.id === "modalOverlay") closeModal();
});
document.addEventListener("keydown", event => {
  if (event.key === "Escape") closeModal();
});

document.getElementById("ticketForm").addEventListener("submit", async event => {
  event.preventDefault();

  if (!currentUser) {
    showToast("Não foi possível identificar o usuário solicitante. Verifique se o backend está no ar.", "error");
    return;
  }

  const submitBtn = event.target.querySelector('button[type="submit"]');
  submitBtn.disabled = true;

  const local = document.getElementById("local").value.trim();
  const descricao = document.getElementById("descricao").value;

  const payload = {
    titulo: document.getElementById("titulo").value,
    descricao: local ? `Local: ${local}\n\n${descricao}` : descricao,
    categoriaId: Number(document.getElementById("categoria").value),
    solicitanteId: currentUser.id,
    prioridade: document.getElementById("prioridade").value
  };

  try {
    const response = await fetch(`${API_BASE_URL}/chamados`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const body = await response.json().catch(() => null);
      throw new Error(body?.message || `HTTP ${response.status}`);
    }

    const chamadoCriado = await response.json();

    let anexoFalhou = false;
    const arquivo = document.getElementById("anexo").files[0];
    if (arquivo) {
      try {
        const formData = new FormData();
        formData.append("chamadoId", chamadoCriado.id);
        formData.append("enviadoPorId", currentUser.id);
        formData.append("arquivo", arquivo);

        const anexoResponse = await fetch(`${API_BASE_URL}/anexos`, {
          method: "POST",
          body: formData
        });

        if (!anexoResponse.ok) throw new Error(`HTTP ${anexoResponse.status}`);
      } catch {
        anexoFalhou = true;
      }
    }

    event.target.reset();
    await fetchTickets();
    showToast(
      anexoFalhou ? "Chamado aberto, mas não foi possível enviar o anexo." : "Chamado aberto com sucesso!",
      anexoFalhou ? "error" : "success"
    );
    showPage("dashboard");
  } catch (error) {
    console.error("Falha ao criar chamado:", error);
    showToast(error.message || "Não foi possível abrir o chamado. Tente novamente.", "error");
  } finally {
    submitBtn.disabled = false;
  }
});

async function init() {
  await Promise.all([loadCategorias(), loadCurrentUser()]);
  await fetchTickets();
}

init();
