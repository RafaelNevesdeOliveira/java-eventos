# Material do professor da Jornada B3

Esta é a branch **professor** dos laboratórios de Arquitetura Orientada a Eventos. Comece pelo roteiro abaixo para orientar a preparação dos alunos; use o guia do professor para conduzir as aulas 1 e 2.

## Preparar e orientar a turma

1. [Roteiro de preparação e condução das duas aulas com Maven](professor/PASSOS-PARA-OS-ALUNOS.md).
2. [Baixe o roteiro do professor em Word](professor/Roteiro_Professor_Passos_Alunos_Aula_1.docx).
3. [Consulte o guia do professor](docs/PROFESSOR.md), com planejamento, gabaritos das duas aulas, critérios de avaliação e diagnóstico.

## Entrega aos alunos

- [Baixar o ZIP pela release](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Laboratorios_EDA_Aulas_1_e_2_Java25.zip).
- [Acessar o mesmo ZIP nesta branch](entrega/Laboratorios_EDA_Aulas_1_e_2_Java25.zip): abra o arquivo e clique em **Download raw file**.
- [Verificar o SHA-256 do ZIP](entrega/SHA256SUMS.txt).

O ZIP contém a mensagem simplificada de uma página, os preparadores automáticos para Windows e Mac, o guia de preparação, os roteiros das duas aulas e o bundle Git. O aluno extrai o ZIP, abre **Entrega_EDA_Alunos** e executa apenas o comando do seu sistema, conforme o roteiro.

Para enviar a mensagem à turma, use **Mensagem_Alunos_Preparacao_Aula_1.docx**, dentro do ZIP. O preparo termina com **PRONTO**, depois da verificação HTTP da aula 1.

## Conduzir as aulas

- [Guia da aula 1 na branch aula-1](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-1/docs/AULA-1.md).
- [Guia da aula 2 na branch aula-2](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-2/docs/AULA-2.md).
- [Mapa da arquitetura](docs/ARQUITETURA.md).
- [Registro da validação das duas aulas no Podman](docs/VALIDACAO.md).
- [Verificação das branches](docs/VALIDACAO-BRANCHES.md).

Use as branches **aula-1** e **aula-2** para executar as respectivas aulas. Esta branch reúne o material de apoio do professor e tem como base o código de referência da aula 2. O bundle do ZIP contém **main**, **aula-1** e **aula-2**; a branch **professor** fica disponível no GitHub.

## Separação do material docente

Os gabaritos, o planejamento dos blocos e o roteiro do professor ficam nesta branch. O ZIP e o bundle dos alunos não incluem esses materiais. O bundle contém o histórico filtrado de `main`, `aula-1` e `aula-2`, com os mesmos commits publicados no GitHub e sem materiais docentes em nenhum commit. O histórico completo do professor continua preservado nesta branch.

## Maven e as quatro camadas

As aulas usam Maven e Java 25, com **controller**, **domain**, **infra** e **presenter**. Todos os tipos Java possuem comentários didáticos. Consulte [a arquitetura](docs/ARQUITETURA.md) e [a função de cada classe e quem chama quem](docs/CLASSES-E-FLUXOS.md). Os casos de uso e presenters são Java sem dependência de Spring; infra faz a composição e implementa as portas.
