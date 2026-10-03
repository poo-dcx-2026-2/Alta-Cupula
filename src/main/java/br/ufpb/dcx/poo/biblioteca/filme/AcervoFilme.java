package br.ufpb.dcx.poo.biblioteca.filme;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import br.ufpb.dcx.poo.biblioteca.contrato.AcervoService;
import br.ufpb.dcx.poo.biblioteca.contrato.ExemplarView;
import br.ufpb.dcx.poo.biblioteca.contrato.ItemView;
import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

public class AcervoFilme implements AcervoService {

    private final List<Item> itens = new ArrayList<>();

    @Override
    public void cadastrarItem(String codigo, String titulo, String autoria,
                              String categoria, int ano)
            throws RecursoDuplicadoException {

        exigirTextoPreenchido(codigo, "codigo");
        exigirTextoPreenchido(titulo, "titulo");

        if (localizar(codigo) != null) {
            throw new RecursoDuplicadoException("Já existe item com o código " + codigo);
        }
        itens.add(new Item(codigo, titulo, autoria, categoria, ano));
    }

    @Override
    public ItemView buscarItem(String codigo) throws RecursoNaoEncontradoException {
        Item item = localizar(codigo);
        if (item == null) {
            throw new RecursoNaoEncontradoException("Item não encontrado: " + codigo);
        }
        return paraView(item);
    }

    @Override
    public List<ItemView> listarItens() {
        List<ItemView> resultado = new ArrayList<>();
        for (Item item : itens) {
            resultado.add(paraView(item));
        }
        resultado.sort((a, b) -> a.titulo().compareToIgnoreCase(b.titulo()));
        return resultado;
    }

    @Override
    public List<ItemView> buscarPorTitulo(String trecho) {
        if (trecho == null || trecho.isBlank()) {
            return List.of();
        }
        return itens.stream()
                .filter(item -> item.getTitulo().toLowerCase().contains(trecho.toLowerCase()))
                .map(this::paraView)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemView> buscarPorCategoria(String categoria) {
        throw new UnsupportedOperationException("Entrega 2: implementar buscarPorCategoria");
    }

    @Override
    public void adicionarExemplar(String codigoDoItem, String tombo)
            throws RecursoNaoEncontradoException, RecursoDuplicadoException {

        exigirTextoPreenchido(codigoDoItem, "codigo do item");
        exigirTextoPreenchido(tombo, "tombo");

        for (Item itemExistente : itens){
            for (Exemplar exp : itemExistente.getExemplares()){
                if (exp.getTombo().equals(tombo)){
                    throw new RecursoDuplicadoException("Já existe um exemplar com o tombo"+ tombo);

                }
            }
        }
        Item item = localizar(codigoDoItem);
        if (item == null){
            throw new RecursoNaoEncontradoException("Item não encontrado:" + codigoDoItem);
        }
        item.getExemplares().add(new Exemplar(tombo, StatusExemplar.DISPONIVEL, codigoDoItem));
    }

    @Override
    public List<ExemplarView> listarExemplares(String codigoDoItem)
            throws RecursoNaoEncontradoException {
        Item item = localizar(codigoDoItem);
        if (item == null ){
            throw new RecursoNaoEncontradoException("Item não encontrado: " + codigoDoItem);
        }
        List<ExemplarView> resultado = new ArrayList<>();
        for (Exemplar exemplar : item.getExemplares()){
            resultado.add(new ExemplarView(exemplar.getTombo(), codigoDoItem, exemplar.getStatus()));
        }
        return resultado;
    }

    @Override
    public void baixarExemplar(String tombo)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        throw new UnsupportedOperationException("Entrega 2: implementar baixarExemplar");
    }

    private Item localizar(String codigo) {
        for (Item item : itens) {
            if (item.getCodigo().equals(codigo)) {
                return item;
            }
        }
        return null;
    }

    private ItemView paraView(Item item) {
        int disponiveis = 0;
        for (Exemplar exemplar : item.getExemplares()) {
            if (exemplar.getStatus() == StatusExemplar.DISPONIVEL) {
                disponiveis++;
            }
        }
        return new ItemView(
                item.getCodigo(),
                item.getTitulo(),
                item.getAutoria(),
                item.getCategoria(),
                item.getAno(),
                item.getExemplares().size(),
                disponiveis);
    }

    private static void exigirTextoPreenchido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException("O campo " + campo + " é obrigatório.");
        }
    }

    /** Acesso interno usado pelos demais serviços da implementação inicial. */
    List<Item> itens() {
        return itens;
    }

}
