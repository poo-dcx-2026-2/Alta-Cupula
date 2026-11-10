# Devolutiva da Entrega 1 — Alta Cúpula

## Nota: 8,5 de 10 (85 de 100 pontos)

Avaliei o último commit da branch `main` até 12/10/2026 às 23h59 (o prazo original, 05/10, mais uma semana de extensão), pela leitura do código, dos testes, do histórico de commits, dos pull requests e da documentação.

## Pontos por critério

| Critério | O que é avaliado | Pontos | Nível |
|---|---|---|---|
| 4.1 | Correção funcional e aderência ao contrato | 20 de 20 | Excelente |
| 4.2 | Modelagem OO, responsabilidades, coesão e acoplamento | 20 de 25 | Parcial |
| 4.3 | Testes e evidências de depuração | 12 de 15 | Adequado |
| 4.4 | Robustez, exceções e casos-limite | 8 de 10 | Adequado |
| 4.5 | Evolução, refatoração e qualidade do código | 8 de 10 | Adequado |
| 4.6 | Git, build, pipeline e documentação | 8 de 10 | Adequado |
| 4.7 | Processo, checkpoints, explicação e autoria | 9 de 10 | Excelente |
| | **Total** | **85 de 100** | |

## Justificativa (onde eu tirei pontos)

- Usuario mistura entidade e repositório, a coleção de Item é exposta e a UML contém divergências de tipos e relações.
- Muitas regras continuam concentradas em AcervoFilme.
- Inconsistência ao devolver lista vazia para trecho inválido, em vez de sinalizar dados inválidos.

## Checklist de correções

- [ ] **Separar a entidade `Usuario` do serviço.** Hoje `Usuario` guarda os dados de *um* usuário e também a coleção de *todos* os usuários, misturando entidade (o objeto do domínio) e repositório (o lugar onde os objetos ficam guardados). Deixem `Usuario` só com os dados de uma pessoa e movam o `Map<String, Usuario>` para o serviço de usuários.
- [ ] **Não devolver a lista interna de exemplares.** Quando um método faz `return exemplares;`, quem chama recebe a *mesma* lista que o objeto usa por dentro e pode adicionar ou apagar exemplares sem passar pelas regras (tombo repetido, item inexistente). Devolvam uma cópia que não pode ser alterada: `return List.copyOf(exemplares);` (ou `Collections.unmodifiableList(exemplares)`). Para conferir, escrevam um teste que tenta `add` na lista devolvida e espera `UnsupportedOperationException`.
- [ ] **Levar regras para dentro das entidades.** Validações como "o tombo não pode ser vazio" devem ficar no construtor de `Exemplar`, e não espalhadas em `AcervoFilme`. Assim o objeto nunca existe num estado inválido. Revisem também o campo de `Exemplar` cujo nome não deixa claro o que guarda.
- [ ] **Tratar trecho inválido em `buscarPorTitulo`.** Para trecho nulo ou em branco, lancem `DadosInvalidosException` em vez de devolver lista vazia. Lista vazia significa "procurei e não achei", o que é diferente de "a pergunta não faz sentido".
- [ ] **Corrigir as divergências do UML.** Conferam cada classe do `modelo.puml` contra o código: tipos dos atributos, nomes e as setas entre as classes.
- [ ] **Versionar a imagem do UML.** Além do `docs/modelo.puml` (o texto do diagrama), o enunciado pede a imagem gerada a partir dele (PNG ou SVG) dentro de `docs/`, adicionada ao repositório com `git add` e `commit`. Gerem a imagem pelo plugin PlantUML do IntelliJ ou pelo site do PlantUML e confiram que ela aparece no GitHub.
- [ ] **Justificar no README cada coleção escolhida.** Para cada `List`, `Set` ou `Map` do código, uma frase dizendo qual operação ela torna fácil. Exemplo: "usamos `Map<String, Item>` com o código como chave porque `buscarItem` precisa achar um item pelo código sem percorrer a lista inteira".
