# Arquitetura do laboratório

Esta visão descreve a evolução até `aula-2`. Na branch `aula-1`, estão presentes somente o domínio e a primeira aplicação; os caminhos da segunda aula passam a existir após a troca de branch.

A base usa Clean Architecture com domínio compartilhado em um módulo Maven próprio e casos de uso Java independentes de Spring. Cada aula mantém sua aplicação executável e seus adaptadores. A direção das dependências é protegida por testes ArchUnit em `mvn verify`.

## Módulos e responsabilidades

| Local | Responsabilidade | Dependências permitidas |
|---|---|---|
| `ordens-domain` | `Order`, `OrderAccepted` e invariantes do negócio | Java; JUnit somente nos testes |
| `aula1/application` | Enfileirar ordem e aceitar comando recebido | Java e domínio |
| `aula2/application` | Criar ordem, publicar pendentes, aplicar evento, consultar e reenviar | Java e domínio |
| `aula*/adapters/in/web` | HTTP, validação de entrada e tradução de respostas | Casos de uso e contratos de integração |
| `aula*/adapters/in/messaging` | Receber mensagens e chamar casos de uso | Casos de uso, contratos e bibliotecas de mensageria |
| `aula2/adapters/in/scheduling` | Acionar periodicamente a publicação | Caso de uso e agendamento Spring |
| `aula*/adapters/out` | Implementar portas de publicação, persistência e consulta | Portas, domínio e bibliotecas externas |
| `aula*/adapters/contract` | DTOs de comandos/eventos e envelope CloudEvents | Domínio e formato da integração |
| `aula*/infrastructure` | Composição de objetos, configuração e transações Spring | Portas, casos de uso e framework |

Os casos de uso estão em pacotes dentro de cada aplicação, com independência verificada por ArchUnit. O domínio está separado também no build: seu JAR não traz Spring, Jackson, Kafka, RabbitMQ ou JDBC. Não há herança de entidades JPA, anotações de validação HTTP ou envelope CloudEvents no domínio.

As classes de caso de uso são as APIs de entrada da aplicação. As interfaces em `application/port/out` descrevem as capacidades externas necessárias. Não é obrigatório criar outra interface com os mesmos métodos para cada caso de uso.

## Fluxos principais

**Aula 1:** `OrderController` → `SubmitOrderUseCase` → `OrderCommandPublisher` → implementação RabbitMQ. Na chegada do comando, `OrderWorker` → `AcceptOrderUseCase` → `OrderEventPublisher` → implementação Kafka. Os assinantes Risco e Auditoria continuam sendo adaptadores de demonstração que registram mensagens, sem regras de negócio próprias.

**Aula 2, criação:** `LabController` → `CreateOrderUseCase` → `TransactionBoundary.required` → `OrderRepository` e `OutboxRepository`. O caso de uso cria o evento e define que as duas gravações pertencem à mesma unidade atômica. `JdbcOrderRepository` e `JdbcOutboxRepository` executam SQL; `SpringTransactionBoundary` executa a transação real.

**Aula 2, relay:** `OutboxScheduler` → `PublishPendingEventsUseCase` → bloqueio de pendentes, publicação confirmada e marcação de envio dentro da unidade transacional. O adaptador JDBC contém `FOR UPDATE SKIP LOCKED`; o adaptador Kafka espera o ack. Se a publicação falhar, o caso de uso não marca o evento e a transação é revertida. O agendamento não contém SQL ou política de publicação.

**Aula 2, projeção:** `PositionConsumer` desserializa/valida `CloudEvent` → `ApplyOrderAcceptedUseCase` → registro em `InboxRepository` e atualização de `PositionRepository` na mesma transação. O listener só retorna depois do commit SQL. A confirmação do offset continua posterior ao retorno. Duplicatas retornam sem repetir o efeito.

**Consultas:** `QueryLabUseCase` usa `LabReadRepository`, com modelos de leitura tipados independentes dos nomes JSON. O adaptador HTTP preserva nomes como `conta_id` e `publicada_em`, usados pelos exercícios existentes.

## Contratos e dados existentes

URLs, portas, filas, tópicos, grupos, tabelas e identificadores de eventos foram preservados. Os registros anteriores de Outbox e Inbox continuam utilizáveis. O envelope CloudEvents permanece no limite da integração, com mapeamento para `OrderAccepted`. Nenhuma migração de banco é necessária para esta reorganização.

A unidade de deduplicação continua sendo `(consumer_name, source, eventId)`. A criação pela API não é idempotente por conta própria: uma nova chamada produz uma nova ordem. O relay continua sendo polling com entrega que pode se repetir, e não uma transação distribuída PostgreSQL/Kafka.

## Como evoluir

1. Acrescente invariantes e fatos de negócio em `ordens-domain`, com testes que não inicializam Spring.
2. Acrescente o fluxo em `application/usecase`; defina uma porta de saída quando o caso precisar de algo externo.
3. Implemente a porta em `adapters/out`. Receba HTTP, filas ou agendamento em um adaptador de entrada que chame o caso de uso.
4. Registre novos casos de uso em `infrastructure/config/UseCaseConfiguration`. Não anote o caso de uso com `@Service` ou `@Transactional`.
5. Mantenha os testes ArchUnit e valide o cenário completo. Novos consumidores precisam de identidade própria na Inbox e da mesma atomicidade entre deduplicação e efeito.

Para a aula 3, uma Saga pode ter estado e transições no domínio, casos de uso de avanço/compensação na aplicação e portas de persistência/publicação implementadas pelos adaptadores. Uma integração OCI futura implementa as portas de mensageria; a adaptação ainda precisa respeitar as semânticas do serviço e a configuração dos listeners.

## Testes

`mvn verify` executa 21 testes: invariantes do domínio, casos de uso, conversão CloudEvents e regras de dependência. Os testes de unidade usam portas falsas ou mocks e verificam decisões, identidade e ordem das operações. Eles não substituem o PostgreSQL para provar rollback físico.

`python3 scripts/smoke.py` verifica o fluxo real com banco e brokers iniciados, incluindo rollback, pausa/retomada e reentrega. `python3 scripts/contratos.py` verifica evolução contra o Schema Registry real. Veja o registro em `VALIDACAO.md`.

Referência da ferramenta de proteção arquitetural: [ArchUnit](https://www.archunit.org/userguide/html/000_Index.html).
