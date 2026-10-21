# Laboratórios EDA — núcleo inicial

## Download para os alunos

- [Baixar o ZIP completo — recomendado](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Laboratorios_EDA_Aulas_1_e_2_Java25.zip)
- [Baixar somente o bundle Git](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Laboratorios_EDA_Aulas_1_e_2.bundle)
- [Ver a release v1.0.0](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/tag/aulas-1-e-2-v1.0.0)

O ZIP contém a pasta `Entrega_EDA_Alunos`, com `LEIA-PRIMEIRO.md`, os guias revisados, a validação das duas aulas no Podman e o bundle com as três branches e o histórico. Descompacte o pacote, leia `LEIA-PRIMEIRO.md` e clone o bundle conforme as instruções. O primeiro build ainda precisa de internet para baixar imagens e dependências.

Esta branch `main` contém o domínio compartilhado e os guias de entrada. Para executar as aulas, selecione uma das branches preparadas:

```sh
git switch aula-1
# Depois da primeira aula, com os containers encerrados e alterações salvas:
git switch aula-2
```

Comece pela [preparação do ambiente e do repositório](docs/COMECE-AQUI.md). As branches são progressivas: `aula-1` introduz fila e streaming; `aula-2` acrescenta transações, contratos, Outbox e consumo idempotente.


## Repositório da turma

```sh
git clone -b aula-1 https://github.com/RafaelNevesdeOliveira/java-eventos.git laboratorios-eda
cd laboratorios-eda
```

- [Guia da aula 1: filas e streaming](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-1/docs/AULA-1.md).
- [Guia da aula 2: contratos, Outbox e idempotência](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-2/docs/AULA-2.md).

Para obter as três branches, clone o repositório ou use o bundle incluído no ZIP da entrega. O botão **Code → Download ZIP** do GitHub baixa somente uma branch, sem histórico.
