# Roteiro do professor para as duas aulas com Maven

Jornada B3 de Arquitetura Orientada a Eventos

Conduza as aulas com Java 25, Maven e Clean Architecture em quatro camadas. Consulte este roteiro na branch `professor` e execute os comandos em outra cópia do laboratório, clonada do bundle atualizado, nas branches `aula-1` e `aula-2`. Faça o primeiro download e build antes do encontro.

## Preparação da turma e do professor

Quem já tinha o projeto deve salvar os exercícios fora da pasta antiga, encerrar seus containers com `podman compose --profile contratos down` e renomear a pasta antiga. Baixe o ZIP novo, extraia em uma pasta nova e faça um novo clone. Retome apenas os arquivos dos exercícios necessários, sem copiar `.git`, usar `git pull` na cópia antiga ou mesclar o histórico anterior.

[Baixar o ZIP atualizado das aulas](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Laboratorios_EDA_Aulas_1_e_2_Java25.zip)

A mensagem para repassar à turma é `Mensagem_Alunos_Preparacao_Aula_1.docx`, dentro do ZIP. Na pasta extraída `Entrega_EDA_Alunos`, o preparador faz o clone e verifica a aula 1. Deixe livre o destino `JornadaB3/laboratorios-eda` na pasta pessoal.

**Windows no PowerShell:**

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\preparar-windows.ps1
```

**Mac no Terminal:**

```bash
bash ./preparar-mac.sh
```

Aguarde **PRONTO**. Se o Windows pedir administrador ou reinício, siga a solicitação e repita o comando na mesma pasta. Os preparadores atendem ao fluxo em containers; **não instalam JDK e Maven no host**. Para conduzir pelo Maven no computador, prepare também **JDK 25 e Maven 3.9** e confira:

```sh
java -version
mvn -version
```

O Maven deve indicar Java 25. Confirme Podman e Compose funcionando. Nas páginas seguintes, comandos são executados na raiz do projeto, onde estão `pom.xml` e `compose.yaml`. No Windows, use `python` no lugar de `python3` se esse for o comando disponível para Python 3.10 ou superior.

## Apresente as quatro camadas

| Camada | Papel na aula | O que ler |
|---|---|---|
| `domain` | Entidades, casos de uso e portas sem Spring | `ordens-domain`, `domain/usecase` e `domain/port/out` |
| `controller` | Receber HTTP, mensagens e agendamentos | `controller/http`, `messaging` e `scheduling` |
| `presenter` | Preparar respostas HTTP e contratos externos | Presenters e `presenter/contract` |
| `infra` | Implementar portas e compor os objetos | JDBC, brokers, transações e `infra/config` |

Explique a execução: entrada → controller → caso de uso → porta → infra. Na resposta HTTP: resultado interno → presenter → controller → cliente. A dependência do código aponta para o domínio; o domínio não conhece suas implementações de infra. `UseCaseConfiguration` liga tudo por construtores. Nos fluxos assíncronos, o broker ou o agendador inicia outra execução.

### Leitura de apoio

- [Arquitetura das quatro camadas](../docs/ARQUITETURA.md).
- [Catálogo completo de classes e chamadas](../docs/CLASSES-E-FLUXOS.md).
- [Gabaritos e critérios de avaliação](../docs/PROFESSOR.md).
- [Evidências e limites da validação](../docs/VALIDACAO.md).
- [Versão deste roteiro em Word](Roteiro_Professor_Passos_Alunos_Aula_1.docx).

<!-- PAGEBREAK -->

## Aula 1 com Maven

**Objetivo:** receber um comando HTTP, enfileirar no RabbitMQ e publicar um evento Kafka observado por Risco e Auditoria. Divida os três blocos entre vocabulário e domínio, leitura do fluxo e execução com exercícios; desconte os intervalos do tempo disponível.

### Iniciar e testar

Na cópia nova do laboratório, depois de salvar alterações e com o diretório de trabalho limpo:

```sh
git status
git switch aula-1
podman compose stop aula1
podman compose up -d rabbitmq kafka
podman compose ps
mvn clean verify
mvn -pl aula1 -am install -DskipTests
mvn -pl aula1 spring-boot:run
```

Espere RabbitMQ e Kafka saudáveis antes de iniciar a API. **Esperado:** 11 testes em `verify` e aplicação em `http://localhost:8080`. O `install` prepara o domínio compartilhado no repositório Maven local; só pule os testes nessa etapa porque `verify` já os executou. Deixe o terminal do Maven aberto.

Em outro terminal, na mesma raiz:

```sh
python3 scripts/smoke.py --aula 1
```

**Esperado:** HTTP 202 para comando válido e 400 para entrada inválida. Confirme no terminal Maven os registros de Risco e Auditoria. HTTP 202 confirma o enfileiramento, e o processamento pelo worker acontece depois. O painel RabbitMQ está em `http://localhost:15673`, com `aluno` e `aluno-local`.

### Sequência de leitura das classes

1. **Domínio compartilhado:** `Order` protege as invariantes e `OrderAccepted` representa o fato aceito. Mostre que os dois não conhecem HTTP, Spring ou brokers.
2. **Caso de uso e porta:** `SubmitOrderUseCase` cria a ordem e chama `OrderCommandPublisher`. A implementação concreta é fornecida por infra, sem mudar o caso de uso.
3. **Entrada e saída HTTP:** `OrderController.create` valida a requisição, chama o caso de uso e entrega o ID ao `OrderPresenter.accepted`. O presenter monta `AcceptedOrderResponse` para HTTP 202.
4. **Enfileiramento:** `RabbitOrderCommandPublisher` converte com `OrderCommand`, serializa e aguarda a confirmação do RabbitMQ. Mostre o limite entre contrato externo e entidade.
5. **Processamento assíncrono:** `OrderWorker` converte o comando e chama `AcceptOrderUseCase`; este usa `OrderEventPublisher`, implementada por `KafkaOrderEventPublisher`. `AcceptedEventMessage` prepara o formato externo. `EventSubscribers` demonstra os dois grupos Kafka independentes.
6. **Composição e proteção:** `Topology` cria fila/tópico e `UseCaseConfiguration` injeta portas e presenter. Termine com `OrderUseCasesTest`, `HttpContractTest` e `ArchitectureTest`: decisões do caso, contrato HTTP e dependências das camadas.

### Fechar a aula e preparar a troca

Encerre a aplicação com **Ctrl+C** no terminal Maven. Na raiz, execute `podman compose down`, salve os exercícios e confirme `git status`. Só troque para a aula 2 com o trabalho salvo e sem alterações pendentes. Mantenha os volumes; não use `down -v`.

<!-- PAGEBREAK -->

## Aula 2 com Maven

**Objetivo:** gravar ordem e Outbox atomicamente, publicar por relay e aplicar o evento com Inbox e projeção. Distribua os blocos entre contratos, semânticas de entrega e os experimentos executáveis de Outbox/Inbox.

### Iniciar e testar

Depois de encerrar a aplicação e a infraestrutura da aula 1 e salvar os exercícios:

```sh
git status
git switch aula-2
podman compose stop aula2
podman compose up -d postgres kafka
podman compose ps
mvn -pl aula2 -am verify
mvn -pl aula2 -am install -DskipTests
mvn -pl aula2 spring-boot:run
```

Espere PostgreSQL e Kafka saudáveis antes de iniciar a API. **Esperado:** 21 testes selecionando domínio + aula 2 e aplicação em `http://localhost:8082`. Para verificar também a primeira aplicação nesta branch, use `mvn clean verify`: são **30 testes** na raiz.

Em outro terminal, na mesma raiz:

```sh
python3 scripts/smoke.py --aula 2
```

**Esperado:** entrada inválida rejeitada, rollback de ordem + Outbox, relay pausado e retomado, convergência e três reenvios sem duplicar a posição. Execute sem outros alunos usando a mesma instância: o teste de rollback compara contadores globais.

Para o bloco de evolução de contratos, em outro terminal:

```sh
podman compose --profile contratos up -d schema-registry
python3 scripts/contratos.py
```

Aguarde o registry responder em `http://localhost:8085` antes do script. Esse bloco usa o serviço real e não faz parte dos 30 testes Java.

### Sequência de leitura das classes

1. **Contrato:** leia `CloudEvent`, `OrdemData` e `CloudEventTest`. A conversão valida o envelope antes de produzir `OrderAccepted`; o contrato externo fica em presenter, não na entidade.
2. **Criação atômica:** `LabController.create` chama `CreateOrderUseCase`, que usa `TransactionBoundary`, `OrderRepository` e `OutboxRepository`. Veja `SpringTransactionBoundary`, `JdbcOrderRepository` e `JdbcOutboxRepository` para o commit SQL real. `LabPresenter.created` prepara a resposta CloudEvents.
3. **Relay:** `OutboxScheduler` chama `PublishPendingEventsUseCase`, que consulta `RelayControl`, bloqueia pendentes e publica por `OrderEventPublisher`. `KafkaOrderEventPublisher` aguarda o ack antes da marcação na Outbox; uma falha antes do commit ainda permite reentrega.
4. **Inbox e projeção:** `PositionConsumer` converte o evento e chama `ApplyOrderAcceptedUseCase`. `InboxRepository` e `PositionRepository`, implementadas pelos adaptadores JDBC, registram a identidade e alteram a posição na mesma transação.
5. **Consulta e reenvio:** `QueryLabUseCase` usa `LabReadRepository` e `ReadModels`; `LabPresenter` traduz os resultados para JSON. `RequeueEventUseCase` recoloca o mesmo ID como pendente. O presenter não consulta o banco nem executa os casos de uso.
6. **Composição e testes:** termine com `UseCaseConfiguration`, `KafkaConfiguration`, `OrderUseCasesTest`, `HttpContractTest` e `ArchitectureTest`. Diferencie os testes sem serviços externos do smoke com banco e brokers reais.

### Encerrar e consultar os detalhes

Encerre o Maven com **Ctrl+C** e execute `podman compose --profile contratos down` na raiz. Os volumes permanecem. Não execute a mesma API pelo Maven e no container ao mesmo tempo.
