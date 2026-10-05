package br.ufpb.dcx.poo.biblioteca;

import java.util.List;

import br.ufpb.dcx.poo.biblioteca.contrato.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.BibliotecaException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.DadosInvalidosException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoDuplicadoException;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.RecursoNaoEncontradoException;

import static org.junit.jupiter.api.Assertions.*;

class AcervoTest {

    private Biblioteca biblioteca;

    @BeforeEach
    void criarBibliotecaVazia() {
        biblioteca = Fabrica.novaBiblioteca();
    }

    @Test
    @DisplayName("um item cadastrado pode ser recuperado pelo código")
    void cadastrarEBuscar() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("L1", "Java Efetivo", "Bloch", "livro", 2019);

        ItemView item = biblioteca.acervo().buscarItem("L1");

        assertEquals("L1", item.codigo());
        assertEquals("Java Efetivo", item.titulo());
        assertEquals("Bloch", item.autoria());
        assertEquals(2019, item.ano());
    }

    @Test
    @DisplayName("item novo começa sem exemplares")
    void itemNovoNaoTemExemplares() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("L1", "Java Efetivo", "Bloch", "livro", 2019);

        ItemView item = biblioteca.acervo().buscarItem("L1");

        assertEquals(0, item.totalDeExemplares());
        assertEquals(0, item.exemplaresDisponiveis());
    }

    @Test
    @DisplayName("código repetido é recusado")
    void codigoDuplicado() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("L1", "Java Efetivo", "Bloch", "livro", 2019);

        assertThrows(RecursoDuplicadoException.class,
                () -> biblioteca.acervo().cadastrarItem("L1", "Outro", "Outra", "livro", 2020));
    }

    @Test
    @DisplayName("buscar item inexistente lança RecursoNaoEncontradoException")
    void buscarInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.acervo().buscarItem("NAO-EXISTE"));
    }

    @Test
    @DisplayName("código em branco é entrada inválida, não regra de negócio")
    void codigoEmBranco() {
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.acervo().cadastrarItem("  ", "Título", "Autoria", "livro", 2019));
    }

    @Test
    @DisplayName("listar devolve os itens ordenados por título")
    void listarOrdenado() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("F1", "Refatoração", "Fowler", "livro", 2004);
        biblioteca.acervo().cadastrarItem("F2", "Código limpo", "Martin", "livro", 2009);
        biblioteca.acervo().cadastrarItem("F3", "Java Efetivo", "Bloch", "livro", 2019);

        List<ItemView> itens = biblioteca.acervo().listarItens();

        assertEquals(3, itens.size());
        assertEquals("Código limpo", itens.get(0).titulo());
        assertEquals("Java Efetivo", itens.get(1).titulo());
        assertEquals("Refatoração", itens.get(2).titulo());
    }

    @Test
    @DisplayName("acervo vazio devolve lista vazia, não null")
    void acervoVazio() {
        assertEquals(List.of(), biblioteca.acervo().listarItens());
    }

    // ------------------------------------------------------------------
    // A implementar na Entrega 1. Remova @Disabled ao implementar cada um.
    // ------------------------------------------------------------------

    @Test
    @DisplayName("exemplar adicionado entra como DISPONIVEL e conta no item")
    void adicionarExemplar() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("L1", "Java Efetivo", "Bloch", "livro", 2019);
        biblioteca.acervo().adicionarExemplar("L1", "T-001");
        biblioteca.acervo().adicionarExemplar("L1", "T-002");

        ItemView item = biblioteca.acervo().buscarItem("L1");
        assertEquals(2, item.totalDeExemplares());
        assertEquals(2, item.exemplaresDisponiveis());

        assertEquals(StatusExemplar.DISPONIVEL,
                biblioteca.acervo().listarExemplares("L1").get(0).status());
    }

    @Test
    @DisplayName("tombo é único no acervo inteiro, não apenas dentro do item")
    void tomboDuplicadoEntreItensDiferentes() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("L1", "Java Efetivo", "Bloch", "livro", 2019);
        biblioteca.acervo().cadastrarItem("L2", "Refatoração", "Fowler", "livro", 2004);
        biblioteca.acervo().adicionarExemplar("L1", "T-001");

        assertThrows(RecursoDuplicadoException.class,
                () -> biblioteca.acervo().adicionarExemplar("L2", "T-001"));
    }

    @Test
    @DisplayName("não se adiciona exemplar a item que não existe")
    void exemplarDeItemInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> biblioteca.acervo().adicionarExemplar("NAO-EXISTE", "T-001"));
    }

    @Test
    @DisplayName("busca por título ignora maiúsculas e aceita trecho")
    void buscarPorTitulo() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("L1", "Java Efetivo", "Bloch", "livro", 2019);
        biblioteca.acervo().cadastrarItem("L2", "Refatoração", "Fowler", "livro", 2004);

        assertEquals(1, biblioteca.acervo().buscarPorTitulo("efetivo").size());
        assertEquals(1, biblioteca.acervo().buscarPorTitulo("JAVA").size());
    }

    @Test
    @DisplayName("busca sem resultado devolve lista vazia, não exceção")
    void buscarPorTituloSemResultado() {
        assertEquals(List.of(), biblioteca.acervo().buscarPorTitulo("inexistente"));
    }

    @Test
    @DisplayName("não permite cadastrar exemplar com tombo nulo ou em branco")
    void adicionarExemplarComTomboInvalido() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("L1", "Java Efetivo", "Bloch", "livro", 2019);

        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.acervo().adicionarExemplar("L1", null));
        assertThrows(DadosInvalidosException.class,
                () -> biblioteca.acervo().adicionarExemplar("L1", "   "));
    }

    @Test
    @DisplayName("passo 22: ordenação de itens deve considerar regras e acentuação do português")
    void ordenacaoComCaracteresEspeciaisEAcentos() throws BibliotecaException {
        biblioteca.acervo().cadastrarItem("L1", "Ação", "Autor1", "Ação", 2020);
        biblioteca.acervo().cadastrarItem("L2", "Abacaxi", "Autor2", "Comedia", 2021);
        biblioteca.acervo().cadastrarItem("L3", "Zebra", "Autor3", "Aventura", 2019);

        List<ItemView> itens = biblioteca.acervo().listarItens();

        assertEquals("Abacaxi", itens.get(0).titulo());
        assertEquals("Ação", itens.get(1).titulo());
        assertEquals("Zebra", itens.get(2).titulo());
    }

    @Test
    @DisplayName("busca por trecho nulo deve retornar lista vazia")
    void buscarPorTituloNuloeBranco() {
        assertTrue(biblioteca.acervo().buscarPorTitulo("   ").isEmpty());
        assertTrue(biblioteca.acervo().buscarPorTitulo(null).isEmpty());
    }

    @Test
    @DisplayName("listar exemplares de um item inexistente lança RecursoNaoEncontradoException")
    void listarExemplaresDeItemInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () ->
                biblioteca.acervo().listarExemplares("CODIGO-INEXISTENTE")
        );
    }

    @Test
    @DisplayName("matricula Repetida é recusado")
    void matriculaDuplicada() throws BibliotecaException {
        biblioteca.usuarios().cadastrarUsuario("M1", "Alan");

        assertThrows(RecursoDuplicadoException.class,
                () -> biblioteca.usuarios().cadastrarUsuario("M1", "Ryan"));
    }

    @Test
    @DisplayName("passo 24: duas bibliotecas criadas pela fábrica devem ser independentes")
    void fabricaSempreCriaInstanciasIndependentes() throws BibliotecaException {
        Biblioteca bibliotecaA = Fabrica.novaBiblioteca();
        Biblioteca bibliotecaB = Fabrica.novaBiblioteca();

        bibliotecaA.acervo().cadastrarItem("L1", "Java Efetivo", "Bloch", "livro", 2019);

        assertEquals(1, bibliotecaA.acervo().listarItens().size());
        assertTrue(bibliotecaB.acervo().listarItens().isEmpty());
    }

}


