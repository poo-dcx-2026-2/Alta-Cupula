package br.ufpb.dcx.poo.biblioteca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.ufpb.dcx.poo.biblioteca.contrato.Biblioteca;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.BibliotecaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

/** Testes públicos dos usuários. */
class UsuarioTest {

    private Biblioteca biblioteca;

    @BeforeEach
    void criarBibliotecaVazia() {
        biblioteca = Fabrica.novaBiblioteca();
    }

    @Test
    @DisplayName("usuário cadastrado começa ativo e sem empréstimos")
    void cadastrarEBuscar() throws BibliotecaException {
        biblioteca.usuarios().cadastrarUsuario("2026001", "Ana Souza");

        UsuarioView usuario = biblioteca.usuarios().buscarUsuario("2026001");

        assertEquals("Ana Souza", usuario.nome());
        assertTrue(usuario.ativo());
        assertEquals(0, usuario.emprestimosAtivos());
    }

    @Test
    @DisplayName("matrícula repetida é recusada")
    void matriculaDuplicada() throws BibliotecaException {
        biblioteca.usuarios().cadastrarUsuario("2026001", "Ana Souza");

        assertThrows(RecursoDuplicadoException.class,
                () -> biblioteca.usuarios().cadastrarUsuario("2026001", "Outro Nome"));
    }

    @Test
    @DisplayName("buscar usuário inexistente lança RecursoNaoEncontradoException")
    void buscarInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.usuarios().buscarUsuario("0000"));
    }

    @Test
    @DisplayName("nome em branco é entrada inválida")
    void nomeEmBranco() {
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.usuarios().cadastrarUsuario("2026001", "   "));
    }

    @Test
    @DisplayName("listar devolve os usuários ordenados por nome")
    void listarOrdenado() throws BibliotecaException {
        biblioteca.usuarios().cadastrarUsuario("3", "Carlos");
        biblioteca.usuarios().cadastrarUsuario("1", "Ana");
        biblioteca.usuarios().cadastrarUsuario("2", "Bruno");

        assertEquals("Ana", biblioteca.usuarios().listarUsuarios().get(0).nome());
        assertEquals("Carlos", biblioteca.usuarios().listarUsuarios().get(2).nome());
    }

    @Test
    @DisplayName("Tenta Deativar um usuario")
    void DesativarUsuarioComSucesso() throws BibliotecaException {

        biblioteca.usuarios().cadastrarUsuario("2026001", "Ana Silva");

        biblioteca.usuarios().desativarUsuario("2026001");

        UsuarioView view = biblioteca.usuarios().buscarUsuario("2026001");
        assertFalse(view.ativo(), "O usuário deveria estar desativado.");
    }

    @Test
    @DisplayName("Tenta Reativar um usuario")
    void ReativarUsuarioComSucesso() throws BibliotecaException {
        biblioteca.usuarios().cadastrarUsuario("2026001", "Ana Silva");
        biblioteca.usuarios().desativarUsuario("2026001");

        biblioteca.usuarios().reativarUsuario("2026001");

        UsuarioView view = biblioteca.usuarios().buscarUsuario("2026001");
        assertTrue(view.ativo(), "O usuário deveria estar reativado.");
    }
    @Test
    @DisplayName("não é possível desativar usuário com empréstimo ativo")
    void naoDeveDesativarUsuarioComEmprestimoAtivo() throws BibliotecaException {
        biblioteca.usuarios().cadastrarUsuario("2026001", "Ana Silva");

        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.usuarios().desativarUsuario(""));
    }

    @Test
    @DisplayName("reativar usuário inexistente lança RecursoNaoEncontradoException")
    void reativarInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.usuarios().reativarUsuario("999999"));
    }
}
