package br.ufpb.dcx.poo.biblioteca.filme;

import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;
import br.ufpb.dcx.poo.biblioteca.filme.Item;

public class Exemplar {

    private String tombo;
    private br.ufpb.dcx.poo.biblioteca.filme.Item item;
    private StatusExemplar status;

    public Exemplar(String tombo, br.ufpb.dcx.poo.biblioteca.filme.Item item) {
        this.tombo = tombo;
        this.item = item;
        this.status = StatusExemplar.DISPONIVEL;
    }

    public String getTombo() { return tombo; }
    public void setTombo(String tombo) { this.tombo = tombo; }

    public br.ufpb.dcx.poo.biblioteca.filme.Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public StatusExemplar getStatus() { return status; }
    public void setStatus(StatusExemplar status) { this.status = status; }
}
