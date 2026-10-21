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

## Como conferir ao alterar uma classe

Execute `mvn clean verify`. Os testes ArchUnit impedem dependências do domínio ou presenter em frameworks e acesso direto do controller à infra. Os testes HTTP verificam que a extração do presenter preserva status e nomes JSON. O smoke em `scripts/smoke.py` exige os serviços iniciados e continua necessário para validar transações e mensageria reais.
