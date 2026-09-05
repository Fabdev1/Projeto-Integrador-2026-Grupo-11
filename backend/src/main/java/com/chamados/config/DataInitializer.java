package com.chamados.config;

import com.chamados.domain.entity.*;
import com.chamados.domain.enums.PerfilUsuario;
import com.chamados.domain.enums.PrioridadeChamado;
import com.chamados.domain.enums.StatusChamado;
import com.chamados.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Carga automatica dos dados iniciais quando o banco estiver vazio.
 * Permite rodar e testar imediatamente no IntelliJ usando H2 em memoria,
 * sem precisar de PostgreSQL ou Docker instalado no notebook.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final HistoricoStatusRepository historicoStatusRepository;
    private final ComentarioRepository comentarioRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotificacaoRepository notificacaoRepository;

    public DataInitializer(CategoriaRepository categoriaRepository,
                           UsuarioRepository usuarioRepository,
                           ChamadoRepository chamadoRepository,
                           HistoricoStatusRepository historicoStatusRepository,
                           ComentarioRepository comentarioRepository,
                           AvaliacaoRepository avaliacaoRepository,
                           NotificacaoRepository notificacaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.chamadoRepository = chamadoRepository;
        this.historicoStatusRepository = historicoStatusRepository;
        this.comentarioRepository = comentarioRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // Ja possui dados (PostgreSQL com seed rodado), nao faz nada.
        }

        // 1. Categorias padrao
        Categoria catHardware = categoriaRepository.save(new Categoria(null, "Hardware"));
        Categoria catSoftware = categoriaRepository.save(new Categoria(null, "Software / Sistemas"));
        Categoria catRedes = categoriaRepository.save(new Categoria(null, "Redes / Conectividade"));
        Categoria catAcessos = categoriaRepository.save(new Categoria(null, "Acessos e Permissões"));

        // 2. Usuarios padrao
        Usuario joao = usuarioRepository.save(Usuario.builder()
                .nome("João Solicitante")
                .email("joao.solicitante@empresa.com")
                .perfil(PerfilUsuario.SOLICITANTE)
                .build());

        Usuario maria = usuarioRepository.save(Usuario.builder()
                .nome("Maria Técnica")
                .email("maria.tecnica@empresa.com")
                .perfil(PerfilUsuario.TECNICO)
                .build());

        Usuario carlos = usuarioRepository.save(Usuario.builder()
                .nome("Carlos Administrador")
                .email("carlos.admin@empresa.com")
                .perfil(PerfilUsuario.ADMIN)
                .build());

        Usuario ana = usuarioRepository.save(Usuario.builder()
                .nome("Ana Ribeiro")
                .email("ana.ribeiro@empresa.com")
                .perfil(PerfilUsuario.SOLICITANTE)
                .build());

        Usuario bruno = usuarioRepository.save(Usuario.builder()
                .nome("Bruno Teixeira")
                .email("bruno.teixeira@empresa.com")
                .perfil(PerfilUsuario.SOLICITANTE)
                .build());

        Usuario rafael = usuarioRepository.save(Usuario.builder()
                .nome("Rafael Nunes")
                .email("rafael.nunes@empresa.com")
                .perfil(PerfilUsuario.TECNICO)
                .build());

        // 3. Chamados de demonstracao
        LocalDateTime agora = LocalDateTime.now();

        // Chamado 1: EM_ANDAMENTO
        Chamado c1 = chamadoRepository.save(Chamado.builder()
                .categoria(catHardware)
                .solicitante(joao)
                .tecnico(maria)
                .status(StatusChamado.EM_ANDAMENTO)
                .prioridade(PrioridadeChamado.ALTA)
                .titulo("Impressora do financeiro nao imprime")
                .descricao("A impressora aparece offline para todos os computadores do setor desde a manha de segunda. Ja reiniciamos o equipamento e o cabo de rede esta conectado.")
                .dataCriacao(agora.minusDays(2))
                .dataAtualizacao(agora.minusDays(2).plusHours(2))
                .build());

        historicoStatusRepository.save(new HistoricoStatus(null, c1, joao, null, StatusChamado.ABERTO, agora.minusDays(2)));
        historicoStatusRepository.save(new HistoricoStatus(null, c1, maria, StatusChamado.ABERTO, StatusChamado.EM_ANDAMENTO, agora.minusDays(2).plusHours(2)));

        comentarioRepository.save(new Comentario(null, c1, maria,
                "Verifiquei remotamente e a fila de impressao esta travada. Vou ate o setor hoje a tarde para trocar o cabo de rede.",
                agora.minusDays(2).plusHours(3)));
        comentarioRepository.save(new Comentario(null, c1, joao,
                "Combinado. O setor fica aberto ate as 18h.",
                agora.minusDays(2).plusHours(4)));

        notificacaoRepository.save(new Notificacao(null, joao, c1,
                "Seu chamado #" + c1.getId() + " foi assumido por Maria Técnica.", false, agora.minusDays(2).plusHours(2)));

        // Chamado 2: ABERTO
        Chamado c2 = chamadoRepository.save(Chamado.builder()
                .categoria(catAcessos)
                .solicitante(ana)
                .status(StatusChamado.ABERTO)
                .prioridade(PrioridadeChamado.MEDIA)
                .titulo("Sem acesso ao sistema de CRM")
                .descricao("A tela de login devolve \"usuario ou senha invalidos\" mesmo apos a redefinicao de senha.")
                .dataCriacao(agora.minusDays(1))
                .dataAtualizacao(agora.minusDays(1))
                .build());
        historicoStatusRepository.save(new HistoricoStatus(null, c2, ana, null, StatusChamado.ABERTO, agora.minusDays(1)));

        // Chamado 3: ABERTO
        Chamado c3 = chamadoRepository.save(Chamado.builder()
                .categoria(catRedes)
                .solicitante(bruno)
                .status(StatusChamado.ABERTO)
                .prioridade(PrioridadeChamado.ALTA)
                .titulo("Internet instavel na sala 204")
                .descricao("A conexao cai por cerca de um minuto, varias vezes ao dia, sempre durante as reunioes.")
                .dataCriacao(agora.minusHours(6))
                .dataAtualizacao(agora.minusHours(6))
                .build());
        historicoStatusRepository.save(new HistoricoStatus(null, c3, bruno, null, StatusChamado.ABERTO, agora.minusHours(6)));

        // Chamado 4: RESOLVIDO
        Chamado c4 = chamadoRepository.save(Chamado.builder()
                .categoria(catSoftware)
                .solicitante(joao)
                .tecnico(rafael)
                .status(StatusChamado.RESOLVIDO)
                .prioridade(PrioridadeChamado.BAIXA)
                .titulo("Instalacao do pacote Office na maquina nova")
                .descricao("Maquina recebida pelo setor de compras, sem o pacote Office instalado.")
                .dataCriacao(agora.minusDays(5))
                .dataAtualizacao(agora.minusDays(4))
                .build());
        historicoStatusRepository.save(new HistoricoStatus(null, c4, joao, null, StatusChamado.ABERTO, agora.minusDays(5)));
        historicoStatusRepository.save(new HistoricoStatus(null, c4, rafael, StatusChamado.ABERTO, StatusChamado.EM_ANDAMENTO, agora.minusDays(5).plusHours(2)));
        historicoStatusRepository.save(new HistoricoStatus(null, c4, rafael, StatusChamado.EM_ANDAMENTO, StatusChamado.RESOLVIDO, agora.minusDays(4)));

        // Chamado 5: FECHADO (com avaliacao)
        Chamado c5 = chamadoRepository.save(Chamado.builder()
                .categoria(catSoftware)
                .solicitante(ana)
                .tecnico(maria)
                .status(StatusChamado.FECHADO)
                .prioridade(PrioridadeChamado.MEDIA)
                .titulo("Erro ao iniciar o sistema interno")
                .descricao("A aplicacao fecha sozinha na tela de carregamento, sem mensagem de erro.")
                .dataCriacao(agora.minusDays(8))
                .dataAtualizacao(agora.minusDays(6))
                .build());
        historicoStatusRepository.save(new HistoricoStatus(null, c5, ana, null, StatusChamado.ABERTO, agora.minusDays(8)));
        historicoStatusRepository.save(new HistoricoStatus(null, c5, maria, StatusChamado.ABERTO, StatusChamado.EM_ANDAMENTO, agora.minusDays(8).plusHours(2)));
        historicoStatusRepository.save(new HistoricoStatus(null, c5, maria, StatusChamado.EM_ANDAMENTO, StatusChamado.RESOLVIDO, agora.minusDays(7)));
        historicoStatusRepository.save(new HistoricoStatus(null, c5, ana, StatusChamado.RESOLVIDO, StatusChamado.FECHADO, agora.minusDays(6)));

        avaliacaoRepository.save(new Avaliacao(null, c5, ana, 5,
                "Resolvido no mesmo dia e com explicacao do que tinha acontecido.",
                agora.minusDays(6)));
    }
}
