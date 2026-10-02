package br.ufpb.dcx.poo.biblioteca.filme;

import br.ufpb.dcx.poo.biblioteca.contrato.AcervoService;
import br.ufpb.dcx.poo.biblioteca.contrato.Biblioteca;
import br.ufpb.dcx.poo.biblioteca.contrato.EmprestimoService;
import br.ufpb.dcx.poo.biblioteca.contrato.RelatorioService;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioService;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.PersistenciaException;

public class Cineclube implements Biblioteca {

    private final AcervoFilme acervo = new AcervoFilme();
    private final UsuariosEmMemoria usuarios = new UsuariosEmMemoria();
    private final EmprestimosNaoImplementados emprestimos = new EmprestimosNaoImplementados();
    private final RelatoriosNaoImplementados relatorios = new RelatoriosNaoImplementados();

    @Override
    public AcervoService acervo() { return acervo; }

    @Override
    public UsuarioService usuarios() { return usuarios; }

    @Override
    public EmprestimoService emprestimos() { return emprestimos; }

    @Override
    public RelatorioService relatorios() { return relatorios; }

    @Override
    public void salvar() throws PersistenciaException {
        throw new UnsupportedOperationException("Entrega 2: implementar salvar");
    }

    @Override
    public void carregar() throws PersistenciaException {
        throw new UnsupportedOperationException("Entrega 2: implementar carregar");
    }
}
