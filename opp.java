// ================ INTERFACES ================

// Interface Reservavel - define contrato para itens que podem ser reservados
interface Reservavel {
    boolean podeSerReservado();
    void reservar(int usuarioId);
    void liberar();
    String getDetalhesReserva();
}

// Interface Persistivel - define contrato para persistência
interface Persistivel {
    String toJson();
    void fromJson(String json);
}

// Interface Notificavel - define contrato para notificações
interface Notificavel {
    void receberNotificacao(String mensagem);
    boolean aceitaNotificacoes();
}

// ================ CLASSES ABSTRATAS ================

// Classe abstrata Usuario - base para diferentes tipos de usuários
abstract class Usuario implements Notificavel, Persistivel {
    protected static int proximoId = 1; // Atributo de classe
    protected static final int MAX_RESERVAS = 3; // Constante final
    
    protected int id;
    protected String nome;
    protected String email;
    protected String senhaCriptografada;
    protected boolean ativo;
    protected boolean receberNotificacoes;
    
    // Construtor sobrecarregado - básico
    public Usuario(String nome) {
        this.id = proximoId++;
        this.nome = nome;
        this.ativo = true;
        this.receberNotificacoes = true;
    }
    
    // Construtor sobrecarregado - completo
    public Usuario(String nome, String email, String senha) {
        this(nome);
        this.email = email;
        this.senhaCriptografada = criptografarSenha(senha);
    }
    
    // Método estático de classe
    public static String criptografarSenha(String senha) {
        return "hash_" + senha.hashCode();
    }
    
    // Métodos abstratos - devem ser implementados pelas subclasses
    public abstract boolean podeReservar();
    public abstract void processarLogin();
    public abstract String getTipo();
    
    // Método concreto que pode ser sobrescrito
    public boolean autenticar(String senhaFornecida) {
        return this.senhaCriptografada.equals(criptografarSenha(senhaFornecida));
    }
    
    // Encapsulamento - getters e setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public boolean isAtivo() { return ativo; }
    
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    
    // Implementação da interface Notificavel
    @Override
    public void receberNotificacao(String mensagem) {
        if (receberNotificacoes) {
            System.out.println("📧 Notificação para " + nome + ": " + mensagem);
        }
    }
    
    @Override
    public boolean aceitaNotificacoes() {
        return receberNotificacoes;
    }
    
    // Implementação básica da interface Persistivel
    @Override
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"nome\":\"%s\",\"email\":\"%s\",\"tipo\":\"%s\",\"ativo\":%b}",
            id, nome, email != null ? email : "", getTipo(), ativo
        );
    }
    
    @Override
    public String toString() {
        return String.format("%s[%d]: %s (%s)", getTipo(), id, nome, 
                           ativo ? "Ativo" : "Inativo");
    }
}

// Classe abstrata Livro - base para diferentes tipos de livros
abstract class Livro implements Reservavel, Persistivel {
    protected static int proximoId = 1;
    
    protected int id;
    protected String titulo;
    protected String autor;
    protected String isbn;
    protected String categoria;
    protected boolean disponivel;
    protected int usuarioReservaId;
    protected String dataReserva;
    
    // Construtor sobrecarregado - básico
    public Livro(String titulo, String autor) {
        this.id = proximoId++;
        this.titulo = titulo;
        this.autor = autor;
        this.disponivel = true;
        this.usuarioReservaId = -1;
    }
    
    // Construtor sobrecarregado - completo
    public Livro(String titulo, String autor, String isbn, String categoria) {
        this(titulo, autor);
        this.isbn = isbn;
        this.categoria = categoria;
    }
    
    // Método estático para validar ISBN
    public static boolean validarISBN(String isbn) {
        return isbn != null && isbn.matches("\\d{3}-\\d{2}-\\d{4}-\\d{3}-\\d");
    }
    
    // Métodos abstratos
    public abstract String getTipoMidia();
    public abstract boolean calcularDisponibilidade();
    
    // Implementação da interface Reservavel
    @Override
    public boolean podeSerReservado() {
        return disponivel && calcularDisponibilidade();
    }
    
    @Override
    public void reservar(int usuarioId) {
        if (podeSerReservado()) {
            this.disponivel = false;
            this.usuarioReservaId = usuarioId;
            this.dataReserva = java.time.LocalDate.now().toString();
        }
    }
    
    @Override
    public void liberar() {
        this.disponivel = true;
        this.usuarioReservaId = -1;
        this.dataReserva = null;
    }
    
    @Override
    public String getDetalhesReserva() {
        if (!disponivel) {
            return String.format("Reservado por usuário %d em %s", 
                               usuarioReservaId, dataReserva);
        }
        return "Disponível";
    }
    
    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getIsbn() { return isbn; }
    public String getCategoria() { return categoria; }
    public boolean isDisponivel() { return disponivel; }
    
    @Override
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"titulo\":\"%s\",\"autor\":\"%s\",\"isbn\":\"%s\"," +
            "\"categoria\":\"%s\",\"disponivel\":%b,\"tipo\":\"%s\"}",
            id, titulo, autor, isbn != null ? isbn : "", 
            categoria != null ? categoria : "", disponivel, getTipoMidia()
        );
    }
    
    @Override
    public String toString() {
        return String.format("%s[%d]: %s - %s (%s) - %s", 
                           getTipoMidia(), id, titulo, autor, 
                           categoria != null ? categoria : "Sem categoria",
                           disponivel ? "Disponível" : "Reservado");
    }
}

// Classe abstrata Relatorio - template para relatórios
abstract class Relatorio {
    protected String titulo;
    protected String dataGeracao;
    
    public Relatorio(String titulo) {
        this.titulo = titulo;
        this.dataGeracao = java.time.LocalDateTime.now().toString();
    }
    
    // Template Method Pattern
    public final String gerar() {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append(gerarCabecalho());
        relatorio.append("\n");
        relatorio.append(gerarConteudo());
        relatorio.append("\n");
        relatorio.append(gerarRodape());
        return relatorio.toString();
    }
    
    private String gerarCabecalho() {
        return String.format("=== %s ===\nGerado em: %s\n", titulo, dataGeracao);
    }
    
    private String gerarRodape() {
        return "\n=== Fim do Relatório ===";
    }
    
    // Método abstrato - implementado pelas subclasses
    protected abstract String gerarConteudo();
}

// ================ CLASSES CONCRETAS ================

// Herança - UsuarioComum herda de Usuario
class UsuarioComum extends Usuario {
    private int quantidadeReservas;
    
    public UsuarioComum(String nome) {
        super(nome);
        this.quantidadeReservas = 0;
    }
    
    public UsuarioComum(String nome, String email, String senha) {
        super(nome, email, senha);
        this.quantidadeReservas = 0;
    }
    
    // Sobrescrita de métodos abstratos
    @Override
    public boolean podeReservar() {
        return quantidadeReservas < MAX_RESERVAS && ativo;
    }
    
    @Override
    public void processarLogin() {
        System.out.println("🔑 Login de usuário comum: " + nome);
    }
    
    @Override
    public String getTipo() {
        return "UsuarioComum";
    }
    
    public void incrementarReservas() { quantidadeReservas++; }
    public void decrementarReservas() { 
        if (quantidadeReservas > 0) quantidadeReservas--; 
    }
    public int getQuantidadeReservas() { return quantidadeReservas; }
}

// Herança - Administrador herda de Usuario
class Administrador extends Usuario {
    private String[] permissoes;
    
    public Administrador(String nome) {
        super(nome);
        this.permissoes = new String[]{"READ", "WRITE", "DELETE", "ADMIN"};
    }
    
    public Administrador(String nome, String email, String senha) {
        super(nome, email, senha);
        this.permissoes = new String[]{"READ", "WRITE", "DELETE", "ADMIN"};
    }
    
    // Sobrescrita - comportamento específico para admin
    @Override
    public boolean podeReservar() {
        return true; // Admin pode sempre reservar
    }
    
    @Override
    public void processarLogin() {
        System.out.println("👑 Login de administrador: " + nome + " (Privilégios elevados)");
    }
    
    @Override
    public String getTipo() {
        return "Administrador";
    }
    
    // Sobrescrita - admins têm autenticação adicional
    @Override
    public boolean autenticar(String senhaFornecida) {
        boolean autenticado = super.autenticar(senhaFornecida);
        if (autenticado) {
            System.out.println("🔐 Acesso administrativo concedido para: " + nome);
        }
        return autenticado;
    }
    
    public boolean temPermissao(String permissao) {
        for (String p : permissoes) {
            if (p.equals(permissao)) return true;
        }
        return false;
    }
}

// Herança - LivroFisico herda de Livro
class LivroFisico extends Livro {
    private String localizacao;
    private String estado; // novo, usado, danificado
    
    public LivroFisico(String titulo, String autor) {
        super(titulo, autor);
        this.estado = "novo";
    }
    
    public LivroFisico(String titulo, String autor, String isbn, String categoria, String localizacao) {
        super(titulo, autor, isbn, categoria);
        this.localizacao = localizacao;
        this.estado = "novo";
    }
    
    @Override
    public String getTipoMidia() {
        return "LivroFisico";
    }
    
    @Override
    public boolean calcularDisponibilidade() {
        return !estado.equals("danificado");
    }
    
    public String getLocalizacao() { return localizacao; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

// Herança - LivroDigital herda de Livro
class LivroDigital extends Livro {
    private String caminhoArquivo;
    private long tamanhoMB;
    private int licencasDisponiveis;
    private int licencasEmUso;
    
    public LivroDigital(String titulo, String autor) {
        super(titulo, autor);
        this.licencasDisponiveis = 1;
        this.licencasEmUso = 0;
    }
    
    public LivroDigital(String titulo, String autor, String isbn, String categoria, 
                       String caminhoArquivo, int licencas) {
        super(titulo, autor, isbn, categoria);
        this.caminhoArquivo = caminhoArquivo;
        this.licencasDisponiveis = licencas;
        this.licencasEmUso = 0;
    }
    
    @Override
    public String getTipoMidia() {
        return "LivroDigital";
    }
    
    // Sobrescrita - considera licenças digitais
    @Override
    public boolean calcularDisponibilidade() {
        return licencasEmUso < licencasDisponiveis;
    }
    
    @Override
    public void reservar(int usuarioId) {
        if (calcularDisponibilidade()) {
            licencasEmUso++;
            this.usuarioReservaId = usuarioId;
            this.dataReserva = java.time.LocalDate.now().toString();
            // Livros digitais podem ter múltiplas "reservas" simultâneas
            if (licencasEmUso >= licencasDisponiveis) {
                this.disponivel = false;
            }
        }
    }
    
    @Override
    public void liberar() {
        if (licencasEmUso > 0) {
            licencasEmUso--;
            if (licencasEmUso < licencasDisponiveis) {
                this.disponivel = true;
            }
        }
    }
    
    public String getCaminhoArquivo() { return caminhoArquivo; }
    public int getLicencasDisponiveis() { return licencasDisponiveis; }
    public int getLicencasEmUso() { return licencasEmUso; }
}

// Classe de reserva
class Reserva implements Persistivel {
    private static int proximoId = 1;
    
    private int id;
    private int usuarioId;
    private int livroId;
    private String dataReserva;
    private String dataVencimento;
    private String status;
    
    public Reserva(int usuarioId, int livroId) {
        this.id = proximoId++;
        this.usuarioId = usuarioId;
        this.livroId = livroId;
        this.dataReserva = java.time.LocalDate.now().toString();
        this.dataVencimento = java.time.LocalDate.now().plusDays(14).toString();
        this.status = "ativa";
    }
    
    public int getId() { return id; }
    public int getUsuarioId() { return usuarioId; }
    public int getLivroId() { return livroId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"usuarioId\":%d,\"livroId\":%d,\"dataReserva\":\"%s\"," +
            "\"dataVencimento\":\"%s\",\"status\":\"%s\"}",
            id, usuarioId, livroId, dataReserva, dataVencimento, status
        );
    }
    
    @Override
    public void fromJson(String json) {
        // Implementação simplificada
    }
    
    @Override
    public String toString() {
        return String.format("Reserva[%d]: Usuário %d - Livro %d (%s)", 
                           id, usuarioId, livroId, status);
    }
}

// Relatório de uso - herda de Relatorio
class RelatorioUso extends Relatorio {
    private java.util.List<Reserva> reservas;
    
    public RelatorioUso(java.util.List<Reserva> reservas) {
        super("Relatório de Uso da Biblioteca");
        this.reservas = reservas;
    }
    
    @Override
    protected String gerarConteudo() {
        StringBuilder conteudo = new StringBuilder();
        conteudo.append("Total de reservas: ").append(reservas.size()).append("\n");
        
        long reservasAtivas = reservas.stream()
            .filter(r -> "ativa".equals(r.getStatus()))
            .count();
        
        conteudo.append("Reservas ativas: ").append(reservasAtivas).append("\n");
        conteudo.append("Reservas finalizadas: ").append(reservas.size() - reservasAtivas);
        
        return conteudo.toString();
    }
}

// ================ CLASSES GENÉRICAS ================

// Repositório genérico para qualquer tipo de objeto
class Repositorio<T> {
    private java.util.List<T> itens;
    
    public Repositorio() {
        this.itens = new java.util.ArrayList<>();
    }
    
    public void adicionar(T item) {
        itens.add(item);
    }
    
    public void remover(T item) {
        itens.remove(item);
    }
    
    public java.util.List<T> obterTodos() {
        return new java.util.ArrayList<>(itens);
    }
    
    public int tamanho() {
        return itens.size();
    }
    
    // Método genérico para busca com critério
    public java.util.List<T> buscar(java.util.function.Predicate<T> criterio) {
        return itens.stream()
                   .filter(criterio)
                   .collect(java.util.stream.Collectors.toList());
    }
}

// Cache genérico
class Cache<K, V> {
    private java.util.Map<K, V> cache;
    private final int tamanhoMaximo;
    
    public Cache(int tamanhoMaximo) {
        this.cache = new java.util.LinkedHashMap<>();
        this.tamanhoMaximo = tamanhoMaximo;
    }
    
    public V obter(K chave) {
        return cache.get(chave);
    }
    
    public void armazenar(K chave, V valor) {
        if (cache.size() >= tamanhoMaximo) {
            // Remove o item mais antigo
            K primeiraChave = cache.keySet().iterator().next();
            cache.remove(primeiraChave);
        }
        cache.put(chave, valor);
    }
    
    public boolean contem(K chave) {
        return cache.containsKey(chave);
    }
    
    public void limpar() {
        cache.clear();
    }
}

// ================ CLASSE PRINCIPAL - SISTEMA ================

class SistemaBiblioteca {
    private Repositorio<Usuario> repositorioUsuarios;
    private Repositorio<Livro> repositorioLivros;
    private Repositorio<Reserva> repositorioReservas;
    private Cache<String, java.util.List<Livro>> cacheConsultas;
    
    // Constantes final
    private static final String VERSAO = "1.0";
    private static final int CACHE_SIZE = 50;
    
    public SistemaBiblioteca() {
        this.repositorioUsuarios = new Repositorio<>();
        this.repositorioLivros = new Repositorio<>();
        this.repositorioReservas = new Repositorio<>();
        this.cacheConsultas = new Cache<>(CACHE_SIZE);
        
        inicializarDadosDemo();
    }
    
    private void inicializarDadosDemo() {
        // Criando usuários - demonstra polimorfismo e herança
        Usuario user1 = new UsuarioComum("João Silva", "joao@email.com", "123456");
        Usuario user2 = new UsuarioComum("Maria Santos");
        Usuario admin = new Administrador("Ana Admin", "ana@admin.com", "admin123");
        
        repositorioUsuarios.adicionar(user1);
        repositorioUsuarios.adicionar(user2);
        repositorioUsuarios.adicionar(admin);
        
        // Criando livros - demonstra polimorfismo e herança
        Livro livro1 = new LivroFisico("Java: Como Programar", "Paul Deitel", 
                                      "978-85-7522-123-4", "Programação", "Estante A1");
        Livro livro2 = new LivroDigital("Clean Code", "Robert Martin", 
                                       "978-85-7522-456-7", "Programação", 
                                       "/livros/clean_code.pdf", 3);
        Livro livro3 = new LivroFisico("Design Patterns", "Gang of Four");
        
        repositorioLivros.adicionar(livro1);
        repositorioLivros.adicionar(livro2);
        repositorioLivros.adicionar(livro3);
    }
    
    // Método sobrecarregado para busca
    public java.util.List<Livro> buscarLivro(String titulo) {
        return buscarLivro(titulo, null);
    }
    
    public java.util.List<Livro> buscarLivro(String titulo, String autor) {
        String chaveCache = titulo + (autor != null ? "_" + autor : "");
        
        // Verifica cache primeiro
        if (cacheConsultas.contem(chaveCache)) {
            System.out.println("📋 Resultado obtido do cache");
            return cacheConsultas.obter(chaveCache);
        }
        
        // Busca nos repositórios
        java.util.List<Livro> resultado = repositorioLivros.buscar(livro -> {
            boolean tituloMatch = livro.getTitulo().toLowerCase()
                                      .contains(titulo.toLowerCase());
            boolean autorMatch = autor == null || livro.getAutor().toLowerCase()
                                                      .contains(autor.toLowerCase());
            return tituloMatch && autorMatch;
        });
        
        // Armazena no cache
        cacheConsultas.armazenar(chaveCache, resultado);
        
        return resultado;
    }
    
    public boolean realizarReserva(int usuarioId, int livroId) {
        // Busca usuário e livro
        Usuario usuario = repositorioUsuarios.buscar(u -> u.getId() == usuarioId)
                                           .stream().findFirst().orElse(null);
        Livro livro = repositorioLivros.buscar(l -> l.getId() == livroId)
                                     .stream().findFirst().orElse(null);
        
        if (usuario == null || livro == null) {
            System.out.println("❌ Usuário ou livro não encontrado");
            return false;
        }
        
        if (!usuario.podeReservar()) {
            System.out.println("❌ Usuário não pode fazer mais reservas");
            return false;
        }
        
        if (!livro.podeSerReservado()) {
            System.out.println("❌ Livro não está disponível para reserva");
            return false;
        }
        
        // Realiza a reserva
        livro.reservar(usuarioId);
        Reserva reserva = new Reserva(usuarioId, livroId);
        repositorioReservas.adicionar(reserva);
        
        // Incrementa contador para usuários comuns
        if (usuario instanceof UsuarioComum) {
            ((UsuarioComum) usuario).incrementarReservas();
        }
        
        // Notifica o usuário
        usuario.receberNotificacao("Reserva realizada: " + livro.getTitulo());
        
        System.out.println("✅ Reserva realizada com sucesso!");
        return true;
    }
    
    public void listarUsuarios() {
        System.out.println("\n👥 === USUÁRIOS DO SISTEMA ===");
        repositorioUsuarios.obterTodos().forEach(System.out::println);
    }
    
    public void listarLivros() {
        System.out.println("\n📚 === CATÁLOGO DE LIVROS ===");
        repositorioLivros.obterTodos().forEach(livro -> {
            System.out.println(livro);
            if (!livro.isDisponivel()) {
                System.out.println("   📍 " + livro.getDetalhesReserva());
            }
        });
    }
    
    public void listarReservas() {
        System.out.println("\n📋 === RESERVAS ATIVAS ===");
        repositorioReservas.obterTodos().forEach(System.out::println);
    }
    
    public void gerarRelatorioUso() {
        RelatorioUso relatorio = new RelatorioUso(repositorioReservas.obterTodos());
        System.out.println("\n" + relatorio.gerar());
    }
    
    public void demonstrarPolimorfismo() {
        System.out.println("\n🔄 === DEMONSTRAÇÃO DE POLIMORFISMO ===");
        
        // Array com diferentes tipos de usuários
        java.util.List<Usuario> usuarios = repositorioUsuarios.obterTodos();
        
        System.out.println("Processando login de diferentes tipos de usuário:");
        for (Usuario usuario : usuarios) {
            usuario.processarLogin(); // Comportamento polimórfico
        }
        
        System.out.println("\nTestando autenticação:");
        for (Usuario usuario : usuarios) {
            boolean autenticado = false;
            if (usuario instanceof UsuarioComum) {
                autenticado = usuario.autenticar("123456");
            } else if (usuario instanceof Administrador) {
                autenticado = usuario.autenticar("admin123");
            }
            
            System.out.println(usuario.getNome() + ": " + 
                             (autenticado ? "✅ Autenticado" : "❌ Falhou"));
        }
    }
    
    public void demonstrarGenerics() {
        System.out.println("\n🧬 === DEMONSTRAÇÃO DE GENERICS ===");
        
        // Cache genérico em ação
        Cache<String, String> cacheInfo = new Cache<>(3);
        cacheInfo.armazenar("sistema", "Biblioteca Digital v" + VERSAO);
        cacheInfo.armazenar("usuarios", String.valueOf(repositorioUsuarios.tamanho()));
        cacheInfo.armazenar("livros", String.valueOf(repositorioLivros.tamanho()));
        
        System.out.println("Cache de informações:");
        System.out.println("Sistema: " + cacheInfo.obter("sistema"));
        System.out.println("Total de usuários: " + cacheInfo.obter("usuarios"));
        System.out.println("Total de livros: " + cacheInfo.obter("livros"));
        
        // Repositório genérico em ação
        System.out.println("\nRepositórios genéricos:");
        System.out.println("Usuários no repositório: " + repositorioUsuarios.tamanho());
        System.out.println("Livros no repositório: " + repositorioLivros.tamanho());
        System.out.println("Reservas no repositório: " + repositorioReservas.tamanho());
    }
    
    // Método final que não pode ser sobrescrito
    public final void logOperacao(String operacao) {
        System.out.println("🔍 LOG: " + java.time.LocalDateTime.now() + " - " + operacao);
    }
}

// ================ CLASSE PRINCIPAL PARA EXECUÇÃO ================

public class BibliotecaDigitalDemo {
    public static void main(String[] args) {
        System.out.println("🏛️ === SISTEMA DE BIBLIOTECA DIGITAL ===");
        System.out.println("Demonstração dos Conceitos de OOP\n");
        
        SistemaBiblioteca biblioteca = new SistemaBiblioteca();
        
        // Demonstrar listagens básicas
        biblioteca.listarUsuarios();
        biblioteca.listarLivros();
        
        // Demonstrar busca (com cache)
        System.out.println("\n🔍 === TESTE DE BUSCA ===");
        biblioteca.logOperacao("Iniciando busca por 'Java'");
        
        java.util.List<Livro> resultados = biblioteca.buscarLivro("Java");
        System.out.println("Livros encontrados com 'Java':");
        resultados.forEach(System.out::println);
        
        // Segunda busca (deve usar cache)
        System.out.println("\nSegunda busca pelo mesmo termo:");
        biblioteca.buscarLivro("Java");
        
        // Demonstrar reservas
        System.out.println("\n📝 === TESTE DE RESERVAS ===");
        biblioteca.logOperacao("Realizando reservas de teste");
        
        biblioteca.realizarReserva(1, 1); // João reserva Java
        biblioteca.realizarReserva(2, 2); // Maria reserva Clean Code
        biblioteca.realizarReserva(3, 3); // Admin reserva Design Patterns
        
        biblioteca.listarReservas();
        biblioteca.listarLivros(); // Mostrar status atualizado
        
        // Demonstrar polimorfismo
        biblioteca.demonstrarPolimorfismo();
        
        // Demonstrar generics
        biblioteca.demonstrarGenerics();
        
        // Gerar relatório
        biblioteca.gerarRelatorioUso();
        
        System.out.println("\n✅ Demonstração concluída!");
        System.out.println("📖 Este protótipo demonstra:");
        System.out.println("   • Herança (Usuario -> UsuarioComum/Administrador)");
        System.out.println("   • Polimorfismo (processamento uniforme de diferentes tipos)");
        System.out.println("   • Encapsulamento (atributos privados com getters/setters)");
        System.out.println("   • Interfaces (Reservavel, Persistivel, Notificavel)");
        System.out.println("   • Classes Abstratas (Usuario, Livro, Relatorio)");
        System.out.println("   • Sobrescrita de métodos (processarLogin, calcularDisponibilidade)");
        System.out.println("   • Sobrecarga de métodos/construtores (buscarLivro, construtores)");
        System.out.println("   • Modificadores final (constantes, métodos que não podem ser sobrescritos)");
        System.out.println("   • Tipos Genéricos (Repositorio<T>, Cache<K,V>)");
        System.out.println("   • Atributos e métodos estáticos de classe");
        System.out.println("   • Template Method Pattern (classe Relatorio)");
        
        // Demonstração adicional de conceitos avançados
        demonstrarConceitosAvancados(biblioteca);
    }
    
    private static void demonstrarConceitosAvancados(SistemaBiblioteca biblioteca) {
        System.out.println("\n🚀 === CONCEITOS AVANÇADOS ===");
        
        // Demonstrar uso de final
        System.out.println("\n🔒 Demonstração de 'final':");
        System.out.println("• Constante VERSAO (final): " + SistemaBiblioteca.getVersao());
        System.out.println("• Método logOperacao é final e não pode ser sobrescrito");
        
        // Demonstrar validação estática
        System.out.println("\n✅ Demonstração de métodos estáticos:");
        System.out.println("• Validação ISBN válido: " + Livro.validarISBN("978-85-7522-123-4"));
        System.out.println("• Validação ISBN inválido: " + Livro.validarISBN("123-invalid"));
        System.out.println("• Criptografia de senha: " + Usuario.criptografarSenha("minhasenha"));
        
        // Demonstrar comportamento de herança múltipла interfaces
        System.out.println("\n🔗 Demonstração de múltiplas interfaces:");
        LivroDigital livroDigital = new LivroDigital("Refactoring", "Martin Fowler");
        System.out.println("Livro implementa Reservavel: " + (livroDigital instanceof Reservavel));
        System.out.println("Livro implementa Persistivel: " + (livroDigital instanceof Persistivel));
        System.out.println("JSON do livro: " + livroDigital.toJson());
        
        // Demonstrar diferenças entre tipos de livros
        System.out.println("\n📱 Demonstração de especialização por herança:");
        LivroFisico livroFisico = new LivroFisico("Algoritmos", "Cormen");
        LivroDigital livroDigitalMulti = new LivroDigital("Padrões", "Gamma", null, "Design", "/files/patterns.pdf", 5);
        
        System.out.println("Livro físico - disponibilidade: " + livroFisico.calcularDisponibilidade());
        System.out.println("Livro digital - licenças disponíveis: " + livroDigitalMulti.getLicencasDisponiveis());
        
        // Simular múltiplas reservas digitais
        livroDigitalMulti.reservar(1);
        livroDigitalMulti.reservar(2);
        livroDigitalMulti.reservar(3);
        System.out.println("Após 3 reservas - ainda disponível: " + livroDigitalMulti.calcularDisponibilidade());
        System.out.println("Licenças em uso: " + livroDigitalMulti.getLicencasEmUso());
        
        // Demonstrar polimorfismo com array misto
        System.out.println("\n🎭 Polimorfismo avançado:");
        Livro[] livrosMistos = {
            new LivroFisico("Livro Físico 1", "Autor A"),
            new LivroDigital("Livro Digital 1", "Autor B"),
            new LivroFisico("Livro Físico 2", "Autor C"),
            new LivroDigital("Livro Digital 2", "Autor D")
        };
        
        for (Livro livro : livrosMistos) {
            System.out.println("Processando " + livro.getTipoMidia() + ": " + livro.getTitulo());
            System.out.println("  Disponível: " + livro.calcularDisponibilidade());
            System.out.println("  Pode ser reservado: " + livro.podeSerReservado());
        }
        
        // Demonstrar Template Method Pattern
        System.out.println("\n📊 Template Method Pattern:");
        java.util.List<Reserva> reservasDemo = java.util.Arrays.asList(
            new Reserva(1, 1),
            new Reserva(2, 2)
        );
        
        RelatorioUso relatorioUso = new RelatorioUso(reservasDemo);
        RelatorioInventario relatorioInventario = new RelatorioInventario(java.util.Arrays.asList(livrosMistos));
        
        System.out.println("Relatório de Uso:");
        System.out.println(relatorioUso.gerar());
        
        System.out.println("\nRelatório de Inventário:");
        System.out.println(relatorioInventario.gerar());
        
        // Demonstrar uso avançado de generics
        System.out.println("\n🧬 Generics Avançados:");
        demonstrarGenericsAvancados();
    }
    
    private static void demonstrarGenericsAvancados() {
        // Cache para diferentes tipos
        Cache<Integer, Usuario> cacheUsuarios = new Cache<>(10);
        Cache<String, java.util.List<String>> cacheBuscas = new Cache<>(20);
        
        // Populando caches
        UsuarioComum user = new UsuarioComum("Usuário Cache");
        cacheUsuarios.armazenar(999, user);
        
        java.util.List<String> resultadoBusca = java.util.Arrays.asList("Resultado1", "Resultado2");
        cacheBuscas.armazenar("busca_programacao", resultadoBusca);
        
        System.out.println("Cache de usuários contém ID 999: " + cacheUsuarios.contem(999));
        System.out.println("Cache de buscas contém 'busca_programacao': " + cacheBuscas.contem("busca_programacao"));
        
        // Repositório genérico com busca avançada
        Repositorio<String> repositorioTags = new Repositorio<>();
        repositorioTags.adicionar("java");
        repositorioTags.adicionar("python");
        repositorioTags.adicionar("javascript");
        repositorioTags.adicionar("java-advanced");
        
        java.util.List<String> tagsJava = repositorioTags.buscar(tag -> tag.contains("java"));
        System.out.println("Tags relacionadas a Java: " + tagsJava);
        
        // Demonstrar wildcards com generics
        demonstrarWildcards();
    }
    
    private static void demonstrarWildcards() {
        System.out.println("\n🃏 Wildcards com Generics:");
        
        // Lista de diferentes tipos de usuários
        java.util.List<UsuarioComum> usuariosComuns = java.util.Arrays.asList(
            new UsuarioComum("João"),
            new UsuarioComum("Maria")
        );
        
        java.util.List<Administrador> administradores = java.util.Arrays.asList(
            new Administrador("Admin1"),
            new Administrador("Admin2")
        );
        
        // Método que aceita qualquer lista de Usuario ou subtipos
        processarUsuarios(usuariosComuns);
        processarUsuarios(administradores);
    }
    
    // Método com wildcard para aceitar qualquer subtipo de Usuario
    private static void processarUsuarios(java.util.List<? extends Usuario> usuarios) {
        System.out.println("Processando " + usuarios.size() + " usuários:");
        for (Usuario usuario : usuarios) {
            System.out.println("  - " + usuario.getTipo() + ": " + usuario.getNome());
        }
    }
}

// ================ CLASSES ADICIONAIS PARA DEMONSTRAÇÃO ================

// Classe adicional para demonstrar Template Method Pattern
class RelatorioInventario extends Relatorio {
    private java.util.List<Livro> livros;
    
    public RelatorioInventario(java.util.List<Livro> livros) {
        super("Relatório de Inventário");
        this.livros = livros;
    }
    
    @Override
    protected String gerarConteudo() {
        StringBuilder conteudo = new StringBuilder();
        conteudo.append("Total de livros: ").append(livros.size()).append("\n");
        
        long livrosFisicos = livros.stream()
            .filter(l -> l instanceof LivroFisico)
            .count();
        
        long livrosDigitais = livros.stream()
            .filter(l -> l instanceof LivroDigital)
            .count();
        
        long livrosDisponiveis = livros.stream()
            .filter(Livro::isDisponivel)
            .count();
        
        conteudo.append("Livros físicos: ").append(livrosFisicos).append("\n");
        conteudo.append("Livros digitais: ").append(livrosDigitais).append("\n");
        conteudo.append("Livros disponíveis: ").append(livrosDisponiveis).append("\n");
        conteudo.append("Livros reservados: ").append(livros.size() - livrosDisponiveis);
        
        return conteudo.toString();
    }
}

// Extensão da classe SistemaBiblioteca para demonstrar método estático
class SistemaBiblioteca {
    private Repositorio<Usuario> repositorioUsuarios;
    private Repositorio<Livro> repositorioLivros;
    private Repositorio<Reserva> repositorioReservas;
    private Cache<String, java.util.List<Livro>> cacheConsultas;
    
    // Constantes final
    private static final String VERSAO = "1.0";
    private static final int CACHE_SIZE = 50;
    
    // Método estático para acessar versão
    public static String getVersao() {
        return VERSAO;
    }
    
    public SistemaBiblioteca() {
        this.repositorioUsuarios = new Repositorio<>();
        this.repositorioLivros = new Repositorio<>();
        this.repositorioReservas = new Repositorio<>();
        this.cacheConsultas = new Cache<>(CACHE_SIZE);
        
        inicializarDadosDemo();
    }
    
    private void inicializarDadosDemo() {
        // Criando usuários - demonstra polimorfismo e herança
        Usuario user1 = new UsuarioComum("João Silva", "joao@email.com", "123456");
        Usuario user2 = new UsuarioComum("Maria Santos");
        Usuario admin = new Administrador("Ana Admin", "ana@admin.com", "admin123");
        
        repositorioUsuarios.adicionar(user1);
        repositorioUsuarios.adicionar(user2);
        repositorioUsuarios.adicionar(admin);
        
        // Criando livros - demonstra polimorfismo e herança
        Livro livro1 = new LivroFisico("Java: Como Programar", "Paul Deitel", 
                                      "978-85-7522-123-4", "Programação", "Estante A1");
        Livro livro2 = new LivroDigital("Clean Code", "Robert Martin", 
                                       "978-85-7522-456-7", "Programação", 
                                       "/livros/clean_code.pdf", 3);
        Livro livro3 = new LivroFisico("Design Patterns", "Gang of Four");
        
        repositorioLivros.adicionar(livro1);
        repositorioLivros.adicionar(livro2);
        repositorioLivros.adicionar(livro3);
    }
    
    // Método sobrecarregado para busca
    public java.util.List<Livro> buscarLivro(String titulo) {
        return buscarLivro(titulo, null);
    }
    
    public java.util.List<Livro> buscarLivro(String titulo, String autor) {
        String chaveCache = titulo + (autor != null ? "_" + autor : "");
        
        // Verifica cache primeiro
        if (cacheConsultas.contem(chaveCache)) {
            System.out.println("📋 Resultado obtido do cache");
            return cacheConsultas.obter(chaveCache);
        }
        
        // Busca nos repositórios
        java.util.List<Livro> resultado = repositorioLivros.buscar(livro -> {
            boolean tituloMatch = livro.getTitulo().toLowerCase()
                                      .contains(titulo.toLowerCase());
            boolean autorMatch = autor == null || livro.getAutor().toLowerCase()
                                                      .contains(autor.toLowerCase());
            return tituloMatch && autorMatch;
        });
        
        // Armazena no cache
        cacheConsultas.armazenar(chaveCache, resultado);
        
        return resultado;
    }
    
    public boolean realizarReserva(int usuarioId, int livroId) {
        // Busca usuário e livro
        Usuario usuario = repositorioUsuarios.buscar(u -> u.getId() == usuarioId)
                                           .stream().findFirst().orElse(null);
        Livro livro = repositorioLivros.buscar(l -> l.getId() == livroId)
                                     .stream().findFirst().orElse(null);
        
        if (usuario == null || livro == null) {
            System.out.println("❌ Usuário ou livro não encontrado");
            return false;
        }
        
        if (!usuario.podeReservar()) {
            System.out.println("❌ Usuário não pode fazer mais reservas");
            return false;
        }
        
        if (!livro.podeSerReservado()) {
            System.out.println("❌ Livro não está disponível para reserva");
            return false;
        }
        
        // Realiza a reserva
        livro.reservar(usuarioId);
        Reserva reserva = new Reserva(usuarioId, livroId);
        repositorioReservas.adicionar(reserva);
        
        // Incrementa contador para usuários comuns
        if (usuario instanceof UsuarioComum) {
            ((UsuarioComum) usuario).incrementarReservas();
        }
        
        // Notifica o usuário
        usuario.receberNotificacao("Reserva realizada: " + livro.getTitulo());
        
        System.out.println("✅ Reserva realizada com sucesso!");
        return true;
    }
    
    public void listarUsuarios() {
        System.out.println("\n👥 === USUÁRIOS DO SISTEMA ===");
        repositorioUsuarios.obterTodos().forEach(System.out::println);
    }
    
    public void listarLivros() {
        System.out.println("\n📚 === CATÁLOGO DE LIVROS ===");
        repositorioLivros.obterTodos().forEach(livro -> {
            System.out.println(livro);
            if (!livro.isDisponivel()) {
                System.out.println("   📍 " + livro.getDetalhesReserva());
            }
        });
    }
    
    public void listarReservas() {
        System.out.println("\n📋 === RESERVAS ATIVAS ===");
        repositorioReservas.obterTodos().forEach(System.out::println);
    }
    
    public void gerarRelatorioUso() {
        RelatorioUso relatorio = new RelatorioUso(repositorioReservas.obterTodos());
        System.out.println("\n" + relatorio.gerar());
    }
    
    public void demonstrarPolimorfismo() {
        System.out.println("\n🔄 === DEMONSTRAÇÃO DE POLIMORFISMO ===");
        
        // Array com diferentes tipos de usuários
        java.util.List<Usuario> usuarios = repositorioUsuarios.obterTodos();
        
        System.out.println("Processando login de diferentes tipos de usuário:");
        for (Usuario usuario : usuarios) {
            usuario.processarLogin(); // Comportamento polimórfico
        }
        
        System.out.println("\nTestando autenticação:");
        for (Usuario usuario : usuarios) {
            boolean autenticado = false;
            if (usuario instanceof UsuarioComum) {
                autenticado = usuario.autenticar("123456");
            } else if (usuario instanceof Administrador) {
                autenticado = usuario.autenticar("admin123");
            }
            
            System.out.println(usuario.getNome() + ": " + 
                             (autenticado ? "✅ Autenticado" : "❌ Falhou"));
        }
    }
    
    public void demonstrarGenerics() {
        System.out.println("\n🧬 === DEMONSTRAÇÃO DE GENERICS ===");
        
        // Cache genérico em ação
        Cache<String, String> cacheInfo = new Cache<>(3);
        cacheInfo.armazenar("sistema", "Biblioteca Digital v" + VERSAO);
        cacheInfo.armazenar("usuarios", String.valueOf(repositorioUsuarios.tamanho()));
        cacheInfo.armazenar("livros", String.valueOf(repositorioLivros.tamanho()));
        
        System.out.println("Cache de informações:");
        System.out.println("Sistema: " + cacheInfo.obter("sistema"));
        System.out.println("Total de usuários: " + cacheInfo.obter("usuarios"));
        System.out.println("Total de livros: " + cacheInfo.obter("livros"));
        
        // Repositório genérico em ação
        System.out.println("\nRepositórios genéricos:");
        System.out.println("Usuários no repositório: " + repositorioUsuarios.tamanho());
        System.out.println("Livros no repositório: " + repositorioLivros.tamanho());
        System.out.println("Reservas no repositório: " + repositorioReservas.tamanho());
    }
    
    // Método final que não pode ser sobrescrito
    public final void logOperacao(String operacao) {
        System.out.println("🔍 LOG: " + java.time.LocalDateTime.now() + " - " + operacao);
    }