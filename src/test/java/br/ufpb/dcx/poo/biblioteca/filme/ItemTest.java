package br.ufpb.dcx.poo.biblioteca.filme;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import java.util.List;
import br.ufpb.dcx.poo.biblioteca.contrato.StatusExemplar;

public class ItemTest {

    @Test
    void naoDevePermitirModificarListaDeExemplaresDiretamente() {
        Item item = new Item("001", "Filme Teste", "Autor", "Ação", 2023);

        List<Exemplar> listaRetornada = item.getExemplares();

        assertThrows(UnsupportedOperationException.class, () -> {
            listaRetornada.add(new Exemplar("T01", StatusExemplar.DISPONIVEL, "001"));
        });
    }
}