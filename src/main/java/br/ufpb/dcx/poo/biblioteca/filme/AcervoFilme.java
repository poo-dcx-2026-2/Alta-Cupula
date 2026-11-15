package br.ufpb.dcx.poo.biblioteca.filme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.text.Collator;
import java.util.Locale;

import br.ufpb.dcx.poo.biblioteca.contrato.AcervoService;
import br.ufpb.dcx.poo.biblioteca.contrato.ExemplarView;
import br.ufpb.dcx.poo.biblioteca.contrato.ItemView;
import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.OperacaoNaoPermitidaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

public class AcervoFilme implements AcervoService, Serializable {

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
        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);

        resultado.sort((a, b) -> collator.compare(a.titulo(), b.titulo()));
        return resultado;
    }

    @Override
    public List<ItemView> buscarPorTitulo(String trecho) {
        exigirTextoPreenchido(trecho, "trecho do título");
        String busca = trecho.toLowerCase();
        return buscarOrdenado(item -> item.getTitulo().toLowerCase().contains(busca));
    }

    @Override
    public List<ItemView> buscarPorCategoria(String categoria) {
        exigirTextoPreenchido(categoria, "categoria");
        return buscarOrdenado(item -> item.getCategoria() != null
                && item.getCategoria().equalsIgnoreCase(categoria));
    }

    private List<ItemView> buscarOrdenado(Predicate<Item> filtro) {
        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);
        return itens.stream()
                .filter(filtro)
                .map(this::paraView)
                .sorted((a, b) -> collator.compare(a.titulo(), b.titulo()))
                .collect(Collectors.toList());
    }
    @Override
    public void adicionarExemplar(String codigoDoItem, String tombo)
            throws RecursoNaoEncontradoException, RecursoDuplicadoException {

        exigirTextoPreenchido(codigoDoItem, "codigo do item");
        exigirTextoPreenchido(tombo, "tombo");

        Item item = localizar(codigoDoItem);
        if (item == null) {
            throw new RecursoNaoEncontradoException("Item não encontrado: " + codigoDoItem);
        }
        if (localizarExemplar(tombo) != null) {
            throw new RecursoDuplicadoException("Já existe um exemplar com o tombo " + tombo);
        }

        item.adicionarExemplar(new Exemplar(tombo, StatusExemplar.DISPONIVEL, codigoDoItem));

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
        resultado.sort(Comparator.comparingInt(exemplar -> {
            String tombo = exemplar.tombo();
            if (tombo == null) return 0;
            String ordem = tombo.replaceAll("\\D+", "");
            return ordem.isEmpty() ? 0 : Integer.parseInt(ordem);
        }));

        return resultado;
    }

    @Override
    public void baixarExemplar(String tombo)
            throws RecursoNaoEncontradoException, OperacaoNaoPermitidaException {
        if (tombo == null || tombo.trim().isEmpty()) {
            throw new RecursoNaoEncontradoException("Tombo inválido.");
        }

        Exemplar exemplar = localizarExemplar(tombo);
        if (exemplar == null) {
            throw new RecursoNaoEncontradoException("Exemplar com tombo " + tombo + " não encontrado.");
        }

        exemplar.marcarIndisponivel();
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

    Item localizarItem(String codigo) {
        return localizar(codigo);
    }

    Exemplar localizarExemplar(String tombo) {
        for (Item item : itens) {
            for (Exemplar e : item.getExemplares()) {
                if (e.getTombo().equals(tombo)) {
                    return e;
                }
            }
        }
        return null;
    }


    List<Item> itens() {
        return itens;
    }

}
