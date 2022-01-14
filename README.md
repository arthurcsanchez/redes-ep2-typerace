# redes-ep2-typerace
Repositório para o EP2 de Redes de Computadores, EACH-USP - 2021/2

# Integrantes
* Arthur Calhares de Almeida Sanchez - 11831039
* Julianne Amanda de Sá Laurindo - 11894590
* Henrique Peterlevitz - 10338169

## Pré-requisitos
* JDK 11 ou maior (testado com a JDK11 OpenJDK no IntelliJ IDEA 2021.3)
* Gradle (incluso no repositório, não é necessário instalá-lo)
* Recomenda-se execução no macOS ou Linux para evitar problemas de *encoding* decorrentes da execução no Windows[^1]
[^1]: Durante os testes, foi possível a execução no Windows através da IDE IntelliJ IDEA, que interpreta os arquivos com o *encoding* UTF-8. A execução normal do programa não é garantida fora desse ambiente.

### Rodando
Para rodar o servidor
```sh
./gradlew server:run
```

Para rodar um cliente
```sh
./gradlew client:run
```
