package br.ufpb.dcx.poo.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;
import br.ufpb.dcx.poo.biblioteca.filme.Cineclube;
import br.ufpb.dcx.poo.biblioteca.filme.MultaService;

class MultaTest {

    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 2);
    private Cineclube biblioteca;
    private MultaService multas;

    @BeforeEach
    void montarCenario() throws BibliotecaException {
        biblioteca = new Cineclube();
        biblioteca.acervo().cadastrarItem("L1", "Refatoração", "Fowler", "livro", 2004);
        biblioteca.acervo().adicionarExemplar("L1", "T-1");
        biblioteca.usuarios().cadastrarUsuario("2026001", "Ana");
        multas = biblioteca.multas();
    }

    @Test
    void semAtrasoNaoHaMulta() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertEquals(0, BigDecimal.ZERO.compareTo(
                multas.multaTotalDoUsuario("2026001", LocalDate.of(2026, 11, 16))));
    }

    @Test
    void tresDiasDeAtrasoCustamUmRealEMeio() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertEquals(0, new BigDecimal("1.50").compareTo(
                multas.multaTotalDoUsuario("2026001", LocalDate.of(2026, 11, 19))));
    }

    @Test
    void multaFicaCongeladaNaDataDaDevolucao() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        biblioteca.emprestimos().devolver("T-1", LocalDate.of(2026, 11, 18)); // 2 dias
        assertEquals(0, new BigDecimal("1.00").compareTo(
                multas.multaTotalDoUsuario("2026001", LocalDate.of(2026, 12, 31))));
    }

    @Test
    void devolverContinuaDevolvendoComAtraso() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertDoesNotThrow(() ->
                biblioteca.emprestimos().devolver("T-1", LocalDate.of(2026, 11, 30)));
    }

    @Test
    void usuarioInexistenteEDataNula() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> multas.multaTotalDoUsuario("999", SEGUNDA));
        assertThrows(DadosInvalidosException.class,
                () -> multas.multaTotalDoUsuario("2026001", null));
    }
}