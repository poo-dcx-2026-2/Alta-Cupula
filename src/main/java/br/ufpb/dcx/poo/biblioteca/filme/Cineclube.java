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

    private  AcervoFilme acervo = new AcervoFilme();
    private  Usuario usuarios = new Usuario();
    private  Emprestimo emprestimos = new Emprestimo();
    private  Relatorios relatorios = new Relatorios();

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
        String diretorioDados = System.getProperty("biblioteca.dados", "dados");
        Path caminhoDir = Paths.get(diretorioDados);

        try {
            Files.createDirectories(caminhoDir);
            Path arquivo = caminhoDir.resolve("biblioteca.dat");

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(arquivo))) {
                oos.writeObject(this);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao salvar os dados da biblioteca: " + e.getMessage());
        }
    }

    @Override
    public void carregar() throws PersistenciaException {
        String diretorioDados = System.getProperty("biblioteca.dados", "dados");
        Path arquivo = Paths.get(diretorioDados, "biblioteca.dat");

        if (!Files.exists(arquivo)) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(arquivo))) {
            Cineclube bibliotecaCarregada = (Cineclube) ois.readObject();

            this.acervo = bibliotecaCarregada.acervo;
            this.usuarios = bibliotecaCarregada.usuarios;
            this.emprestimos = bibliotecaCarregada.emprestimos;
            this.relatorios = bibliotecaCarregada.relatorios;

        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Erro ao carregar os dados da biblioteca: " + e.getMessage());
        }
    }
}
