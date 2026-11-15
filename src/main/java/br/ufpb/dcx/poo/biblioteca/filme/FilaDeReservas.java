package br.ufpb.dcx.poo.biblioteca.filme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FilaDeReservas implements Serializable {

    private final List<Reserva> reservas = new ArrayList<>();

    public void entrar(Reserva reserva) {
        reservas.add(reserva);
    }

    public boolean contem(String matricula) {
        return reservas.stream().anyMatch(r -> r.getMatricula().equals(matricula));
    }

    public Optional<Reserva> proximaSemExemplar() {
        return reservas.stream().filter(r -> !r.temExemplar()).findFirst();
    }

    public Optional<Reserva> buscarPorId(String id) {
        return reservas.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public void remover(Reserva reserva) {
        reservas.remove(reserva);
    }

    public List<Reserva> todas() {
        return List.copyOf(reservas);
    }
}