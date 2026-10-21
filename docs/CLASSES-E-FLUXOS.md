# Classes e fluxo de chamadas das aulas

Este guia acompanha os Javadocs de todas as classes, records e interfaces Java presentes nesta branch. Os gabaritos de avaliação ficam exclusivamente na branch `professor`; aqui estão as responsabilidades do código usado pelo aluno.

Leia primeiro [ARQUITETURA.md](ARQUITETURA.md). Para cada fluxo HTTP, o controller recebe e valida, o domínio decide, a infra executa efeitos externos pelas portas e o presenter transforma o resultado em resposta. Nos fluxos assíncronos, quem inicia é o broker ou o agendador, sem uma requisição HTTP esperando o resultado.

## Roteiro de leitura

1. Comece por `Order` e `OrderAccepted` no módulo `ordens-domain`.
2. Na aula 1, leia `OrderController`, `SubmitOrderUseCase`, `OrderCommandPublisher`, `RabbitOrderCommandPublisher` e `OrderPresenter`. Depois acompanhe `OrderWorker`, `AcceptOrderUseCase` e a publicação Kafka.
3. Na aula 2, siga `LabController`, `CreateOrderUseCase`, as portas SQL e `LabPresenter`. Depois leia o relay da Outbox e o consumidor da Inbox.
4. Consulte `UseCaseConfiguration` para ver as ligações reais feitas pelo Spring e os testes para observar cada fronteira isoladamente.

## Exemplo de uma requisição da aula 1

`POST /api/ordens` chama `OrderController.create`, que chama `SubmitOrderUseCase.execute`. O caso de uso cria `Order`, chama a porta `OrderCommandPublisher` e recebe o controle de volta após a confirmação do RabbitMQ. O controller então chama `OrderPresenter.accepted` e retorna HTTP 202. Isso confirma o comando enfileirado; o processamento do evento acontece depois, pelo worker.

## Exemplo de uma requisição da aula 2

`POST /api/ordens` chama `LabController.create`, que chama `CreateOrderUseCase.execute`. `TransactionBoundary` envolve as gravações de ordem e Outbox. Depois do commit, `LabPresenter.created` monta o envelope retornado com HTTP 201. Mais tarde, `OutboxScheduler` aciona a publicação Kafka. `PositionConsumer` chama o caso de uso da projeção; Inbox e posição são confirmadas juntas antes do retorno do listener. `GET /api/posicoes` consulta o estado convergido por `QueryLabUseCase` e `LabPresenter.positions`.

## Catálogo por módulo

### ordens-domain

| Classe ou tipo | Responsabilidade | Quem chama | Quem é chamado ou recebe o resultado |
|---|---|---|---|
| [Order](../ordens-domain/src/main/java/br/com/fiap/b3/ordens/domain/Order.java) | Representa uma ordem válida e protege conta, ativo, quantidade e identidade. | casos de uso e conversores de contratos | validações Java; não acessa HTTP, banco ou brokers |
| [OrderAccepted](../ordens-domain/src/main/java/br/com/fiap/b3/ordens/domain/OrderAccepted.java) | Representa o fato de que uma ordem foi aceita, com identidade e instante. | AcceptOrderUseCase, CreateOrderUseCase e CloudEvent.toDomain | Order; não publica mensagens por conta própria |
| [OrderTest](../ordens-domain/src/test/java/br/com/fiap/b3/ordens/OrderTest.java) | Verifica as invariantes da ordem no módulo de domínio compartilhado. | Maven Surefire no módulo ordens-domain | construtor Order com entradas válidas e inválidas |

### aula1

| Classe ou tipo | Responsabilidade | Quem chama | Quem é chamado ou recebe o resultado |
|---|---|---|---|
| [Application](../aula1/src/main/java/br/com/fiap/b3/aula1/Application.java) | Inicializa a aplicação Spring Boot e a descoberta dos componentes da aula. | JVM pelo método main, após o build Maven | SpringApplication e configurações de infra que compõem os objetos |
| [CreateOrderRequest](../aula1/src/main/java/br/com/fiap/b3/aula1/controller/http/OrderController.java) | Recebe os campos de entrada HTTP com validação na fronteira. | Spring MVC ao desserializar POST /api/ordens | controller encaminha valores simples ao caso de uso |
| [OrderController](../aula1/src/main/java/br/com/fiap/b3/aula1/controller/http/OrderController.java) | Recebe HTTP de criação, valida a entrada e coordena o caso de uso. | cliente HTTP em POST /api/ordens | SubmitOrderUseCase e OrderPresenter; não acessa RabbitMQ diretamente |
| [EventSubscribers](../aula1/src/main/java/br/com/fiap/b3/aula1/controller/messaging/EventSubscribers.java) | Demonstra assinaturas Kafka independentes para Risco e Auditoria. | listeners Kafka dos grupos aula1-risco e aula1-auditoria | logger com grupo, partição, offset, chave e evento; não altera o domínio |
| [OrderWorker](../aula1/src/main/java/br/com/fiap/b3/aula1/controller/messaging/OrderWorker.java) | Recebe o comando RabbitMQ, converte o contrato e aciona a aceitação. | listener RabbitMQ do Spring, com dois workers configurados | OrderCommand.toDomain e AcceptOrderUseCase; retorno permite o ack |
| [OrderCommandPublisher](../aula1/src/main/java/br/com/fiap/b3/aula1/domain/port/out/OrderCommandPublisher.java) | Define a capacidade de enfileirar uma ordem sem conhecer o broker. | SubmitOrderUseCase | implementação RabbitOrderCommandPublisher injetada por infra |
| [OrderEventPublisher](../aula1/src/main/java/br/com/fiap/b3/aula1/domain/port/out/OrderEventPublisher.java) | Define a capacidade de publicar o evento aceito sem acoplar o domínio ao Kafka. | AcceptOrderUseCase ou PublishPendingEventsUseCase da respectiva aula | implementação KafkaOrderEventPublisher injetada por infra |
| [AcceptOrderUseCase](../aula1/src/main/java/br/com/fiap/b3/aula1/domain/usecase/AcceptOrderUseCase.java) | Transforma o comando consumido em fato de negócio e solicita sua publicação. | OrderWorker.execute | OrderAccepted e a porta OrderEventPublisher; mantém o ID em reentregas |
| [SubmitOrderUseCase](../aula1/src/main/java/br/com/fiap/b3/aula1/domain/usecase/SubmitOrderUseCase.java) | Cria e valida a ordem antes de solicitar o enfileiramento do comando. | OrderController.create | Order e a porta OrderCommandPublisher; retorna o ID após a publicação |
| [Topology](../aula1/src/main/java/br/com/fiap/b3/aula1/infra/config/Topology.java) | Declara a fila durável e o tópico Kafka com três partições. | Spring durante a inicialização da aula 1 | beans Queue e NewTopic; os listeners usam os mesmos nomes de destino |
| [UseCaseConfiguration](../aula1/src/main/java/br/com/fiap/b3/aula1/infra/config/UseCaseConfiguration.java) | Compõe os casos de uso, o relógio e o presenter por injeção de construtor. | Spring durante a inicialização da aplicação | construtores do domínio e do presenter, conectando portas às implementações infra |
| [KafkaOrderEventPublisher](../aula1/src/main/java/br/com/fiap/b3/aula1/infra/messaging/KafkaOrderEventPublisher.java) | Implementa a saída Kafka e espera a confirmação antes de retornar ao domínio. | caso de uso através de OrderEventPublisher | contrato de evento, ObjectMapper e KafkaTemplate |
| [RabbitOrderCommandPublisher](../aula1/src/main/java/br/com/fiap/b3/aula1/infra/messaging/RabbitOrderCommandPublisher.java) | Implementa a publicação do comando com confirmação e verificação de roteamento. | SubmitOrderUseCase através de OrderCommandPublisher | OrderCommand, ObjectMapper e RabbitTemplate; só retorna após confirmação |
| [AcceptedOrderResponse](../aula1/src/main/java/br/com/fiap/b3/aula1/presenter/OrderPresenter.java) | Transporta os campos JSON da resposta HTTP 202 da aula 1. | OrderPresenter.accepted | serializador HTTP ao retornar pelo controller |
| [OrderPresenter](../aula1/src/main/java/br/com/fiap/b3/aula1/presenter/OrderPresenter.java) | Monta a resposta de comando aceito sem conhecer Spring ou RabbitMQ. | OrderController após SubmitOrderUseCase | AcceptedOrderResponse com ordemId e status; não executa regra de negócio |
| [AcceptedEventMessage](../aula1/src/main/java/br/com/fiap/b3/aula1/presenter/contract/AcceptedEventMessage.java) | Monta o formato externo do evento da aula 1. | KafkaOrderEventPublisher | OrderCommand.from para representar os dados da ordem |
| [OrderCommand](../aula1/src/main/java/br/com/fiap/b3/aula1/presenter/contract/OrderCommand.java) | Representa o comando na fronteira de mensageria e converte a ordem. | RabbitOrderCommandPublisher e OrderWorker | Order em from/toDomain; não envia mensagens |
| [ArchitectureTest](../aula1/src/test/java/br/com/fiap/b3/aula1/ArchitectureTest.java) | Protege as dependências de controller, domain, infra e presenter. | Maven Surefire ao executar mvn verify | ArchUnit sobre as classes de produção, sem incluir os testes |
| [HttpContractTest](../aula1/src/test/java/br/com/fiap/b3/aula1/HttpContractTest.java) | Verifica a integração controller-presenter sem iniciar os serviços externos. | Maven Surefire ao executar mvn verify | MockMvc, presenter real e casos de uso simulados para status e campos JSON |
| [OrderUseCasesTest](../aula1/src/test/java/br/com/fiap/b3/aula1/OrderUseCasesTest.java) | Verifica as decisões dos casos de uso sem iniciar banco ou brokers. | Maven Surefire ao executar mvn verify | casos de uso com portas falsas ou mocks e relógio fixo |

## Como conferir ao alterar uma classe

Execute `mvn clean verify`. Os testes ArchUnit impedem dependências do domínio ou presenter em frameworks e acesso direto do controller à infra. Os testes HTTP verificam que a extração do presenter preserva status e nomes JSON. O smoke em `scripts/smoke.py` exige os serviços iniciados e continua necessário para validar transações e mensageria reais.
