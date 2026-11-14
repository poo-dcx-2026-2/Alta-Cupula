package br.ufpb.dcx.poo.biblioteca.filme;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import br.ufpb.dcx.poo.biblioteca.contrato.EmprestimoService;
import br.ufpb.dcx.poo.biblioteca.contrato.EmprestimoView;
import br.ufpb.dcx.poo.biblioteca.contrato.ReservaView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.ExemplarIndisponivelException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.LimiteDeEmprestimosExcedidoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.UsuarioInativoException;

/**

 Esqueleto dos empréstimos. Conteúdo da Entrega 2.*
 <p>As regras estão no enunciado da entrega, não aqui. Repare que
 {@link EmprestimoService#PRAZO_EM_DIAS} e
 {@link EmprestimoService#LIMITE_DE_EMPRESTIMOS} são constantes do contrato: os
 testes contam com esses valores.</p>*/

public class Emprestimo implements EmprestimoService, Serializable {

    private List<RegistroEmprestimo> emprestimos;
    private List<RegistroReserva> reservas;

    public Emprestimo() {
        this.emprestimos = new ArrayList<>();
        this.reservas = new ArrayList<>();
    }

    @Override
    public String emprestar(String tombo, String matricula, LocalDate data)
            throws RecursoNaoEncontradoException, ExemplarIndisponivelException,
            UsuarioInativoException, LimiteDeEmprestimosExcedidoException {

        long emprestimosAtivos = emprestimos.stream()
                .filter(e -> e.getMatricula().equals(matricula) && e.getDataDevolucaoReal() == null)
                .count();

        if (emprestimosAtivos >= LIMITE_DE_EMPRESTIMOS) {
            throw new LimiteDeEmprestimosExcedidoException("Usuário excedeu o limite de " + LIMITE_DE_EMPRESTIMOS + " empréstimos.");
        }

        boolean Emprestado = emprestimos.stream()
                .anyMatch(e -> e.getTombo().equals(tombo) && e.getDataDevolucaoReal() == null);

        if (Emprestado) {
            throw new ExemplarIndisponivelException("O exemplar com tombo " + tombo + " já se encontra emprestado.");
        }

        String idEmprestimo = UUID.randomUUID().toString();
        LocalDate dataPrevista = data.plusDays(PRAZO_EM_DIAS);

        emprestimos.add(new RegistroEmprestimo(idEmprestimo, tombo, matricula, data, dataPrevista));

        return idEmprestimo;
    }

    @Override
    public void devolver(String tombo, LocalDate data)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {

        RegistroEmprestimo emprestimoAtivo = emprestimos.stream()
                .filter(e -> e.getTombo().equals(tombo) && e.getDataDevolucaoReal() == null)
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não há empréstimo ativo para o tombo: " + tombo));

        emprestimoAtivo.setDataDevolucaoReal(data);
    }

    @Override
    public List<EmprestimoView> listarEmprestimosDoUsuario(String matricula)
            throws RecursoNaoEncontradoException {

        List<EmprestimoView> resultado = emprestimos.stream()
                .filter(e -> e.getMatricula().equals(matricula))
                .map(RegistroEmprestimo::toView)
                .collect(Collectors.toList());

        if (resultado.isEmpty()) {
        }

        return resultado;
    }

    @Override
    public List<EmprestimoView> listarAtrasados(LocalDate data) {
        return emprestimos.stream()
                .filter(e -> e.getDataDevolucaoReal() == null && data.isAfter(e.getDataPrevista()))
                .map(RegistroEmprestimo::toView)
                .collect(Collectors.toList());
    }

    @Override
    public String reservar(String codigoDoItem, String matricula, LocalDate data)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {

        String idReserva = UUID.randomUUID().toString();
        reservas.add(new RegistroReserva(idReserva, codigoDoItem, matricula, data));
        return idReserva;
    }

    @Override
    public List<ReservaView> listarReservas(String codigoDoItem)
            throws RecursoNaoEncontradoException {

        return reservas.stream()
                .filter(r -> r.getCodigoDoItem().equals(codigoDoItem))
                .map(RegistroReserva::toView)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelarReserva(String idDaReserva) throws RecursoNaoEncontradoException {
        boolean removido = reservas.removeIf(r -> r.getId().equals(idDaReserva));
        if (!removido) {
            throw new RecursoNaoEncontradoException("Reserva não encontrada com ID: " + idDaReserva);
        }
    }

    private static class RegistroEmprestimo {
        private String id;
        private String tombo;
        private String matricula;
        private LocalDate dataEmprestimo;
        private LocalDate dataPrevista;
        private LocalDate dataDevolucaoReal;

        public RegistroEmprestimo(String id, String tombo, String matricula, LocalDate dataEmprestimo, LocalDate dataPrevista) {
            this.id = id;
            this.tombo = tombo;
            this.matricula = matricula;
            this.dataEmprestimo = dataEmprestimo;
            this.dataPrevista = dataPrevista;
            this.dataDevolucaoReal = null;
        }

        public String getTombo() { return tombo; }
        public String getMatricula() { return matricula; }
        public LocalDate getDataPrevista() { return dataPrevista; }
        public LocalDate getDataDevolucaoReal() { return dataDevolucaoReal; }
        public void setDataDevolucaoReal(LocalDate dataDevolucaoReal) { this.dataDevolucaoReal = dataDevolucaoReal; }

        public EmprestimoView toView() {
            return null;
        }
    }

    private static class RegistroReserva {
        private String id;
        private String codigoDoItem;
        private String matricula;
        private LocalDate dataReserva;

        public RegistroReserva(String id, String codigoDoItem, String matricula, LocalDate dataReserva) {
            this.id = id;
            this.codigoDoItem = codigoDoItem;
            this.matricula = matricula;
            this.dataReserva = dataReserva;
        }

        public String getId() { return id; }
        public String getCodigoDoItem() { return codigoDoItem; }

        public ReservaView toView() {
            return null;
        }
    }
}