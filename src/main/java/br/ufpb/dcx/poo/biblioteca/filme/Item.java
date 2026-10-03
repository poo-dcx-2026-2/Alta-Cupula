package br.ufpb.dcx.poo.biblioteca.filme;

import java.util.ArrayList;
import java.util.List;

public class Item {

    private final String codigo;
    private final String titulo;
    private final String autoria;
    private final String categoria;
    private final int ano;
    private final List<Exemplar> exemplares = new ArrayList<>();

    public Item(String codigo, String titulo, String autoria, String categoria, int ano) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.autoria = autoria;
        this.categoria = categoria;
        this.ano = ano;
    }

    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getAutoria() { return autoria; }
    public String getCategoria() { return categoria; }
    public int getAno() { return ano; }
    public List<Exemplar> getExemplares() { return exemplares; }

}
