# Laboratórios EDA — aulas 1 e 2

## Download para os alunos

- [Baixar o ZIP completo — recomendado](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Laboratorios_EDA_Aulas_1_e_2_Java25.zip)
- [Baixar o guia de preparação para Windows e macOS em Word](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Guia_Preparacao_Ambiente_Windows_macOS_Jornada_B3.docx)
- [Baixar somente o bundle Git](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Laboratorios_EDA_Aulas_1_e_2.bundle)
- [Ver a release v1.0.0](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/tag/aulas-1-e-2-v1.0.0)

O ZIP contém a pasta `Entrega_EDA_Alunos`, com `LEIA-PRIMEIRO.md`, o guia de preparação em Word para Windows e macOS, os guias revisados, a validação das duas aulas no Podman e o bundle com as três branches e o histórico. Descompacte o pacote e comece por `Mensagem_Alunos_Preparacao_Aula_1.docx`, com os comandos de preparação automática para Windows e Mac. O primeiro build ainda precisa de internet para baixar imagens e dependências.

**Branch de referência: `aula-2`.** Java 25 · Spring Boot 4.1.1 · Clean Architecture · Podman Compose.

1. [Prepare o ambiente e obtenha o repositório](docs/COMECE-AQUI.md).
2. [Siga o passo a passo da aula 2](docs/AULA-2.md).
3. [Consulte o mapa da Clean Architecture](docs/ARQUITETURA.md).

## Executar com Maven: Podman ou OCI

O modo local permanece disponível. Esta evolução adiciona OCI Queue na aula 1 e OCI Streaming
com Kafka/SASL nas duas aulas. Consulte [OCI.md](docs/OCI.md) para configuração, autenticação e fluxos.

```sh
./scripts/iniciar.sh 1 local
./scripts/iniciar.sh 2 local
# Com as credenciais OCI privadas configuradas:
./scripts/iniciar.sh 1 oci
./scripts/iniciar.sh 2 oci
```

Execute cada comando em um terminal separado. No OCI, as APIs usam 8180/8182 e o PostgreSQL da
segunda aula fica no Podman, em schema separado. O ZIP da release v1.0.0 acima corresponde à entrega
anterior; esta integração está na branch `feat/oci-queue-streaming`.

## Iniciar esta aula

Na raiz do projeto, com o motor Podman funcionando:

```sh
podman compose up -d --build aula2
podman compose logs -f aula2
```

Espere a inicialização e use Ctrl+C para sair dos logs. A API fica em `http://localhost:8082`. O build é feito dentro de containers, sem exigir Java/Maven no host.

## Verificar

```sh
python3 scripts/smoke.py --aula 2
```

Para testes Java/ArchUnit, se houver Java 25 e Maven no host, use `mvn verify`. Para executar esses testes só com Podman, siga o guia da aula.

## Evolução e branches

- `main`: núcleo inicial de domínio e preparação.
- `aula-1`: domínio e aplicação da primeira aula, com RabbitMQ e Kafka.
- `aula-2`: evolução a partir da aula 1, acrescentando PostgreSQL, Outbox, Inbox, projeção e Schema Registry.

O código entregue é a solução de referência. Faça as evoluções propostas em `exercicios/aula-2`. Salve suas alterações e encerre os containers antes de trocar de aula. Os volumes persistem entre branches.

## Desenvolver pela IDE

Abra a raiz como projeto Maven. Para instalar o domínio e o POM pai no repositório local:

```sh
mvn install
```

Encerre o container da aplicação antes de iniciar a mesma API no host. Depois de subir somente os brokers/banco necessários, execute `mvn -pl aula2 spring-boot:run`. Endereços do host e dos containers estão no guia de preparação.

## Encerrar preservando os dados

```sh
podman compose --profile contratos down
```

Na aula 2, o registry é iniciado à parte com `podman compose --profile contratos up -d schema-registry` e verificado com `python3 scripts/contratos.py`.

[Registro de validação](docs/VALIDACAO.md)

O laboratório local substitui OCI Queue por RabbitMQ, OCI Streaming por Kafka e Supabase por PostgreSQL em container. Os conceitos são praticados localmente; APIs, permissões e provisionamento OCI são uma atividade específica posterior. As duas aulas foram validadas em Docker e, após recuperação pontual da VM, também em Podman 5.8.1 com 4 GB, sem reset nem exclusão de volumes. O diagnóstico e os limites estão no registro de validação.


## Repositório da turma

```sh
git clone -b aula-1 https://github.com/RafaelNevesdeOliveira/java-eventos.git laboratorios-eda
cd laboratorios-eda
```

- [Guia da aula 1: filas e streaming](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-1/docs/AULA-1.md).
- [Guia da aula 2: contratos, Outbox e idempotência](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-2/docs/AULA-2.md).

Para obter as três branches, clone o repositório ou use o bundle incluído no ZIP da entrega. O botão **Code → Download ZIP** do GitHub baixa somente uma branch, sem histórico.

## Maven e leitura das classes

Use JDK 25 e Maven 3.9. Na raiz desta branch, execute `mvn clean verify`. [ARQUITETURA.md](docs/ARQUITETURA.md) explica as camadas **controller**, **domain**, **infra** e **presenter** e como iniciar as aplicações com Maven. [CLASSES-E-FLUXOS.md](docs/CLASSES-E-FLUXOS.md) apresenta cada classe e quem chama quem. Cada declaração Java possui um comentário didático.
