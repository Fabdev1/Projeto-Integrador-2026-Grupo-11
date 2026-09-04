const tickets = [
  {
    id: "#CH-1026",
    titulo: "Impressora do financeiro não imprime",
    categoria: "Hardware",
    prioridade: "Alta",
    status: "Em Atendimento",
    data: "02/09/2026",
    descricao: "A impressora aparece offline para todos os computadores do setor."
  },
  {
    id: "#CH-1025",
    titulo: "Sem acesso ao sistema de CRM",
    categoria: "Acesso / Senha",
    prioridade: "Média",
    status: "Aberto",
    data: "01/09/2026",
    descricao: "Usuário não consegue autenticar no sistema."
  },
  {
    id: "#CH-1022",
    titulo: "Internet instável na sala 204",
    categoria: "Rede / Internet",
    prioridade: "Alta",
    status: "Aberto",
    data: "31/08/2026",
    descricao: "Conexão apresenta quedas durante reuniões."
  },
  {
    id: "#CH-1018",
    titulo: "Instalação de pacote Office",
    categoria: "Software",
    prioridade: "Baixa",
    status: "Concluído",
    data: "28/08/2026",
    descricao: "Instalação concluída com sucesso."
  },
  {
    id: "#CH-1013",
    titulo: "Erro ao iniciar sistema interno",
    categoria: "Sistemas",
    prioridade: "Média",
    status: "Concluído",
    data: "25/08/2026",
    descricao: "Aplicação atualizada e acesso normalizado."
  },
  {
    id: "#CH-1009",
    titulo: "Troca de teclado",
    categoria: "Hardware",
    prioridade: "Baixa",
    status: "Concluído",
    data: "21/08/2026",
    descricao: "Periférico substituído."
  }
];

const pageInfo = {
  dashboard: ["Dashboard", "Acompanhe suas solicitações de suporte."],
  novo: ["Novo chamado", "Registre uma nova solicitação de suporte."],
  chamados: ["Meus chamados", "Consulte e acompanhe suas solicitações."],
  historico: ["Histórico", "Veja os atendimentos já finalizados."]
};

function normalize(value) {
  return value
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\s+/g, "-");
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

function statusBadge(status) {
  return `<span class="badge status-${normalize(status)}">${status}</span>`;
}

function priorityBadge(priority) {
  return `<span class="badge priority-${normalize(priority)}">${priority}</span>`;
}

function renderRecent() {
  const body = document.getElementById("recentTickets");
  body.innerHTML = tickets.slice(0, 5).map(ticket => `
    <tr>
      <td><strong>${ticket.id}</strong></td>
      <td>${ticket.titulo}</td>
      <td>${ticket.categoria}</td>
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
        <h4>${ticket.titulo}</h4>
        <p>${ticket.id} • ${ticket.data}</p>
      </div>
      <div>
        <span class="meta-label">Categoria</span>
        <strong>${ticket.categoria}</strong>
      </div>
      <div>
        <span class="meta-label">Prioridade</span>
        ${priorityBadge(ticket.prioridade)}
      </div>
      <div>
        <span class="meta-label">Status</span>
        ${statusBadge(ticket.status)}
      </div>
      <button class="secondary-btn" onclick="alert('${ticket.id}: ${ticket.descricao.replace(/'/g, "\\'")}')">Detalhes</button>
    </article>
  `).join("");
}

function renderHistory() {
  const finished = tickets.filter(ticket => ticket.status === "Concluído");
  document.getElementById("historyList").innerHTML = finished.map(ticket => `
    <div class="timeline-item">
      <span class="timeline-dot"></span>
      <h4>${ticket.id} — ${ticket.titulo}</h4>
      <p>${ticket.data} • ${ticket.categoria} • Atendimento concluído</p>
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

document.getElementById("searchInput").addEventListener("input", renderTickets);
document.getElementById("statusFilter").addEventListener("change", renderTickets);

document.getElementById("ticketForm").addEventListener("submit", event => {
  event.preventDefault();

  const newId = `#CH-${1030 + tickets.length}`;
  const today = new Date().toLocaleDateString("pt-BR");

  tickets.unshift({
    id: newId,
    titulo: document.getElementById("titulo").value,
    categoria: document.getElementById("categoria").value,
    prioridade: document.getElementById("prioridade").value,
    status: "Aberto",
    data: today,
    descricao: document.getElementById("descricao").value
  });

  event.target.reset();
  renderRecent();
  renderTickets();
  renderHistory();
  updateCounters();

  const toast = document.getElementById("toast");
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 2500);

  showPage("dashboard");
});

renderRecent();
renderTickets();
renderHistory();
updateCounters();
