package br.ufpb.dcx.poo.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.*;

class ReservaTest {

    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 2);
    private Biblioteca biblioteca;

    @BeforeEach
    void montarCenario() throws BibliotecaException {
        biblioteca = Fabrica.novaBiblioteca();
        biblioteca.acervo().cadastrarItem("L1", "Refatoração", "Fowler", "livro", 2004);
        biblioteca.acervo().adicionarExemplar("L1", "T-1");
        biblioteca.usuarios().cadastrarUsuario("2026001", "Ana");
        biblioteca.usuarios().cadastrarUsuario("2026002", "Bruno");
        biblioteca.usuarios().cadastrarUsuario("2026003", "Carla");
    }

    private StatusExemplar statusDeT1() throws BibliotecaException {
        return biblioteca.acervo().listarExemplares("L1").get(0).status();
    }

    @Test
    void cicloCompletoDeReserva() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertEquals(StatusExemplar.EMPRESTADO, statusDeT1());

        String rBruno = biblioteca.emprestimos().reservar("L1", "2026002", SEGUNDA);
        String rCarla = biblioteca.emprestimos().reservar("L1", "2026003", SEGUNDA);
        List<ReservaView> fila = biblioteca.emprestimos().listarReservas("L1");
        assertEquals(2, fila.size());
        assertEquals(1, fila.get(0).posicaoNaFila());
        assertEquals(2, fila.get(1).posicaoNaFila());

        biblioteca.emprestimos().devolver("T-1", SEGUNDA.plusDays(1));
        assertEquals(StatusExemplar.RESERVADO, statusDeT1());

        assertThrows(ExemplarIndisponivelException.class,
                () -> biblioteca.emprestimos().emprestar("T-1", "2026003", SEGUNDA.plusDays(1)));
        assertEquals(StatusExemplar.RESERVADO, statusDeT1());

        biblioteca.emprestimos().emprestar("T-1", "2026002", SEGUNDA.plusDays(1));
        assertEquals(StatusExemplar.EMPRESTADO, statusDeT1());
        assertEquals(1, biblioteca.emprestimos().listarReservas("L1").size());

        biblioteca.emprestimos().devolver("T-1", SEGUNDA.plusDays(2));
        assertEquals(StatusExemplar.RESERVADO, statusDeT1());

        biblioteca.emprestimos().cancelarReserva(rCarla);
        assertEquals(StatusExemplar.DISPONIVEL, statusDeT1());
        assertTrue(biblioteca.emprestimos().listarReservas("L1").isEmpty());
    }

    @Test
    void reservarComExemplarDisponivelEhRecusado() {
        assertThrows(OperacaoNaoPermitidaException.class,
                () -> biblioteca.emprestimos().reservar("L1", "2026002", SEGUNDA));
    }

    @Test
    void reservaDuplicadaDoMesmoUsuarioEhRecusada() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        biblioteca.emprestimos().reservar("L1", "2026002", SEGUNDA);
        assertThrows(OperacaoNaoPermitidaException.class,
                () -> biblioteca.emprestimos().reservar("L1", "2026002", SEGUNDA));
        assertEquals(1, biblioteca.emprestimos().listarReservas("L1").size());
    }

    @Test
    void reservarComUsuarioInativoEhRecusado() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        biblioteca.usuarios().desativarUsuario("2026002");
        assertThrows(UsuarioInativoException.class,
                () -> biblioteca.emprestimos().reservar("L1", "2026002", SEGUNDA));
    }

    @Test
    void reservarItemInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.emprestimos().reservar("X9", "2026002", SEGUNDA));
    }

    @Test
    void cancelarReservaInexistenteOuVazia() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.emprestimos().cancelarReserva("nao-existe"));
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.emprestimos().cancelarReserva(" "));
    }
    @Test
    void baixarExemplarEmprestadoEhRecusado() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        assertThrows(OperacaoNaoPermitidaException.class,
                () -> biblioteca.acervo().baixarExemplar("T-1"));
        assertEquals(StatusExemplar.EMPRESTADO, statusDeT1());
    }

    @Test
    void baixarDuasVezesEhRecusado() throws BibliotecaException {
        biblioteca.acervo().baixarExemplar("T-1");
        assertThrows(OperacaoNaoPermitidaException.class,
                () -> biblioteca.acervo().baixarExemplar("T-1"));
    }

    @Test
    void baixarComTomboEmBrancoOuInexistente() {
        assertThrows(DadosInvalidosException.class, () -> biblioteca.acervo().baixarExemplar(" "));
        assertThrows(RecursoNaoEncontradoException.class, () -> biblioteca.acervo().baixarExemplar("X"));
    }

    @Test
    void baixarExemplarReservadoSoltaAReserva() throws BibliotecaException {
        biblioteca.emprestimos().emprestar("T-1", "2026001", SEGUNDA);
        biblioteca.emprestimos().reservar("L1", "2026002", SEGUNDA);
        biblioteca.emprestimos().devolver("T-1", SEGUNDA.plusDays(1)); // RESERVADO
        biblioteca.acervo().baixarExemplar("T-1");
        assertEquals(StatusExemplar.INDISPONIVEL, statusDeT1());
        assertEquals(1, biblioteca.emprestimos().listarReservas("L1").size());
    }
}