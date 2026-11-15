package br.ufpb.dcx.poo.biblioteca.filme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioService;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

/**
 * Implementação inicial e parcial dos usuários.
 *
 * <p>Não existe classe de domínio para o usuário: os dados estão soltos em listas
 * paralelas. É proposital. Uma das primeiras decisões da Entrega 1 é definir se
 * isso deve continuar assim.</p>
 */

public class Usuario implements UsuarioService, Serializable {

    private static class DadosUsuario implements Serializable {
        private final String matricula;
        private final String nome;
        private boolean ativo = true;

        DadosUsuario(String matricula, String nome) {
            this.matricula = matricula;
            this.nome = nome;
        }
    }

    private final Map<String, DadosUsuario> usuariosPorMatricula = new HashMap<>();
    private Emprestimo emprestimos;

    public Usuario() { }

    public void setEmprestimos(Emprestimo emprestimos) {
        this.emprestimos = emprestimos;
    }

    private int ativosDe(String matricula) {
        return emprestimos == null ? 0 : emprestimos.contarAtivos(matricula);
    }

    private UsuarioView paraView(DadosUsuario d) {
        return new UsuarioView(d.matricula, d.nome, d.ativo, ativosDe(d.matricula));
    }

    private DadosUsuario localizar(String matricula) throws RecursoNaoEncontradoException {
        if (matricula == null || matricula.isBlank()) {
            throw new DadosInvalidosException("A matrícula é obrigatória.");
        }
        DadosUsuario dados = usuariosPorMatricula.get(matricula);
        if (dados == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado: " + matricula);
        }
        return dados;
    }

    @Override
    public void cadastrarUsuario(String matricula, String nome)
            throws RecursoDuplicadoException {
        if (matricula == null || matricula.isBlank()) {
            throw new DadosInvalidosException("A matrícula é obrigatória.");
        }
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome é obrigatório.");
        }
        if (usuariosPorMatricula.containsKey(matricula)) {
            throw new RecursoDuplicadoException("Já existe usuário com a matrícula " + matricula);
        }
        usuariosPorMatricula.put(matricula, new DadosUsuario(matricula, nome));
    }

    @Override
    public UsuarioView buscarUsuario(String matricula) throws RecursoNaoEncontradoException {
        return paraView(localizar(matricula));
    }

    @Override
    public List<UsuarioView> listarUsuarios() {
        return usuariosPorMatricula.values().stream()
                .map(this::paraView)
                .sorted((a, b) -> a.nome().compareToIgnoreCase(b.nome()))
                .toList();
    }

    @Override
    public void desativarUsuario(String matricula)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        DadosUsuario dados = localizar(matricula);
        if (!dados.ativo) {
            return; // idempotente
        }
        if (ativosDe(matricula) >= 1) {
            throw new OperacaoNaoPermitidaException(
                    "Não é possível desativar um usuário com empréstimo ativo.");
        }
        dados.ativo = false;
    }

    @Override
    public void reativarUsuario(String matricula) throws RecursoNaoEncontradoException {
        localizar(matricula).ativo = true; // idempotente
    }
}