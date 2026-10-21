# Aula 2 — passo a passo do aluno

**Branch:** `aula-2`. **Resultado:** gravar ordem e evento na mesma transação, publicar por Outbox e processar reentregas sem repetir o efeito no banco. Os conceitos de fila/grupos vistos na aula 1 continuam válidos; esta aplicação usa diretamente PostgreSQL e Kafka.

Leia também [o guia de cada classe e suas chamadas](CLASSES-E-FLUXOS.md) e [a arquitetura controller, domain, infra e presenter](ARQUITETURA.md).

Prepare as imagens antes da aula. Siga a sequência de experimentos abaixo.

## 1. Encerrar a aula 1 e trocar de branch

Se você estava executando a aula 1, encerre **antes** da troca:

```sh
podman compose down
git status
```

Salve suas alterações em `exercicios/aula-1` conforme o guia anterior. Com o diretório de trabalho sem alterações pendentes:

```sh
git switch aula-2
git branch --show-current
```

Em um clone, o Git normalmente cria a branch local a partir de `origin/aula-2`. Se não fizer isso automaticamente e `git branch -a` mostrar essa referência, execute `git switch --track origin/aula-2`.

**Esperado:** `aula-2`. A branch contém o código da primeira aula e acrescenta o módulo `aula2`, PostgreSQL e contratos. Ela não incorpora automaticamente o exercício individual salvo em `exercicios/aula-1`.

Se estiver começando direto nesta aula, conclua [COMECE-AQUI.md](COMECE-AQUI.md) e depois selecione `aula-2`.

## 2. Subir somente o que esta aula precisa

```sh
podman info
podman compose config
podman compose up -d --build aula2
podman compose ps
podman compose logs -f aula2
```

**Esperado:** PostgreSQL, Kafka e a aplicação `aula2` em execução. Aguarde `Started Application`, depois use **Ctrl+C** para sair dos logs. RabbitMQ não é usado pelo fluxo da aula 2. A API está em `8082`, não em `8080`.

```sh
curl -s http://localhost:8082/api/lab/status
```

A resposta contém `relayPausado`, `ordens`, `outbox`, `inbox` e `pendentes`. Os números não precisam começar em zero: volumes persistem entre execuções. A seleção de uma branch não apaga dados.

## 3. Localizar as camadas

Prefixo Java da aula: `aula2/src/main/java/br/com/fiap/b3/aula2/`. O domínio compartilhado permanece em `ordens-domain/src/main/java/br/com/fiap/b3/ordens/domain/`.

| Camada | Arquivo relativo ao prefixo da aula | Responsabilidade |
|---|---|---|
| Entrada HTTP | `controller/http/LabController.java` | JSON, status HTTP e chamada dos casos de uso. |
| Aplicação | `domain/usecase/CreateOrderUseCase.java` | Criar ordem/evento e exigir atomicidade de ordem + Outbox. |
| Portas | `domain/port/out/OrderRepository.java` e `OutboxRepository.java` | Gravar ordem e armazenar/publicar pendentes, sem conhecer SQL. |
| Aplicação | `domain/usecase/PublishPendingEventsUseCase.java` | Ler lote pendente, publicar e só depois marcar envio. |
| Entrada por agenda | `controller/scheduling/OutboxScheduler.java` | Chamar periodicamente o caso de uso de publicação. |
| Saída Kafka | `infra/messaging/KafkaOrderEventPublisher.java` | Serializar CloudEvents, enviar com chave e esperar ack. |
| Contrato | `presenter/contract/CloudEvent.java` | Envelope, validação e conversão para/de `OrderAccepted`. |
| Entrada Kafka | `controller/messaging/PositionConsumer.java` | Receber JSON e chamar a aplicação. |
| Aplicação | `domain/usecase/ApplyOrderAcceptedUseCase.java` | Registrar identidade na Inbox e aplicar efeito uma única vez. |
| Portas | `domain/port/out/InboxRepository.java` e `PositionRepository.java` | Deduplicação e atualização da projeção. |
| Saídas SQL | `infra/persistence/Jdbc*.java` | Implementações com SQL e `JdbcTemplate`. |
| Porta transacional | `domain/port/out/TransactionBoundary.java` | Permitir que casos de uso definam a unidade atômica. |
| Infraestrutura | `infra/transaction/SpringTransactionBoundary.java` | Executar essa unidade com `TransactionTemplate`. |
| Consultas | `domain/usecase/QueryLabUseCase.java` e `domain/model/ReadModels.java` | Ler modelos tipados por uma porta. |
| Composição | `infra/config/UseCaseConfiguration.java` | Construir casos de uso e conectá-los às implementações. |

As tabelas são criadas por `aula2/src/main/resources/schema.sql`. A configuração de conexão e de consumidores está em `aula2/src/main/resources/application.yml`.

## 4. Conhecer os três fluxos antes de testar

**Criação:** controller → `CreateOrderUseCase` → `TransactionBoundary` → repositórios de ordem e Outbox. As duas inserções precisam do mesmo commit. O HTTP 201 confirma o banco; ainda não confirma consumo.

**Publicação:** scheduler → `PublishPendingEventsUseCase` → Outbox com lock → publisher Kafka → marcação de envio → commit. A transação segura locks durante o envio neste exemplo didático. Um crash entre confirmação Kafka e commit SQL pode gerar reenvio.

**Consumo:** listener Kafka → validação/conversão do CloudEvent → `ApplyOrderAcceptedUseCase` → Inbox + projeção na mesma transação. O offset só é confirmado depois do retorno do listener. Um crash entre commit SQL e offset gera reentrega; a Inbox evita repetir o efeito.

Os nomes CloudEvents, Kafka, SQL e Jackson pertencem aos adaptadores/infraestrutura. O domínio trabalha com `Order` e `OrderAccepted`, e a aplicação depende de portas.

## 5. Criar uma ordem e guardar o evento

Crie uma pasta local para as respostas; ela é ignorada pelo Git:

```sh
mkdir -p .lab
curl -s -X POST http://localhost:8082/api/ordens \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"conta-aula2-eventual-01","ativo":"PETR4","quantidade":10}' \
  -o .lab/primeiro-evento.json
python3 -m json.tool .lab/primeiro-evento.json
```

A API devolve HTTP **201**. Para enxergar o status em uma chamada seguinte, use `curl -i`; no comando acima, o corpo foi salvo para inspeção. Identifique:

| Campo | Significado |
|---|---|
| `specversion` | Versão do padrão CloudEvents, aqui `1.0`. |
| `id` + `source` | Identidade da ocorrência. Será usada na deduplicação. |
| `type` | Tipo/versão semântica do evento de integração. |
| `subject` | Recurso do qual o evento trata. |
| `time` | Instante da ocorrência. |
| `datacontenttype` | Formato do payload, `application/json`. |
| `dataschema` | Identificador do esquema de dados. |
| `data` | Dados de negócio, incluindo `ordemId`, conta, ativo e quantidade. |

**Não confunda:** `id` é o ID do evento e `data.ordemId` é o ID da ordem. Eles são diferentes nesta aula. O cabeçalho Kafka de formato é `content-type: application/cloudevents+json`; o produtor usa JSON estruturado, não a serialização binária Confluent.

Aguarde alguns segundos e consulte:

```sh
curl -s http://localhost:8082/api/posicoes
podman compose logs --tail=100 aula2
```

**Esperado:** uma posição da conta criada e logs `OUTBOX_PUBLICADA` e `POSICAO_ATUALIZADA`. Se repetir a mesma criação, você gera outra ordem e a soma aumenta. Para repetir os experimentos com resultados isolados, use uma conta nova, por exemplo trocando o sufixo `01` por `02` em todos os comandos relacionados.

## 6. Evoluir um contrato no Schema Registry

```sh
podman compose --profile contratos up -d schema-registry
python3 scripts/contratos.py
```

O script espera o registry ficar disponível e cria um subject novo a cada execução. **Esperado:** campo opcional aceito sob BACKWARD e troca de tipo rejeitada com HTTP 409.

Abra `contratos/ordem-aceita-v1.json`, `ordem-aceita-v2-compativel.json` e `ordem-aceita-v3-incompativel.json`.

- V1 define o esquema do objeto `data`.
- V2 acrescenta `canal` opcional: o novo esquema aceita mensagens antigas.
- V3 troca o tipo de `quantidade`: o novo esquema não aceita os dados numéricos anteriores.

BACKWARD significa que o esquema novo aceita dados do anterior. Não é promessa de que todo consumidor antigo aceitará dados novos. Como v1 fecha propriedades adicionais, discuta o que ocorre se um produtor enviar `canal` a um consumidor que valida estritamente v1.

**Onde fica na arquitetura:** schemas e CloudEvents são contratos de integração na borda. O Schema Registry deste exercício demonstra governança; o produtor não o consulta automaticamente para validar cada publicação. O consumidor verifica em Java os campos e valores previstos em `CloudEvent.validar()`; isso não equivale a executar todo o JSON Schema v1. Identifique o adaptador em que você acrescentaria validação integral de JSON Schema.

## 7. Comparar propagação de estado

O evento atual leva conta, ativo e quantidade. O consumidor consegue atualizar sua visão sem consultar a API de ordens: é um exemplo de Event Carried State Transfer.

Desenhe uma alternativa de Event Notification com apenas ID/referência e responda:

1. Que consulta o consumidor precisaria fazer?
2. O que ocorreria se a API de ordens estivesse indisponível?
3. A consulta poderia devolver um estado posterior ao instante do evento?
4. Quais dados precisam ser minimizados em cada abordagem?

`GET /api/ordens/{id}` é uma proposta para o desenho; esse endpoint individual não existe na referência. O endpoint implementado é `GET /api/ordens`, que lista ordens.

## 8. Provar atomicidade com uma falha

Sem outro colega criando ordens nesta mesma instância, compare:

```sh
curl -s http://localhost:8082/api/lab/status
curl -i -X POST 'http://localhost:8082/api/ordens?falhar=true' \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"conta-aula2-rollback-01","ativo":"PETR4","quantidade":10}'
curl -s http://localhost:8082/api/lab/status
```

**Esperado:** HTTP **503**; contadores `ordens` e `outbox` iguais aos valores anteriores. Outros contadores podem mudar se havia pendentes sendo consumidos.

Siga o `CreateOrderUseCase`: ele grava ordem e Outbox e depois lança `SimulatedFailure`. A implementação Spring da porta transacional reverte ambas. Não há uma gravação de negócio sem o evento correspondente. A falha deliberada existe para o laboratório.

## 9. Observar consistência eventual

Pause o relay e use uma **nova conta**:

```sh
curl -s -X POST http://localhost:8082/api/lab/relay/pausar
curl -s -X POST http://localhost:8082/api/ordens \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"conta-aula2-pausa-01","ativo":"PETR4","quantidade":10}' \
  -o .lab/evento-pausa.json
python3 -m json.tool .lab/evento-pausa.json
curl -s http://localhost:8082/api/lab/status
curl -s http://localhost:8082/api/outbox
curl -s http://localhost:8082/api/posicoes
```

**Esperado:** ordem gravada, nova linha com `publicada_em: null`, contador de pendentes aumentado e nenhuma posição para a nova conta. A pausa controla próximos ciclos; um lote que já estava executando pode terminar. Outros registros antigos podem continuar aparecendo.

Retome:

```sh
curl -s -X POST http://localhost:8082/api/lab/relay/retomar
```

Aguarde alguns segundos, repita as três consultas e observe a convergência: `publicada_em` preenchido e quantidade 10 para a conta. O intervalo de 1,5 segundo é apenas o agendamento configurado; não é um limite máximo de latência de ponta a ponta.

## 10. Reenviar o mesmo evento

Extraia o **ID do evento** salvo na etapa 9:

```sh
EVENT_ID=$(python3 -c 'import json; print(json.load(open(".lab/evento-pausa.json"))["id"])')
echo "$EVENT_ID"
curl -s -X POST "http://localhost:8082/api/lab/outbox/$EVENT_ID/reenviar"
```

A resposta indica `PENDENTE`. Aguarde a próxima publicação e consulte:

```sh
curl -s http://localhost:8082/api/posicoes
podman compose logs --tail=100 aula2
```

**Esperado:** `DUPLICATA_IGNORADA`; a conta `conta-aula2-pausa-01` permanece com quantidade **10**, não 20. Você pode repetir o reenvio mais duas vezes, aguardando entre eles.

O endpoint apenas torna a linha da Outbox pendente novamente; o relay publica o mesmo ID. Não use um novo POST de criação para demonstrar duplicata: ele representa outra ordem e outro evento.

**Onde está a decisão:** `ApplyOrderAcceptedUseCase` pede registro na Inbox e só chama a porta de posição se a identidade for nova. A restrição única é implementada por `JdbcInboxRepository`. Registrar a Inbox e aplicar o efeito em transações distintas criaria uma janela de perda ou duplicação do efeito.

## 11. Conferir as quatro tabelas

```sh
podman compose exec postgres psql -U aluno -d eda -c 'TABLE ordens;'
podman compose exec postgres psql -U aluno -d eda -c 'TABLE outbox;'
podman compose exec postgres psql -U aluno -d eda -c 'TABLE inbox;'
podman compose exec postgres psql -U aluno -d eda -c 'TABLE posicao;'
```

| Tabela | Papel | Implementação relevante |
|---|---|---|
| `ordens` | Estado de escrita do exemplo | `JdbcOrderRepository` |
| `outbox` | Eventos aguardando publicação/confirmados | `JdbcOutboxRepository` |
| `inbox` | Identidades já aplicadas por consumidor | `JdbcInboxRepository` |
| `posicao` | Projeção didática de quantidades aceitas | `JdbcPositionRepository` |

Encontre `(consumer_name, event_source, event_id)` na Inbox. O mesmo evento pode gerar efeitos independentes em consumidores diferentes, cada um com sua própria identidade de consumidor. A tabela `posicao` não é uma posição financeira liquidada.

## 12. Simular uma indisponibilidade do Kafka

Faça este experimento com acompanhamento do professor. Pare somente o broker:

```sh
podman compose stop kafka
curl -i -X POST http://localhost:8082/api/ordens \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"conta-aula2-offline-01","ativo":"PETR4","quantidade":7}'
curl -s http://localhost:8082/api/lab/status
podman compose logs --tail=80 aula2
```

**Esperado:** HTTP 201 para a transação SQL; evento pendente; o relay pode registrar erro/timeout e tentar novamente. O endpoint de criação não depende de conseguir publicar no Kafka no mesmo instante.

Reinicie o broker mesmo se alguma consulta anterior falhar:

```sh
podman compose start kafka
podman compose ps
```

Aguarde a recuperação e confira `/api/posicoes` e `/api/outbox`. **Esperado:** quantidade 7 para a conta nova e evento marcado como publicado. Se houve resultado incerto de envio, a reentrega continua sendo possível.

Descreva as garantias: entrega que pode se repetir, efeito idempotente nesta projeção SQL e consistência eventual. Não há exactly once global nem commit único envolvendo PostgreSQL e Kafka.

## 13. Fazer uma evolução guiada na aplicação

Crie uma branch de exercício:

```sh
git switch -c exercicios/aula-2
```

Em `ApplyOrderAcceptedUseCase`, extraia o literal `"posicao"` para uma constante privada `CONSUMER_NAME` e use-a em `inbox.register(...)`. Execute os testes. A mudança deve preservar o valor persistido e o comportamento.

Depois, em `OrderUseCasesTest` da aula 2, acrescente um cenário: a Inbox aceita a identidade, mas `PositionRepository.add(...)` falha. Verifique que a exceção sai do caso de uso e que a fronteira falsa não confirma a transação. Use a mesma classe `Boundary` e o estilo dos testes existentes.

**O que aprender:** testar uma decisão do caso de uso sem subir Spring ou banco. O teste falso verifica coordenação e propagação; o rollback físico ainda precisa da implementação transacional real, como no experimento da etapa 8.

Reconstrua a aplicação após a alteração:

```sh
podman compose up -d --build aula2
```

Como extensão para a dupla: desenhe uma projeção de auditoria independente, com caso de uso, porta, repositório e outro grupo Kafka. Antes de implementar, explique como preservar a atomicidade da Inbox com o novo efeito e por que deve haver outro `consumer_name`.

## 14. Executar as verificações

```sh
python3 scripts/smoke.py --aula 2
python3 scripts/contratos.py
```

O primeiro script não precisa da aplicação da aula 1. Ele usa uma conta única e testa criação, validação, rollback, pausa/retomada e três reenvios. Execute sem outras escritas concorrentes durante a comparação de contadores.

**Com Java 25/Maven no host:**

```sh
mvn verify
```

**Somente com Podman:**

```sh
podman build --target build -f aula2/Dockerfile -t b3-eda-aula2-testes .
podman run --rm b3-eda-aula2-testes mvn -B -pl aula2 -am verify
```

Na referência, `mvn verify` da raiz executa **30 testes** (domínio + duas aplicações). O comando em container seleciona domínio + aula 2 e executa **21 testes**. Seu teste adicional deve aumentar o total correspondente em um.

## 15. Salvar, entregar e encerrar

Salve os dois arquivos alterados na branch `exercicios/aula-2`:

```sh
git status
git add aula2/src/main/java/br/com/fiap/b3/aula2/domain/usecase/ApplyOrderAcceptedUseCase.java
git add aula2/src/test/java/br/com/fiap/b3/aula2/OrderUseCasesTest.java
git commit -m "Exercicio aula 2: identifica consumidor e testa falha na projecao"
```

As respostas em `.lab/` são ignoradas pelo Git. Para entregar evidências em arquivos, copie as que o professor solicitar para outra pasta e inclua-as no commit, ou entregue-as separadamente junto da sua branch. Antes de repetir os experimentos, troque o sufixo das contas, como orientado na etapa 5.

Entregue: CloudEvent anotado; resultado da evolução de contrato; evidência do rollback; antes/depois da pausa; reenvio com quantidade preservada; papel das quatro tabelas; teste novo e explicação dos limites de garantia.

```sh
git status
podman compose --profile contratos down
```

Os volumes são preservados para a evolução das próximas aulas. O mapa completo da arquitetura está em [ARQUITETURA.md](ARQUITETURA.md).
