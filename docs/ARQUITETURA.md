# Clean Architecture das aulas com Maven

O projeto usa Java 25 e Maven multimódulo. `ordens-domain` contém entidades e fatos compartilhados; `aula1` e `aula2` são aplicações Spring Boot independentes. Cada branch contém apenas os módulos daquela etapa. A partir da aula 2, a primeira aplicação permanece como referência.

## As quatro camadas

| Camada | Onde fica | Responsabilidade | Pode conhecer |
|---|---|---|---|
| `controller` | `aula*/controller/http`, `messaging`, `scheduling` | Receber HTTP, mensagens e ticks; chamar casos de uso | `domain`, `presenter` e bibliotecas de entrada |
| `domain` | `ordens-domain/.../domain` e `aula*/domain` | Entidades, casos de uso, modelos internos e portas de saída | Java e o próprio domínio |
| `infra` | `aula*/infra/config`, `messaging`, `persistence`, `transaction` | Spring, JDBC, RabbitMQ, Kafka, composição e transações reais | Portas e tipos de `domain`; contratos de `presenter` |
| `presenter` | `aula*/presenter` e `presenter/contract` | Preparar a resposta HTTP e representar contratos externos | Java, modelos de `domain` e apresentação |

Para manter quatro pastas principais, as regras de aplicação ficam em `domain/usecase` e as portas em `domain/port/out`. Dentro da camada interna, entidades e casos de uso continuam tendo responsabilidades diferentes. Não coloque regras em controllers, SQL em casos de uso ou Spring no domínio. O módulo `ordens-domain` continua separado no Maven.

`Application` é somente o ponto de partida Spring Boot na raiz do pacote. `infra/config/UseCaseConfiguration` liga as portas às implementações e cria os presenters; os casos de uso e presenters são objetos Java sem anotações Spring.

## Quem chama quem

A requisição HTTP segue `cliente -> controller -> caso de uso -> porta -> implementação infra`. Na volta, `resultado interno -> presenter -> controller -> JSON do cliente`. A seta de execução não muda a dependência do código: a implementação de infra conhece a interface do domínio, e o domínio não conhece sua implementação.

Na aula 1: `OrderController -> SubmitOrderUseCase -> OrderCommandPublisher -> RabbitOrderCommandPublisher`. Depois da confirmação, `OrderPresenter` formata HTTP 202. O broker inicia outro fluxo: `OrderWorker -> AcceptOrderUseCase -> OrderEventPublisher -> KafkaOrderEventPublisher`. Risco e Auditoria são observadores independentes do tópico, demonstrados por `EventSubscribers`.

Na aula 2: `LabController -> CreateOrderUseCase -> TransactionBoundary -> OrderRepository + OutboxRepository`. O controller passa o resultado a `LabPresenter`, que devolve CloudEvents. O agendador chama `OutboxScheduler -> PublishPendingEventsUseCase -> OutboxRepository + OrderEventPublisher`. Na chegada do evento, `PositionConsumer -> ApplyOrderAcceptedUseCase -> InboxRepository + PositionRepository`, dentro da mesma transação SQL. Consultas seguem `LabController -> QueryLabUseCase -> LabReadRepository -> LabPresenter`.

Os contratos `OrderCommand`, `AcceptedEventMessage` e `CloudEvent` são formatos externos em `presenter/contract`, compartilhados pelos limites de entrada e saída. Não são entidades. Os listeners convertem esses contratos antes de entrar nos casos de uso. A infraestrutura serializa os contratos para transporte e persistência de payloads.

## Compilar e executar com Maven

Na raiz da branch, com JDK 25 e Maven 3.9 disponíveis:

```sh
mvn clean verify
```

Os testes de domínio, casos de uso, contratos HTTP e ArchUnit fazem parte do build. Para executar somente a aula 1 e suas dependências: `mvn -pl aula1 -am verify`. Na branch aula-2, use `mvn -pl aula2 -am verify` para selecionar domínio e aula 2.

Para rodar a aplicação pelo Maven no computador, deixe o Podman executar apenas os serviços de infraestrutura e encerre antes qualquer container da mesma aplicação:

```sh
# Aula 1, na branch aula-1
podman compose stop aula1
podman compose up -d rabbitmq kafka
mvn -pl aula1 -am install -DskipTests
mvn -pl aula1 spring-boot:run
```

```sh
# Aula 2, na branch aula-2
podman compose stop aula2
podman compose up -d postgres kafka
mvn -pl aula2 -am install -DskipTests
mvn -pl aula2 spring-boot:run
```

`install` prepara o módulo compartilhado no repositório Maven local; `spring-boot:run` inicia somente o módulo escolhido. O teste deve ser feito antes com `verify`. Na aula 2, o Schema Registry é opcional para o bloco de contratos: `podman compose --profile contratos up -d schema-registry`. Use Ctrl+C para encerrar a aplicação Maven e `podman compose --profile contratos down` para encerrar a infraestrutura preservando os volumes.

Os Dockerfiles também compilam com Maven; a opção de containers continua disponível. Não execute a mesma API no host e no container ao mesmo tempo. No host, as portas são 8080/8082 para APIs, 19092 para Kafka, 5673 para RabbitMQ e 5433 para PostgreSQL.

## Garantias e limites

URLs, campos JSON, tabelas, filas, tópicos e volumes não mudam com a organização de pacotes. Inbox e projeção continuam na mesma transação SQL; a entrega pode se repetir. Confirmar Kafka antes de marcar Outbox não produz uma transação distribuída com PostgreSQL. O domínio e o presenter não acessam Spring, JDBC ou os brokers: os testes ArchUnit protegem essas fronteiras.

Leia [CLASSES-E-FLUXOS.md](CLASSES-E-FLUXOS.md) para a responsabilidade de cada classe, seus chamadores, suas saídas e os testes.
