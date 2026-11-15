package br.ufpb.dcx.poo.biblioteca.filme;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.ufpb.dcx.poo.biblioteca.contrato.EmprestimoView;
import br.ufpb.dcx.poo.biblioteca.contrato.StatusEmprestimo;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

public class MultaService {

    public static final BigDecimal VALOR_POR_DIA = new BigDecimal("0.50");

    private final Emprestimo emprestimos;

    public MultaService(Emprestimo emprestimos) {
        this.emprestimos = emprestimos;
    }

    public BigDecimal multaDe(EmprestimoView e, java.time.LocalDate referencia) {
        java.time.LocalDate fim = e.dataDaDevolucao() != null ? e.dataDaDevolucao() : referencia;
        long dias = ChronoUnit.DAYS.between(e.dataPrevistaDeDevolucao(), fim);
        return dias > 0 ? VALOR_POR_DIA.multiply(BigDecimal.valueOf(dias)) : BigDecimal.ZERO;
    }

    public BigDecimal multaTotalDoUsuario(String matricula, java.time.LocalDate referencia)
            throws RecursoNaoEncontradoException {
        if (referencia == null) {
            throw new DadosInvalidosException("A data é obrigatória.");
        }
        List<EmprestimoView> lista = emprestimos.listarEmprestimosDoUsuario(matricula);
        return lista.stream()
                .map(e -> multaDe(e, referencia))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}