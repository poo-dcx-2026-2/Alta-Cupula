package br.ufpb.dcx.poo.biblioteca;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.ExemplarIndisponivelException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.PersistenciaException;
import org.junit.jupiter.api.*;

import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.BibliotecaException;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenciaDaBibliotecaTest {

    @TempDir
    Path pasta;

    @BeforeEach
    void apontarOsDadosParaAPastaTemporaria() {
        System.setProperty("biblioteca.dados", pasta.toString());
    }

    @AfterEach
    void desfazerAPropriedade() {
        System.clearProperty("biblioteca.dados");
    }

    @Test
    void estadoSalvoVoltaNumaBibliotecaNova() throws BibliotecaException {
        Biblioteca original = Fabrica.novaBiblioteca();
        original.acervo().cadastrarItem(
                "L1", "Refatoração", "Fowler", "livro", 2004);
        original.salvar();

        Biblioteca recarregada = Fabrica.novaBiblioteca();
        recarregada.carregar();

        assertEquals("Refatoração", recarregada.acervo().buscarItem("L1").titulo());
    }

    @Test
    void multiplosItensPersistemCorretamenteAposRecarregar() throws BibliotecaException {
        Biblioteca original = Fabrica.novaBiblioteca();

        original.acervo().cadastrarItem("L1", "Refatoração", "Fowler", "livro", 2004);
        original.acervo().cadastrarItem("L2", "Código Limpo", "Robert Martin", "livro", 2008);

        original.salvar();

        Biblioteca C2 = Fabrica.novaBiblioteca();
        C2.carregar();

        assertEquals("Refatoração", C2.acervo().buscarItem("L1").titulo());
        assertEquals("Código Limpo", C2.acervo().buscarItem("L2").titulo());
    }

    @Test
    void carregarBibliotecaSemArquivoSalvoNaoDeveLancarExcecao() {
        Biblioteca original = Fabrica.novaBiblioteca();

        assertDoesNotThrow(() -> original.carregar());
    }

    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 2);

    private void montarEstadoCompleto(Biblioteca b) throws BibliotecaException {
        b.acervo().cadastrarItem("L1", "Refatoração", "Fowler", "livro", 2004);
        b.acervo().adicionarExemplar("L1", "T-1");
        b.usuarios().cadastrarUsuario("2026001", "Ana");
        b.usuarios().cadastrarUsuario("2026002", "Bruno");
        b.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        b.emprestimos().reservar("L1", "2026002", SEGUNDA);
    }

    @Test
    void usuariosEmprestimosEReservasSobrevivemAIdaEVolta() throws BibliotecaException {
        Biblioteca original = Fabrica.novaBiblioteca();
        montarEstadoCompleto(original);
        original.salvar();

        Biblioteca recarregada = Fabrica.novaBiblioteca();
        recarregada.carregar();

        assertEquals(2, recarregada.usuarios().listarUsuarios().size());
        assertEquals(1, recarregada.usuarios().buscarUsuario("2026001").emprestimosAtivos());
        assertEquals(1, recarregada.emprestimos().listarReservas("L1").size());
        assertEquals(StatusExemplar.EMPRESTADO,
                recarregada.acervo().listarExemplares("L1").get(0).status());
    }

    @Test
    void exemplarReservadoMantemODonoDepoisDeRecarregar() throws BibliotecaException {
        Biblioteca original = Fabrica.novaBiblioteca();
        montarEstadoCompleto(original);
        original.emprestimos().devolver("T-1", SEGUNDA.plusDays(1)); // RESERVADO para Bruno
        original.salvar();

        Biblioteca recarregada = Fabrica.novaBiblioteca();
        recarregada.carregar();

        assertEquals(StatusExemplar.RESERVADO,
                recarregada.acervo().listarExemplares("L1").get(0).status());
        assertThrows(ExemplarIndisponivelException.class,
                () -> recarregada.emprestimos().emprestar("T-1", "2026001", SEGUNDA.plusDays(2)));
        recarregada.emprestimos().emprestar("T-1", "2026002", SEGUNDA.plusDays(2));
    }

    @Test
    void bibliotecaCarregadaContinuaFuncionando() throws BibliotecaException {
        Biblioteca original = Fabrica.novaBiblioteca();
        montarEstadoCompleto(original);
        original.salvar();

        Biblioteca recarregada = Fabrica.novaBiblioteca();
        recarregada.carregar();
        recarregada.emprestimos().devolver("T-1", SEGUNDA.plusDays(1));
        // o usuário carregado enxerga o empréstimo carregado (ligação preservada)
        assertEquals(0, recarregada.usuarios().buscarUsuario("2026001").emprestimosAtivos());
    }

    @Test
    void arquivoCorrompidoLancaPersistenciaExceptionEMantemOEstado() throws Exception {
        Biblioteca b = Fabrica.novaBiblioteca();
        b.acervo().cadastrarItem("L1", "Refatoração", "Fowler", "livro", 2004);
        Files.writeString(pasta.resolve("biblioteca.dat"), "isto não é um objeto serializado");

        PersistenciaException erro = assertThrows(PersistenciaException.class, b::carregar);
        assertNotNull(erro.getCause());
        assertEquals("Refatoração", b.acervo().buscarItem("L1").titulo());
    }

    @Test
    void semArquivoCarregarNaoAlteraNada() throws BibliotecaException {
        Biblioteca b = Fabrica.novaBiblioteca();
        b.carregar();
        assertTrue(b.acervo().listarItens().isEmpty());
    }
}
