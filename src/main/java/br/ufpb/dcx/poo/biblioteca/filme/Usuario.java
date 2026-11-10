package br.ufpb.dcx.poo.biblioteca.filme;

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
public class Usuario implements UsuarioService {

    private final Map<String, UsuarioView> usuariosPorMatricula = new HashMap<>();

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
        usuariosPorMatricula.put(matricula, new UsuarioView(matricula, nome, true, 0));
    }

    @Override
    public UsuarioView buscarUsuario(String matricula) throws RecursoNaoEncontradoException {
        UsuarioView usuario = usuariosPorMatricula.get(matricula);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado: " + matricula);
        }
        return usuario;
    }

    @Override
    public List<UsuarioView> listarUsuarios() {
        List<UsuarioView> resultado = new ArrayList<>(usuariosPorMatricula.values());
        resultado.sort((a, b) -> a.nome().compareToIgnoreCase(b.nome()));
        return resultado;
    }

    @Override
    public void desativarUsuario(String matricula)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        throw new UnsupportedOperationException("Entrega 2: implementar desativarUsuario");
    }

    @Override
    public void reativarUsuario(String matricula) throws RecursoNaoEncontradoException {
        throw new UnsupportedOperationException("Entrega 2: implementar reativarUsuario");
    }
}
