package br.ufpb.dcx.poo.biblioteca;

import java.nio.file.Path;
import java.util.List;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import org.junit.jupiter.api.*;

import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.BibliotecaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;
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
}
