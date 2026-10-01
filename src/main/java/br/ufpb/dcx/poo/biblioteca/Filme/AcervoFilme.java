package br.ufpb.dcx.poo.biblioteca.Filme;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.PersistenciaException;

public class AcervoFilme implements Biblioteca {
    @Override
    public AcervoService acervo() {
        return null;
    }

    @Override
    public UsuarioService usuarios() {
        return null;
    }

    @Override
    public EmprestimoService emprestimos() {
        return null;
    }

    @Override
    public RelatorioService relatorios() {
        return null;
    }

    @Override
    public void salvar() throws PersistenciaException {

    }

    @Override
    public void carregar() throws PersistenciaException {

    }
}
