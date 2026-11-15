package br.ufpb.dcx.poo.biblioteca.filme;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.ExemplarIndisponivelException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.LimiteDeEmprestimosExcedidoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.UsuarioInativoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;

/**

 Esqueleto dos empréstimos. Conteúdo da Entrega 2.*
 <p>As regras estão no enunciado da entrega, não aqui. Repare que
 {@link EmprestimoService#PRAZO_EM_DIAS} e
 {@link EmprestimoService#LIMITE_DE_EMPRESTIMOS} são constantes do contrato: os
 testes contam com esses valores.</p>*/

public class Emprestimo implements EmprestimoService, Serializable {

    private final AcervoFilme acervo;
    private final UsuarioService usuarios;
    private final List<RegistroEmprestimo> emprestimos = new ArrayList<>();
    private final List<RegistroReserva> reservas = new ArrayList<>();

    public Emprestimo(AcervoFilme acervo, UsuarioService usuarios) {
        this.acervo = acervo;
        this.usuarios = usuarios;
    }

    public int contarAtivos(String matricula) {
        return (int) emprestimos.stream()
                .filter(e -> e.getMatricula().equals(matricula) && e.estaAtivo())
                .count();
    }

    @Override
    public String emprestar(String tombo, String matricula, LocalDate data)
            throws RecursoNaoEncontradoException, ExemplarIndisponivelException,
            UsuarioInativoException, LimiteDeEmprestimosExcedidoException, DadosInvalidosException {

        if (contarAtivos(matricula) >= LIMITE_DE_EMPRESTIMOS) {
            throw new LimiteDeEmprestimosExcedidoException(
                    "Usuário excedeu o limite de " + LIMITE_DE_EMPRESTIMOS + " empréstimos.");
        }

        if (matricula == null || matricula.trim().isEmpty()) {
            throw new DadosInvalidosException("A Matricula do usuario não pode ser vazio.");
        }

        if (tombo == null || tombo.trim().isEmpty()) {
            throw new DadosInvalidosException("O tombo do exemplar não pode ser vazio.");
        }

        Exemplar exemplar = acervo.localizarExemplar(tombo);
        if (exemplar == null) {
            throw new RecursoNaoEncontradoException("Exemplar não encontrado: " + tombo);
        }

        UsuarioView usuario = usuarios.buscarUsuario(matricula);
        if (!usuario.ativo()) {
            throw new UsuarioInativoException("Usuário inativo: " + matricula);
        }

        if (exemplar.getStatus() != StatusExemplar.DISPONIVEL) {
            throw new ExemplarIndisponivelException("Exemplar indisponível: " + tombo);
        }

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

        exemplar.setStatus(StatusExemplar.EMPRESTADO);
        String id = UUID.randomUUID().toString();
        emprestimos.add(new RegistroEmprestimo(id, tombo, matricula, data,
                data.plusDays(PRAZO_EM_DIAS)));

        return id;

    }

    @Override
    public void devolver(String tombo, LocalDate data)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {

        if (tombo == null || tombo.isBlank() || data == null) {
            throw new DadosInvalidosException("Tombo e data são obrigatórios.");
        }

        Exemplar exemplar = acervo.localizarExemplar(tombo);
        if (exemplar == null) {
            throw new RecursoNaoEncontradoException("Exemplar não encontrado: " + tombo);
        }

        RegistroEmprestimo ativo = emprestimos.stream()
                .filter(e -> e.getTombo().equals(tombo) && e.estaAtivo())
                .findFirst()
                .orElseThrow(() -> new OperacaoNaoPermitidaException(
                        "Não há empréstimo ativo para o tombo: " + tombo));

        if (data.isBefore(ativo.getDataEmprestimo())) {
            throw new DadosInvalidosException("Devolução anterior ao empréstimo.");
        }

        ativo.setDataDevolucaoReal(data);
        exemplar.setStatus(StatusExemplar.DISPONIVEL);
    }

    @Override
    public List<EmprestimoView> listarEmprestimosDoUsuario(String matricula)
            throws RecursoNaoEncontradoException {

        if (matricula == null || matricula.isBlank()) {
            throw new DadosInvalidosException("A matrícula é obrigatória.");
        }
        usuarios.buscarUsuario(matricula);

        return emprestimos.stream()
                .filter(e -> e.getMatricula().equals(matricula))
                .sorted((a, b) -> b.getDataEmprestimo().compareTo(a.getDataEmprestimo()))
                .map(e -> e.toView(e.getDataEmprestimo()))
                .collect(Collectors.toList());
    }

    @Override
    public List<EmprestimoView> listarAtrasados(LocalDate data) {
        if (data == null) {
            throw new DadosInvalidosException("A data é obrigatória.");
        }
        return emprestimos.stream()
                .filter(e -> e.estaAtrasadoEm(data))
                .map(e -> e.toView(data))
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

    private static class RegistroEmprestimo implements Serializable {
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

        public String getId() { return id; }
        public LocalDate getDataEmprestimo() { return dataEmprestimo; }
        public String getTombo() { return tombo; }
        public String getMatricula() { return matricula; }
        public LocalDate getDataPrevista() { return dataPrevista; }
        public LocalDate getDataDevolucaoReal() { return dataDevolucaoReal; }
        public void setDataDevolucaoReal(LocalDate dataDevolucaoReal) { this.dataDevolucaoReal = dataDevolucaoReal; }

        public boolean estaAtivo() {
            return dataDevolucaoReal == null;
        }

        public boolean estaAtrasadoEm(LocalDate data) {
            return estaAtivo() && data.isAfter(dataPrevista);
        }

        public EmprestimoView toView(LocalDate dataReferencia) {
            StatusEmprestimo status = estaAtrasadoEm(dataReferencia) ? StatusEmprestimo.ATRASADO
                    : estaAtivo() ? StatusEmprestimo.ATIVO
                      : StatusEmprestimo.DEVOLVIDO;
            return new EmprestimoView(id, tombo, matricula, dataEmprestimo,
                    dataPrevista, dataDevolucaoReal, status);
        }
    }

    private static class RegistroReserva implements Serializable {
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