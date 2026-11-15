package br.ufpb.dcx.poo.biblioteca;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
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
        biblioteca.usuarios().cadastrarUsuario("2026002", "Bruno");
    }

    private StatusExemplar statusDeT1() throws BibliotecaException {
        return biblioteca.acervo().listarExemplares("L1").stream()
                .filter(e -> e.tombo().equals("T-1"))
                .findFirst()
                .orElseThrow()
                .status();
    }

    private int emprestimosDe(String matricula) throws BibliotecaException {
        return biblioteca.emprestimos().listarEmprestimosDoUsuario(matricula).size();
    }

    @Test
    void dadosInvalidos() throws BibliotecaException {
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.emprestimos().emprestar(" ", "2026001", SEGUNDA));
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", null, SEGUNDA));
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026001", null));
        assertEquals(StatusExemplar.DISPONIVEL, statusDeT1());
        assertEquals(0, emprestimosDe("2026001"));
    }

    @Test
    void tomboInexistente() throws BibliotecaException {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.emprestimos().emprestar("T-99", "2026001", SEGUNDA));
        assertEquals(0, emprestimosDe("2026001"));
    }

    @Test
    void matriculaInexistente() throws BibliotecaException {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "9999999", SEGUNDA));
        assertEquals(StatusExemplar.DISPONIVEL, statusDeT1());
    }

    @Test
    void usuarioInativo() throws BibliotecaException {
        biblioteca.usuarios().desativarUsuario("2026001");
        assertThrows(UsuarioInativoException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA));
        assertEquals(StatusExemplar.DISPONIVEL, statusDeT1());
        assertEquals(0, emprestimosDe("2026001"));
    }

    @Test
    void exemplarJaEmprestado() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertThrows(ExemplarIndisponivelException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026002", SEGUNDA));
        assertEquals(StatusExemplar.EMPRESTADO, statusDeT1());
        assertEquals(0, emprestimosDe("2026002"));
    }

    @Test
    void exemplarBaixadoEhIndisponivel() throws BibliotecaException {
        biblioteca.acervo().baixarExemplar("T-1");
        assertThrows(ExemplarIndisponivelException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA));
        assertEquals(StatusExemplar.INDISPONIVEL, statusDeT1());
    }

    @Test
    void limiteDeEmprestimos() throws BibliotecaException {
        int limite = EmprestimoService.LIMITE_DE_EMPRESTIMOS;
        for (int i = 0; i < limite; i++) {
            biblioteca.acervo().adicionarExemplar("L1", "E-" + i);
            biblioteca.emprestimos().emprestar("E-" + i, "2026001", SEGUNDA);
        }
        assertThrows(LimiteDeEmprestimosExcedidoException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA));
        assertEquals(StatusExemplar.DISPONIVEL, statusDeT1());
        assertEquals(limite, emprestimosDe("2026001"));
    }

    @Test
    void inativoTemPrioridadeSobreExemplarEmprestado() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026002", SEGUNDA);
        biblioteca.usuarios().desativarUsuario("2026001");
        assertThrows(UsuarioInativoException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA));
    }

    @Test
    void exemplarIndisponivelTemPrioridadeSobreLimite() throws BibliotecaException {
        int limite = EmprestimoService.LIMITE_DE_EMPRESTIMOS;
        for (int i = 0; i < limite; i++) {
            biblioteca.acervo().adicionarExemplar("L1", "E-" + i);
            biblioteca.emprestimos().emprestar("E-" + i, "2026001", SEGUNDA);
        }
        biblioteca.emprestimos().emprestar("T-1", "2026002", SEGUNDA);
        assertThrows(ExemplarIndisponivelException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA));
    }

    @Test
    void emprestimoTemPrazoDeQuatorzeDias() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        EmprestimoView e = biblioteca.emprestimos().listarEmprestimosDoUsuario("2026001").get(0);
        assertEquals(LocalDate.of(2026, 11, 16), e.dataPrevistaDeDevolucao());
        assertEquals(StatusEmprestimo.ATIVO, e.status());
        assertEquals(StatusExemplar.EMPRESTADO, statusDeT1());
    }

    @Test
    void noDiaPrevistoAindaNaoHaAtraso() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertEquals(0, biblioteca.emprestimos().listarAtrasados(LocalDate.of(2026, 11, 16)).size());
        assertEquals(1, biblioteca.emprestimos().listarAtrasados(LocalDate.of(2026, 11, 17)).size());
    }

    @Test
    void devolverComDataAnteriorEhInvalido() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.emprestimos().devolver("T-1", SEGUNDA.minusDays(1)));
        assertEquals(StatusExemplar.EMPRESTADO, statusDeT1());
    }

    @Test
    void devolverSemEmprestimoAtivoEhRecusado() {
        assertThrows(OperacaoNaoPermitidaException.class,
                () -> biblioteca.emprestimos().devolver("T-1", SEGUNDA));
    }

    @Test
    void emprestimosDoUsuarioVemDoMaisRecenteParaOMaisAntigo() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        biblioteca.emprestimos().devolver("T-1", SEGUNDA.plusDays(1));
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA.plusDays(5));
        var lista = biblioteca.emprestimos().listarEmprestimosDoUsuario("2026001");
        assertEquals(SEGUNDA.plusDays(5), lista.get(0).dataDoEmprestimo());
        assertEquals(StatusEmprestimo.DEVOLVIDO, lista.get(1).status());
    }
}
