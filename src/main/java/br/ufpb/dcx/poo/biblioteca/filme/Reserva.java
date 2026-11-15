package br.ufpb.dcx.poo.biblioteca.filme;

import java.io.Serializable;
import java.time.LocalDate;

public class Reserva implements Serializable {

    private final String id;
    private final String codigoDoItem;
    private final String matricula;
    private final LocalDate data;
    private Exemplar exemplarSeparado;

    public Reserva(String id, String codigoDoItem, String matricula, LocalDate data) {
        this.id = id;
        this.codigoDoItem = codigoDoItem;
        this.matricula = matricula;
        this.data = data;
    }

    public String getId() { return id; }
    public String getCodigoDoItem() { return codigoDoItem; }
    public String getMatricula() { return matricula; }
    public LocalDate getData() { return data; }
    public Exemplar getExemplarSeparado() { return exemplarSeparado; }
    public boolean temExemplar() { return exemplarSeparado != null; }

    void setExemplarSeparado(Exemplar exemplar) { this.exemplarSeparado = exemplar; }
}