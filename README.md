🎯 Conceitos OOP Implementados no Protótipo:
1. Classes e Objetos

Usuario, Livro, Reserva, SistemaBiblioteca
Cada classe modela uma entidade específica do domínio

2. Herança

Usuario → UsuarioComum, Administrador
Livro → LivroFisico, LivroDigital
Relatorio → RelatorioUso, RelatorioInventario

3. Polimorfismo

Arrays mistos de Usuario[] e Livro[] processados uniformemente
Método processarLogin() com comportamento específico para cada tipo
Método calcularDisponibilidade() com lógica diferente para cada tipo de livro

4. Encapsulamento

Atributos privados/protegidos com getters/setters
Validação interna de dados sensíveis (senhas, tokens)

5. Interfaces

Reservavel - para itens que podem ser reservados
Persistivel - para serialização de dados
Notificavel - para sistema de notificações

6. Classes Abstratas

Usuario - define estrutura comum para usuários
Livro - base para diferentes tipos de livros
Relatorio - implementa Template Method Pattern

7. Sobrescrita

processarLogin() - comportamento específico por tipo de usuário
calcularDisponibilidade() - lógica diferente para livros físicos/digitais
autenticar() - validação adicional para administradores

8. Sobrecarga

Construtores múltiplos para todas as classes
Método buscarLivro() com diferentes parâmetros

9. Modificador final

Constantes VERSAO, MAX_RESERVAS, CACHE_SIZE
Método logOperacao() que não pode ser sobrescrito

10. Tipos Genéricos

Repositorio<T> - repositório genérico para qualquer tipo
Cache<K,V> - cache genérico com chave/valor
Wildcards com <? extends Usuario>

11. Atributos/Métodos Estáticos

proximoId para geração de IDs únicos
criptografarSenha(), validarISBN() - utilitários estáticos

🚀 Funcionalidades do Protótipo:

Sistema Completo de Usuários

Cadastro de usuários comuns e administradores
Autenticação com senhas criptografadas
Sistema de permissões diferenciadas


Gerenciamento de Acervo

Livros físicos e digitais com características específicas
Sistema de disponibilidade inteligente
Controle de licenças para livros digitais


Sistema de Reservas

Validação de regras de negócio
Notificações automáticas
Controle de limites por usuário


Cache e Performance

Cache genérico para consultas frequentes
Repositórios otimizados com busca por critérios


Relatórios Dinâmicos

Template Method Pattern para diferentes tipos de relatório
Geração automática de estatísticas


Demonstrações Interativas

Todos os conceitos OOP em ação
Exemplos práticos de polimorfismo e generics
Validações e casos de uso reais



📊 Saída do Programa Inclui:

Listagem de usuários, livros e reservas
Demonstração de busca com cache
Testes de autenticação polimórfica
Relatórios formatados
Validações de regras de negócio
Logs de operações