package br.ufpb.dcx.poo.biblioteca.filme;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.ufpb.dcx.poo.biblioteca.contrato.ItemView;
import br.ufpb.dcx.poo.biblioteca.contrato.RelatorioService;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioService;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioView;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.PersistenciaException;

public class Relatorios implements RelatorioService, Serializable {

    private final AcervoFilme acervo;
    private final Emprestimo emprestimos;
    private final UsuarioService usuarios;

    public Relatorios(AcervoFilme acervo, Emprestimo emprestimos, UsuarioService usuarios) {
        this.acervo = acervo;
        this.emprestimos = emprestimos;
        this.usuarios = usuarios;
    }


    @Override
    public List<ItemView> itensMaisEmprestados(int n) {
        throw new UnsupportedOperationException("Entrega 3: implementar itensMaisEmprestados");
    }

    @Override
    public Map<String, Long> emprestimosPorCategoria() {
        throw new UnsupportedOperationException("Entrega 3: implementar emprestimosPorCategoria");
    }

    @Override
    public List<UsuarioView> usuariosComAtraso(LocalDate data) {
        throw new UnsupportedOperationException("Entrega 3: implementar usuariosComAtraso");
    }

    @Override
    public double taxaDeOcupacaoDoAcervo() {
        throw new UnsupportedOperationException("Entrega 3: implementar taxaDeOcupacaoDoAcervo");
    }

    @Override
    public int importarItensEmLote(Path arquivo) throws PersistenciaException {
        throw new UnsupportedOperationException("Entrega 3: implementar importarItensEmLote");
    }
}