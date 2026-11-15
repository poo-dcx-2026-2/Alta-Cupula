package br.ufpb.dcx.poo.biblioteca.filme;

import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;

import java.io.Serializable;

public class Exemplar implements Serializable {

    private final String tombo;
    private final String codigoDoItem;
    private StatusExemplar status;
    private Reserva reservaDona;


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


    public Reserva getReservaDona() { return reservaDona; }
    public boolean podeSerEmprestadoPara(String matricula) {
        if (status == StatusExemplar.DISPONIVEL) {
            return true;
        }
        return status == StatusExemplar.RESERVADO
                && reservaDona != null
                && reservaDona.getMatricula().equals(matricula);
    }

    public void separarPara(Reserva reserva) {
        this.status = StatusExemplar.RESERVADO;
        this.reservaDona = reserva;
        reserva.setExemplarSeparado(this);
    }

    public void liberar() {
        this.status = StatusExemplar.DISPONIVEL;
        this.reservaDona = null;
    }

    public void marcarEmprestado() {
        this.status = StatusExemplar.EMPRESTADO;
        this.reservaDona = null;
    }

    public void marcarIndisponivel() {
        if (reservaDona != null) {
            reservaDona.setExemplarSeparado(null);
        }
        this.status = StatusExemplar.INDISPONIVEL;
        this.reservaDona = null;
    }
}