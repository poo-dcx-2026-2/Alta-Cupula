# Sistema de Gestão de Biblioteca/Acervo

Projeto incremental da disciplina de **Programação Orientada a Objetos** — DCX/CCAE/UFPB.

Este repositório é o ponto de partida do seu projeto. Ele compila, executa e tem testes passando, mas está **deliberadamente incompleto** e contém decisões de design questionáveis. Encontrá-las, justificá-las e corrigi-las faz parte da disciplina.

---

## Começando

Você precisa de **JDK 21** e **Maven**. No IntelliJ IDEA, abra a pasta do projeto e ele reconhece o `pom.xml` sozinho.

```bash
mvn -B verify      # compila e roda os testes
mvn -B test        # só os testes
```

Se tudo estiver certo, você verá `Tests run: 19, Failures: 0, Errors: 0, Skipped: 5`.

Os 5 pulados são os testes marcados com `@Disabled`: eles cobrem o que você ainda vai implementar. Remova a anotação de cada um quando implementar o método correspondente.

---

## Mapa do projeto

```
src/main/java/br/ufpb/dcx/poo/biblioteca/
├── contrato/          ← CONGELADO. Não edite nada aqui.
│   ├── Biblioteca.java              ponto único de acesso aos serviços
│   ├── AcervoService.java           itens e exemplares
│   ├── UsuarioService.java          usuários
│   ├── EmprestimoService.java       empréstimos, devoluções e reservas
│   ├── RelatorioService.java        consultas e importação em lote
│   ├── *View.java                   o que as consultas devolvem
│   ├── Status*.java                 estados de exemplar e de empréstimo
│   └── excecoes/                    a hierarquia de erros do sistema
│
├── Fabrica.java       ← devolve new Cineclube()
│
└── filme/             ← implementação da equipe
    ├── Cineclube.java        Facade (implementa Biblioteca, salvar/carregar)
    ├── AcervoFilme.java      itens e exemplares
    ├── Item.java / Exemplar.java
    ├── Usuario.java          serviço de usuários
    ├── Emprestimo.java       empréstimos e reservas
    ├── Reserva.java / FilaDeReservas.java
    ├── MultaService.java     extensão autoral
    └── Relatorios.java       Entrega 3

src/test/java/…       ← seus testes; comece pelos que já estão aqui
dados/                ← arquivos de exemplo
.github/workflows/    ← a integração contínua, já configurada
```

---

## As duas regras

**1. O pacote `contrato` é congelado.** Não renomeie, não altere assinaturas, não acrescente nem remova métodos. Os testes de correção são escritos contra esses tipos: se você mudar qualquer coisa ali, eles não compilam e a entrega não pode ser avaliada.

**2. `Fabrica.novaBiblioteca()` precisa continuar funcionando.** É por esse método que os testes obtêm o seu sistema. Você vai trocar o que ele devolve — não troque o nome, o pacote nem a assinatura. Cada chamada precisa devolver uma instância nova e independente.

Fora isso, **tudo é seu**. O pacote `inicial` não é modelo: é matéria-prima. Você pode reescrevê-lo inteiro.

---

## O que já funciona e o que falta

| Serviço | Situação |
|---|---|
| `AcervoService` | `cadastrarItem`, `buscarItem` e `listarItens` funcionam para os casos simples. O resto é seu. |
| `UsuarioService` | `cadastrarUsuario`, `buscarUsuario` e `listarUsuarios` funcionam. `desativar`/`reativar` são da Entrega 2. |
| `EmprestimoService` | Esqueleto. Entrega 2. |
| `RelatorioService` | Esqueleto. Entrega 3. |
| `Biblioteca.salvar/carregar` | Esqueleto. Entrega 2. |

Métodos ainda não implementados lançam `UnsupportedOperationException` com a indicação da entrega. Cada mensagem diz o que fazer.

---

## Um aviso honesto

O código do pacote `inicial` **tem problemas**. Alguns são de design e você vai reconhecê-los conforme a disciplina avançar. Pelo menos um é um defeito de comportamento que os testes atuais **não** pegam: o sistema faz a coisa errada em uma situação que ninguém testou ainda.

Isso é proposital. Encontrar esse tipo de defeito — reproduzir, escrever o teste que falha, corrigir, ver o teste passar — é uma das competências avaliadas. Não confie em suíte verde como prova de correção.

---

## Extensão autoral: multa por atraso
- O que é: R$ 0,50 por dia de atraso, calculado a partir da data informada.
- Como usar: new Cineclube().multas().multaTotalDoUsuario(matricula, data)
- Onde está: filme/MultaService.java; testes em MultaTest.
- Observação: não altera o contrato; devolver continua devolvendo.

### Nossa extensão

**Acervo:** filmes de um cineclube (classes `Cineclube`, `AcervoFilme`).

**Regra de negócio própria: multa por atraso.**
- **O que é:** cada dia de atraso na devolução custa R$ 0,50. A multa de um empréstimo devolvido fica congelada na data da devolução; a de um empréstimo ainda ativo cresce até a data de referência informada.
- **O que muda no sistema:** nada no contrato. `devolver` continua devolvendo, mesmo com atraso, e o prazo continua sendo `PRAZO_EM_DIAS`. A multa é só consultada num serviço à parte.
- **Como usar:**

  ```java
  Cineclube cineclube = new Cineclube();
  BigDecimal devido = cineclube.multas()
          .multaTotalDoUsuario("2026001", LocalDate.of(2026, 11, 19));
  ```

- **Onde está no código:** `filme/MultaService.java`; testes em `MultaTest`.
---

## Uso de ferramentas de IA

O uso é permitido como apoio, desde que declarado em [`DECLARACAO-DE-USO-DE-IA.md`](DECLARACAO-DE-USO-DE-IA.md). Você continua responsável por explicar, testar e adaptar todo o código entregue — inclusive em uma alteração feita presencialmente, sem consulta, na defesa da Entrega 3.

---

## Equipe

| Nome | Matrícula | GitHub |
|---|---|---|
| Ântoni Êlae | 20250138152 | AntoniElae |
| Ryan Lucas | 20250114141 | Ryan-Lucas001 |