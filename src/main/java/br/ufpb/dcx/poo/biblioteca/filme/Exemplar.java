package br.ufpb.dcx.poo.biblioteca.filme;

import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;

public class Exemplar {

    private final String tombo;
    private final String codigoDoItem;
    private StatusExemplar item;
    private StatusExemplar status;
    
    public Exemplar(String tombo, StatusExemplar item, String codigoDoItem) {
        this.tombo = tombo;
        this.item = item;
        this.codigoDoItem = codigoDoItem;
        this.status = StatusExemplar.DISPONIVEL;
    }

    public String getcodigoDoItem() {return codigoDoItem;}
    public String getTombo() { return tombo; }

    public StatusExemplar getItem() { return item; }
    public void setItem(StatusExemplar item) { this.item = item; }
    public StatusExemplar getStatus() { return status; }
    public void setStatus(StatusExemplar status) { this.status = status; }
}
