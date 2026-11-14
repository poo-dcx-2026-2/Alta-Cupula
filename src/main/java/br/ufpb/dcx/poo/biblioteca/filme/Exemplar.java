package br.ufpb.dcx.poo.biblioteca.filme;

import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;

import java.io.Serializable;

public class Exemplar implements Serializable {

    private final String tombo;
    private final String codigoDoItem;
    private StatusExemplar status;


    public Exemplar(String tombo, StatusExemplar status, String codigoDoItem) {
        if (tombo == null || tombo.trim().isEmpty()) {
            throw new IllegalArgumentException("O tombo do exemplar não pode ser vazio.");
        }
        if (codigoDoItem == null || codigoDoItem.trim().isEmpty()) {
            throw new IllegalArgumentException("O código do item não pode ser vazio.");
        }
        this.tombo = tombo;
        this.codigoDoItem = codigoDoItem;
        this.status = status != null ? status : StatusExemplar.DISPONIVEL;
    }

    public String getCodigoDoItem() {
        return codigoDoItem;
    }

    public String getTombo() {
        return tombo;
    }

    public StatusExemplar getStatus() {
        return status;
    }

    public void setStatus(StatusExemplar status) {
        this.status = status;
    }
}