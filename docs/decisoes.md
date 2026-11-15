# Decisões de projeto

## Fila de reservas: composição em vez de herança
**Contexto.** A reserva precisa respeitar a ordem de chegada.

**Decisão.** `FilaDeReservas` tem uma `List<Reserva>` privada e expõe
`entrar`, `proximaSemExemplar`, `buscarPorId`, `remover`, `contem` e `todas`
(cópia imutável).

**Alternativa rejeitada.** `FilaDeReservas extends ArrayList<Reserva>`: exporia
`add(int, ...)`, `set` e `clear`, e qualquer classe poderia furar a ordem.

**Consequência.** A ordem só muda por métodos da própria fila.

## Contagem de empréstimos ativos: no serviço de empréstimos (GRASP: Especialista)
**Contexto.** `UsuarioView.emprestimosAtivos` precisa de valor real.

**Decisão.** `Emprestimo.contarAtivos(matricula)` faz a conta, porque ele guarda
os registros de empréstimo (Especialista da Informação). `Usuario` consulta
esse método.

**Alternativa rejeitada.** O `Usuario` guardar a própria lista de empréstimos:
duplicaria a informação e acoplaria usuário e empréstimo nos dois sentidos.

**Consequência.** Dependência `Usuario -> Emprestimo` (setter, para evitar
construção circular); a ligação é feita no construtor do `Cineclube`.

## Exemplar decide se pode ser emprestado (GRASP: Especialista)
**Contexto.** Um exemplar RESERVADO só pode ir para o dono da reserva.

**Decisão.** `Exemplar.podeSerEmprestadoPara(matricula)` concentra status e
reserva. O status só muda por métodos com significado (`separarPara`,
`liberar`, `marcarEmprestado`, `marcarIndisponivel`).

**Consequência.** `Emprestimo` não lê o status para decidir; pergunta ao exemplar.

## Usuário: dados internos serializáveis
**Contexto.** `UsuarioView` é um record do contrato, não serializável.

**Decisão.** `Usuario` guarda `DadosUsuario` (serializável) e monta a
`UsuarioView` na consulta.

**Consequência.** `salvar()` funciona e o valor de `emprestimosAtivos` é sempre atual.

## Facade
**Decisão.** `Cineclube` implementa `Biblioteca` e é a única porta de entrada:
quem está fora recebe só views e exceções do contrato, nunca `Item`, `Exemplar`
ou `Reserva`. `salvar`/`carregar` ficam nela; o diretório é lido dentro dos
métodos (propriedade `biblioteca.dados`).

## Persistência por serialização Java
**Decisão.** Todo o grafo do `Cineclube` é gravado em `biblioteca.dat`.
`carregar` lê tudo antes de trocar o estado, então arquivo corrompido não
altera a memória. A causa original é preservada com `initCause`.

## Baixar exemplar RESERVADO
**Contexto.** A §3.1 não diz o que acontece com a reserva.

**Decisão.** O exemplar vai a INDISPONIVEL e a reserva volta à fila sem
exemplar, podendo receber o próximo exemplar devolvido.

**Alternativa rejeitada.** Cancelar a reserva: o usuário perderia a posição.

## Reservas de usuário desativado
**Decisão.** `reservar` recusa usuário inativo. (Reservas já existentes de quem
for desativado depois permanecem; complete aqui o que a equipe decidir.)

## Extensão autoral: multa por atraso fora do contrato
**Decisão.** `MultaService` só consulta `EmprestimoView`; `devolver` e o prazo
(`PRAZO_EM_DIAS`) não mudam. Valores em `BigDecimal`.