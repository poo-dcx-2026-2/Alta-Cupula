package br.ufpb.dcx.poo.biblioteca;

import br.ufpb.dcx.poo.biblioteca.contrato.Biblioteca;
import br.ufpb.dcx.poo.biblioteca.contrato.EmprestimoView;
import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.BibliotecaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.UsuarioInativoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmprestimoTest {

    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 2);
    private Biblioteca biblioteca;

    @BeforeEach
    void montarCenario() throws BibliotecaException {
        biblioteca = Fabrica.novaBiblioteca();
        biblioteca.acervo().cadastrarItem("L1", "Refatoração", "Fowler", "livro", 2004);
        biblioteca.acervo().adicionarExemplar("L1", "T-1");
        biblioteca.usuarios().cadastrarUsuario("2026001", "Ana");
    }

    @Test
    void tomboInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.emprestimos().emprestar("T-99", "2026001", SEGUNDA));
    }

    @Test
    void matriculaInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "9999999", SEGUNDA));
    }

    @Test
    void usuarioInativoTemPrioridadeSobreExemplarEmprestado() throws BibliotecaException {
        biblioteca.usuarios().cadastrarUsuario("2026002", "Bruno");
        biblioteca.emprestimos().emprestar("T-1", "2026002", SEGUNDA); // T-1 emprestado
        biblioteca.usuarios().desativarUsuario("2026001");
        assertThrows(UsuarioInativoException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA));
    }

    @Test
    void emprestimoTemPrazoDeQuatorzeDias() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        EmprestimoView e = biblioteca.emprestimos()
                .listarEmprestimosDoUsuario("2026001").get(0);
        assertEquals(LocalDate.of(2026, 11, 16), e.dataPrevistaDeDevolucao());
    }

    @Test
    void noDiaPrevistoAindaNaoHaAtraso() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertEquals(0, biblioteca.emprestimos()
                .listarAtrasados(LocalDate.of(2026, 11, 16)).size());
        assertEquals(1, biblioteca.emprestimos()
                .listarAtrasados(LocalDate.of(2026, 11, 17)).size());
    }

    @Test
    void devolverLiberaOExemplar() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        biblioteca.emprestimos().devolver("T-1", SEGUNDA.plusDays(3));
        assertEquals(StatusExemplar.DISPONIVEL,
                biblioteca.acervo().listarExemplares("L1").get(0).status());
    }

}
