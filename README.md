# Laboratórios EDA — aula 1

**Branch de referência: `aula-1`.** Java 25 · Spring Boot 4.1.1 · Clean Architecture · Podman Compose.

1. [Prepare o ambiente e obtenha o repositório](docs/COMECE-AQUI.md).
2. [Siga o passo a passo da aula 1](docs/AULA-1.md).
3. [Consulte o mapa da Clean Architecture](docs/ARQUITETURA.md).

## Iniciar esta aula

Na raiz do projeto, com o motor Podman funcionando:

```sh
podman compose up -d --build aula1
podman compose logs -f aula1
```

Espere a inicialização e use Ctrl+C para sair dos logs. A API fica em `http://localhost:8080`. O build é feito dentro de containers, sem exigir Java/Maven no host.

## Verificar

```sh
python3 scripts/smoke.py --aula 1
```

Para testes Java/ArchUnit, se houver Java 25 e Maven no host, use `mvn verify`. Para executar esses testes só com Podman, siga o guia da aula.

## Evolução e branches

- `main`: núcleo inicial de domínio e preparação.
- `aula-1`: domínio e aplicação da primeira aula, com RabbitMQ e Kafka.
- `aula-2`: evolução a partir da aula 1, acrescentando PostgreSQL, Outbox, Inbox, projeção e Schema Registry.

O código entregue é a solução de referência. Faça as evoluções propostas em `exercicios/aula-1`. Salve suas alterações e encerre os containers antes de trocar de aula. Os volumes persistem entre branches.

## Desenvolver pela IDE

Abra a raiz como projeto Maven. Para instalar o domínio e o POM pai no repositório local:

```sh
mvn install
```

Encerre o container da aplicação antes de iniciar a mesma API no host. Depois de subir somente os brokers/banco necessários, execute `mvn -pl aula1 spring-boot:run`. Endereços do host e dos containers estão no guia de preparação.

## Encerrar preservando os dados

```sh
podman compose down
```

[Orientações do professor](docs/PROFESSOR.md) · [Registro de validação](docs/VALIDACAO.md)

O laboratório local substitui OCI Queue por RabbitMQ, OCI Streaming por Kafka e Supabase por PostgreSQL em container. Os conceitos são praticados localmente; APIs, permissões e provisionamento OCI são uma atividade específica posterior. A validação funcional foi feita em Docker com o mesmo Compose, devido ao erro de armazenamento registrado na VM Podman da preparação.
