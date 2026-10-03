package br.ufpb.dcx.poo.biblioteca.filme;

import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;

public class Exemplar {

    private String tombo;
    private StatusExemplar item;
    private StatusExemplar status;

    public Exemplar(String tombo, StatusExemplar item) {
        this.tombo = tombo;
        this.item = item;
        this.status = StatusExemplar.DISPONIVEL;
    }

    public String getTombo() { return tombo; }
    public void setTombo(String tombo) { this.tombo = tombo; }

    public StatusExemplar getItem() { return item; }
    public void setItem(StatusExemplar item) { this.item = item; }

    public StatusExemplar getStatus() { return status; }
    public void setStatus(StatusExemplar status) { this.status = status; }
}
