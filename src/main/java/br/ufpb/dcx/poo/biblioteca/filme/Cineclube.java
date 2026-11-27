package br.ufpb.dcx.poo.biblioteca.filme;

import br.ufpb.dcx.poo.biblioteca.contrato.AcervoService;
import br.ufpb.dcx.poo.biblioteca.contrato.Biblioteca;
import br.ufpb.dcx.poo.biblioteca.contrato.EmprestimoService;
import br.ufpb.dcx.poo.biblioteca.contrato.RelatorioService;
import br.ufpb.dcx.poo.biblioteca.contrato.UsuarioService;
import br.ufpb.dcx.poo.biblioteca.contrato.excecoes.PersistenciaException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Cineclube implements Biblioteca, Serializable {

    private AcervoFilme acervo = new AcervoFilme();
    private UsuariosEmMemoria usuarios = new UsuariosEmMemoria();
    private Emprestimo emprestimos;
    private Relatorios relatorios;

    public Cineclube() {
            this.emprestimos = new Emprestimo(acervo, usuarios);
            this.usuarios.setEmprestimos(this.emprestimos);
            this.relatorios = new Relatorios(acervo, emprestimos, usuarios);

    }
    public MultaService multas() { return new MultaService(emprestimos); }

    private static PersistenciaException falha(String mensagem, Exception causa) {
        PersistenciaException excecao = new PersistenciaException(mensagem);
        excecao.initCause(causa);
        return excecao;
    }

    @Override
    public AcervoService acervo() { return acervo; }

    @Override
    public UsuarioService usuarios() { return usuarios; }

    @Override
    public EmprestimoService emprestimos() { return emprestimos; }

    @Override
    public RelatorioService relatorios() { return relatorios; }

    @Override
    public void salvar() throws PersistenciaException {
        Path diretorio = Paths.get(System.getProperty("biblioteca.dados", "dados"));
        try {
            Files.createDirectories(diretorio);
            Path arquivo = diretorio.resolve("biblioteca.dat");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(arquivo))) {
                oos.writeObject(this);
            }
        } catch (IOException e) {
            throw falha("Erro ao salvar os dados da biblioteca", e);
        }
    }

    @Override
    public void carregar() throws PersistenciaException {
        Path arquivo = Paths.get(System.getProperty("biblioteca.dados", "dados"), "biblioteca.dat");
        if (Files.notExists(arquivo)) {
            return;
        }
        Cineclube lida;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(arquivo))) {
            lida = (Cineclube) ois.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw falha("Erro ao carregar os dados da biblioteca", e);
        }
        this.acervo = lida.acervo;
        this.usuarios = lida.usuarios;
        this.emprestimos = lida.emprestimos;
        this.relatorios = lida.relatorios;
    }

}
