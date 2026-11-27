# Devolutiva da Entrega 2 — Alta Cúpula

## Feedback

A entrega apresenta um domínio bem estruturado, boa cobertura de testes e uma extensão autoral implementada. O principal problema observado está no carregamento de dados ausentes pois a operação deve esvaziar a biblioteca mesmo quando ela já contém registros. No processo de desenvolvimento, falta registrar revisões com mais frequência.

## Checklist de ajustes sugeridos

- [ ] Fazer `carregar()` esvaziar o estado em memória quando o diretório ou os arquivos de dados estiverem ausentes.
- [ ] Restaurar o arquivo `ExemplarView.java` para ficar idêntico ao contrato oficial.
- [ ] Adicionar um teste de carregamento de diretório ausente que comece com a biblioteca preenchida.
- [ ] Registrar revisões substantivas dos dois integrantes em mais de um pull request.
- [ ] Manter as regras de negócio no domínio e reduzir dependências diretas entre serviços.
