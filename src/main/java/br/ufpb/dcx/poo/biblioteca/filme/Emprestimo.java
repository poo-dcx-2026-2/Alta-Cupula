package br.ufpb.dcx.poo.biblioteca.filme;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, FilaDeReservas> filas = new HashMap<>();

    public Emprestimo(AcervoFilme acervo, UsuarioService usuarios) {
        this.acervo = acervo;
        this.usuarios = usuarios;
    }

    public int contarAtivos(String matricula) {
        return (int) emprestimos.stream()
                .filter(e -> e.getMatricula().equals(matricula) && e.estaAtivo())
                .count();
    }

    private FilaDeReservas fila(String codigoDoItem) {
        return filas.computeIfAbsent(codigoDoItem, c -> new FilaDeReservas());
    }

    /** Depois de devolução ou cancelamento: vai para a próxima reserva sem exemplar, ou fica livre. */
    private void destinarExemplar(Exemplar exemplar) {
        Optional<Reserva> proxima = fila(exemplar.getCodigoDoItem()).proximaSemExemplar();
        if (proxima.isPresent()) {
            exemplar.separarPara(proxima.get());
        } else {
            exemplar.liberar();
        }
    }

    @Override
    public String emprestar(String tombo, String matricula, LocalDate data)
            throws RecursoNaoEncontradoException, ExemplarIndisponivelException,
            UsuarioInativoException, LimiteDeEmprestimosExcedidoException {

        if (tombo == null || tombo.isBlank() || matricula == null
                || matricula.isBlank() || data == null) {
            throw new DadosInvalidosException("Tombo, matrícula e data são obrigatórios.");
        }

        Exemplar exemplar = acervo.localizarExemplar(tombo);
        if (exemplar == null) {
            throw new RecursoNaoEncontradoException("Exemplar não encontrado: " + tombo);
        }
        UsuarioView usuario = usuarios.buscarUsuario(matricula);

        if (!usuario.ativo()) {
            throw new UsuarioInativoException("Usuário inativo: " + matricula);
        }

        if (!exemplar.podeSerEmprestadoPara(matricula)) {
            throw new ExemplarIndisponivelException("Exemplar indisponível: " + tombo);
        }

        if (contarAtivos(matricula) >= LIMITE_DE_EMPRESTIMOS) {
            throw new LimiteDeEmprestimosExcedidoException(
                    "Usuário excedeu o limite de " + LIMITE_DE_EMPRESTIMOS + " empréstimos.");
        }

        Reserva atendida = exemplar.getReservaDona();
        if (atendida != null) {
            fila(exemplar.getCodigoDoItem()).remover(atendida);
        }
        exemplar.marcarEmprestado();

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
        destinarExemplar(exemplar);
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

        if (codigoDoItem == null || codigoDoItem.isBlank() || matricula == null
                || matricula.isBlank() || data == null) {
            throw new DadosInvalidosException("Item, matrícula e data são obrigatórios.");
        }
        Item item = acervo.localizarItem(codigoDoItem);
        if (item == null) {
            throw new RecursoNaoEncontradoException("Item não encontrado: " + codigoDoItem);
        }
        UsuarioView usuario = usuarios.buscarUsuario(matricula);
        if (!usuario.ativo()) {
            throw new UsuarioInativoException("Usuário inativo: " + matricula);
        }
        if (item.temExemplarDisponivel()) {
            throw new OperacaoNaoPermitidaException(
                    "Há exemplar disponível: empreste em vez de reservar.");
        }
        FilaDeReservas fila = fila(codigoDoItem);
        if (fila.contem(matricula)) {
            throw new OperacaoNaoPermitidaException("O usuário já reservou este item.");
        }

        String id = UUID.randomUUID().toString();
        fila.entrar(new Reserva(id, codigoDoItem, matricula, data));
        return id;
    }

    @Override
    public List<ReservaView> listarReservas(String codigoDoItem)
            throws RecursoNaoEncontradoException {

        if (codigoDoItem == null || codigoDoItem.isBlank()) {
            throw new DadosInvalidosException("O código do item é obrigatório.");
        }
        if (acervo.localizarItem(codigoDoItem) == null) {
            throw new RecursoNaoEncontradoException("Item não encontrado: " + codigoDoItem);
        }

        List<Reserva> naFila = fila(codigoDoItem).todas();
        List<ReservaView> resultado = new ArrayList<>();
        for (int i = 0; i < naFila.size(); i++) {
            resultado.add(paraView(naFila.get(i), i + 1));
        }
        return resultado;
    }

    @Override
    public void cancelarReserva(String idDaReserva) throws RecursoNaoEncontradoException {
        if (idDaReserva == null || idDaReserva.isBlank()) {
            throw new DadosInvalidosException("O id da reserva é obrigatório.");
        }
        final String id = idDaReserva;

        Reserva reserva = null;
        for (FilaDeReservas f : filas.values()) {
            Optional<Reserva> achada = f.buscarPorId(id);
            if (achada.isPresent()) {
                reserva = achada.get();
                break;
            }
        }
        if (reserva == null) {
            throw new RecursoNaoEncontradoException("Reserva não encontrada: " + id);
        }
        Exemplar separado = reserva.getExemplarSeparado();
        fila(reserva.getCodigoDoItem()).remover(reserva);
        if (separado != null) {
            destinarExemplar(separado);
        }
    }

    private ReservaView paraView(Reserva r, int posicao) {
        return new ReservaView(r.getId(), r.getCodigoDoItem(),
                r.getMatricula(), r.getData(), posicao);
    }

    private static class RegistroEmprestimo implements Serializable {
        private final String id;
        private final String tombo;
        private final String matricula;
        private final LocalDate dataEmprestimo;
        private final LocalDate dataPrevista;
        private LocalDate dataDevolucaoReal;

        RegistroEmprestimo(String id, String tombo, String matricula,
                           LocalDate dataEmprestimo, LocalDate dataPrevista) {
            this.id = id;
            this.tombo = tombo;
            this.matricula = matricula;
            this.dataEmprestimo = dataEmprestimo;
            this.dataPrevista = dataPrevista;
        }

        String getTombo() { return tombo; }
        String getMatricula() { return matricula; }
        LocalDate getDataEmprestimo() { return dataEmprestimo; }
        void setDataDevolucaoReal(LocalDate data) { this.dataDevolucaoReal = data; }

        boolean estaAtivo() { return dataDevolucaoReal == null; }

        boolean estaAtrasadoEm(LocalDate data) {
            return estaAtivo() && data.isAfter(dataPrevista);
        }

        EmprestimoView toView(LocalDate dataReferencia) {
            StatusEmprestimo status = estaAtrasadoEm(dataReferencia) ? StatusEmprestimo.ATRASADO
                    : estaAtivo() ? StatusEmprestimo.ATIVO
                      : StatusEmprestimo.DEVOLVIDO;
            return new EmprestimoView(id, tombo, matricula, dataEmprestimo,
                    dataPrevista, dataDevolucaoReal, status);
        }
    }
}