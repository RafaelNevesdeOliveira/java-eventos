# Aula 1 — passo a passo do aluno

**Branch desta entrega:** `feat/oci-queue-streaming`. **Resultado:** receber um comando HTTP, distribuí-lo por uma fila e publicar um evento consumido de forma independente por Risco e Auditoria.

Leia também [o guia de cada classe e suas chamadas](CLASSES-E-FLUXOS.md) e [a arquitetura controller, domain, infra e presenter](ARQUITETURA.md).

Trabalhe em dupla. Faça a instalação e o download inicial antes da aula.

## 1. Preparar e conferir a branch

Conclua [COMECE-AQUI.md](COMECE-AQUI.md): Git, Podman, Compose, Python e obtenção do repositório. Na raiz do projeto:

```sh
git switch feat/oci-queue-streaming
git branch --show-current
git status
```

**Esperado:** branch `feat/oci-queue-streaming`, com `ordens-domain`, `aula1` e `aula2`.
A branch histórica `aula-1` continua disponível como referência anterior. Permaneça na integrada
para manter a opção OCI. Os comandos e portas deste guia são do **modo local com Podman**;
para executar a mesma aula com Queue/Streaming e Maven, siga [OCI.md](OCI.md).

## 2. Antes de executar: classificar as mensagens

Preencha uma tabela com mensagem, tipo e justificativa:

| Mensagem | Pergunta para classificar |
|---|---|
| `CriarOrdem` | Está pedindo que alguém faça algo? |
| `OrdemAceita` | Está comunicando um fato que já ocorreu? |
| `ConsultarPosicao` | Está solicitando uma leitura? |
| `CancelarOrdem` | Quem deve atender essa intenção? |
| `OrdemRejeitada` | Quais interessados poderiam reagir a esse fato? |

Desenhe também os contextos **Ordens**, **Risco** e **Auditoria**, indicando quem recebe o comando e quem reage ao evento. Risco e Auditoria são assinantes didáticos na mesma aplicação, não serviços implantados separadamente. `CancelarOrdem`, `OrdemRejeitada` e `ConsultarPosicao` são propostas de discussão; não há endpoints ou fluxos implementados para essas mensagens na aula 1.

Neste exemplo, `OrdemAceita` significa que a ordem passou pelas validações didáticas do laboratório. Não significa execução ou liquidação real em uma bolsa.

## 3. Subir o ambiente

```sh
podman info
podman compose version
podman compose config
podman compose up -d --build aula1
podman compose ps
```

O build compila o domínio e a aplicação com Java 25 dentro de uma imagem Maven. O Compose inicia RabbitMQ, Kafka e a aplicação `aula1`. Java/Maven no host não são necessários para esse caminho.

O primeiro build pode demorar pelos downloads. `up -d` devolve o terminal, mas a API pode ainda estar iniciando. Acompanhe:

```sh
podman compose logs -f aula1
```

Procure `Started Application` e a inicialização dos listeners. Interrompa a visualização com **Ctrl+C**; os containers continuam em execução. Confira também:

```sh
podman compose ps
podman compose logs --tail=30 rabbitmq kafka
```

**Esperado:** três serviços em execução e brokers saudáveis. Se a aplicação encerrou, leia os logs antes de continuar.

## 4. Abrir o projeto e localizar a Clean Architecture

Abra a **raiz** na IDE. Não é preciso iniciar a aplicação pela IDE neste momento.

Os caminhos Java abaixo têm os prefixos:

- Domínio: `ordens-domain/src/main/java/br/com/fiap/b3/ordens/`.
- Aplicação da aula: `aula1/src/main/java/br/com/fiap/b3/aula1/`.

| Camada | Arquivo relativo ao prefixo | O que você deve encontrar |
|---|---|---|
| Domínio | `domain/Order.java` | Conta, ativo e quantidade; validações que independem do HTTP. |
| Domínio | `domain/OrderAccepted.java` | Fato do negócio com ID, ordem e instante. |
| Entrada HTTP | `controller/http/OrderController.java` | Recebe JSON e chama o caso de uso; não acessa o RabbitTemplate. |
| Aplicação | `domain/usecase/SubmitOrderUseCase.java` | Cria a ordem e pede publicação do comando por uma porta. |
| Porta de saída | `domain/port/out/OrderCommandPublisher.java` | Capacidade de publicar; não informa como o broker funciona. |
| Saída RabbitMQ | `infra/messaging/RabbitOrderCommandPublisher.java` | Serialização, envio, publisher confirm e retorno de roteamento. |
| Entrada RabbitMQ | `controller/messaging/OrderWorker.java` | Recebe comando e chama `AcceptOrderUseCase`. |
| Aplicação | `domain/usecase/AcceptOrderUseCase.java` | Cria o evento e chama a porta de publicação de eventos. |
| Porta de saída | `domain/port/out/OrderEventPublisher.java` | Capacidade de publicar o evento. |
| Saída Kafka | `infra/messaging/KafkaOrderEventPublisher.java` | Publicação Kafka com chave por conta e espera pelo ack. |
| Entradas Kafka | `controller/messaging/EventSubscribers.java` | Grupos Risco e Auditoria; registra partição/offset. |
| Contratos | `presenter/contract/OrderCommand.java` e `AcceptedEventMessage.java` | JSON de integração e conversão para/de domínio. |
| Infraestrutura | `infra/config/Topology.java` | Fila, tópico e três partições. |
| Infraestrutura | `infra/config/UseCaseConfiguration.java` | Conecta portas/implementações e fornece o relógio. |

Domínio e casos de uso não importam Spring, JDBC, Jackson, Kafka ou RabbitMQ. Os adaptadores conhecem as portas da aplicação. O Spring conecta os objetos por injeção, na borda do sistema.

## 5. Enviar a primeira ordem

```sh
curl -i -X POST http://localhost:8080/api/ordens \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"conta-aula1-01","ativo":"PETR4","quantidade":10}'
```

**Esperado:** HTTP **202**, com resposta semelhante a:

```json
{"ordemId":"UUID_GERADO","status":"COMANDO_ENFILEIRADO"}
```

O publisher confirm permite confirmar o recebimento/roteamento pelo broker antes da resposta. O HTTP 202 não informa que todos os interessados já consumiram o evento. Uma falha/timeout HTTP também pode deixar o resultado de um envio incerto; esta API ainda não deduplica chamadas do cliente.

Agora envie uma quantidade inválida:

```sh
curl -i -X POST http://localhost:8080/api/ordens \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"conta-aula1-01","ativo":"PETR4","quantidade":0}'
```

**Esperado:** HTTP **400**. Localize a validação no DTO HTTP e a invariante no domínio. A regra de negócio continua existindo mesmo se, no futuro, outra entrada chamar o caso de uso sem passar pelo controller.

## 6. Seguir a ordem de ponta a ponta

```sh
podman compose logs --tail=150 aula1
```

Localize o `ordemId` da resposta. A sequência lógica é:

1. HTTP entra em `OrderController` e vira uma chamada a `SubmitOrderUseCase`.
2. O caso de uso cria `Order` e chama `OrderCommandPublisher`.
3. O adaptador RabbitMQ envia o comando para `aula1.ordens.comandos`.
4. Um dos workers recebe a entrega e chama `AcceptOrderUseCase`.
5. O caso de uso cria `OrderAccepted`; a implementação Kafka publica em `aula1.ordens.aceitas.v1`.
6. Os grupos `aula1-risco` e `aula1-auditoria` consomem independentemente.

**Esperado nos logs:** `WORKER=...`, `GRUPO=RISCO` e `GRUPO=AUDITORIA`, com o ID correspondente. Como há concorrência e comunicação assíncrona, as linhas podem aparecer intercaladas; não deduza a ordem de negócio apenas pela posição visual de todas as linhas.

Observe que o domínio não conhece fila, tópico ou JSON. O mesmo caso de uso pode ser acionado por outro adaptador sem incorporar uma biblioteca de transporte.

## 7. Observar os consumidores de fila

Abra [o painel RabbitMQ](http://localhost:15673), entre com `aluno` / `aluno-local` e procure a fila `aula1.ordens.comandos` na área de filas. Observe **dois consumidores**. São duas instâncias de listener na mesma aplicação; não são dois containers diferentes.

Envie uma sequência:

```sh
for n in 1 2 3 4 5 6 7 8 9 10; do
  curl -s -X POST http://localhost:8080/api/ordens \
    -H 'Content-Type: application/json' \
    -d "{\"contaId\":\"conta-aula1-$n\",\"ativo\":\"PETR4\",\"quantidade\":1}"
  echo
done
podman compose logs --tail=200 aula1
```

**O que observar:** os nomes dos workers alternam entre entregas. Cada entrega normal de um comando vai a um consumidor da fila, sem exigir divisão exatamente meio a meio. Reentregas podem ocorrer. `Ready` pode ficar em zero porque os workers são rápidos; isso não significa que a fila foi ignorada.

**Resposta a registrar:** por que não é correto colocar Risco e Auditoria como consumidores concorrentes da mesma fila se ambos precisam receber cada evento?

## 8. Observar grupos, chaves e partições no Kafka

```sh
podman compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka:9092 \
  --describe --topic aula1.ordens.aceitas.v1

podman compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --describe --group aula1-risco
```

**Esperado:** tópico com três partições; o grupo mostra offsets e lag. Os logs da aplicação mostram a chave `contaId`, a partição e o offset de cada consumo.

Envie mais duas ordens para `conta-aula1-01` repetindo o comando da etapa 5. Compare a partição dos eventos da mesma conta. Não é necessário que contas diferentes estejam sempre em partições diferentes; mais de uma chave pode mapear para a mesma partição.

A ordenação é por partição. Neste laboratório, com particionador padrão e número de partições constante, a mesma chave vai à mesma partição. Não existe ordenação global entre todas as contas. Grupos distintos mantêm offsets próprios, mesmo lendo o mesmo tópico.

## 9. Ler o histórico com outro grupo

```sh
podman compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic aula1.ordens.aceitas.v1 \
  --group aula1-inspecao-01 --from-beginning \
  --property print.partition=true --property print.offset=true
```

Espere aparecerem eventos que já foram consumidos pela aplicação e interrompa com **Ctrl+C**. Repita com o nome `aula1-inspecao-02`.

**Esperado:** o novo grupo consegue ler registros retidos no log. O consumo de Risco/Auditoria não remove esses registros. Se repetir com um grupo que já avançou seus offsets, `--from-beginning` sozinho não reinicia esse grupo; use um nome novo neste exercício.

**Entrega:** uma evidência de evento histórico com partição e offset e a explicação da diferença entre fila de trabalho e log retido.

## 10. Fazer a evolução de código

Crie sua branch de exercício a partir da referência:

```sh
git switch -c exercicios/aula-1
```

Em `aula1/src/main/java/br/com/fiap/b3/aula1/controller/messaging/EventSubscribers.java`, adicione um terceiro método de listener seguindo os métodos existentes:

```java
@KafkaListener(topics = Topology.TOPIC, groupId = "aula1-notificacao")
void notificacao(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
  registrar("NOTIFICACAO", record);
}
```

Recompile a imagem e envie uma nova ordem:

```sh
podman compose up -d --build aula1
```

Espere a aplicação reiniciar, repita a requisição da etapa 5 e confira os logs. **Esperado:** `GRUPO=NOTIFICACAO` junto de Risco e Auditoria. O novo grupo também pode ler eventos antigos porque a configuração de primeiro consumo é `earliest`.

**Onde essa mudança fica:** em um adaptador de entrada Kafka. Não foi necessário adicionar uma dependência de Kafka ao domínio ou ao caso de uso. Se notificações passassem a ter regras de negócio e efeitos reais, o listener deveria chamar um caso de uso e este depender de portas próprias.

Salve apenas o arquivo alterado:

```sh
git add aula1/src/main/java/br/com/fiap/b3/aula1/controller/messaging/EventSubscribers.java
git commit -m "Exercicio aula 1: acrescenta grupo de notificacao"
```

## 11. Verificar a solução

```sh
python3 scripts/smoke.py --aula 1
```

Esse script confirma recebimento HTTP e validação; confirme o fan-out nos logs. Para testes Java/ArchUnit, escolha um dos caminhos:

**Com Java 25 e Maven no host:**

```sh
mvn verify
```

**Somente com Podman:**

```sh
podman build --target build -f aula1/Dockerfile -t b3-eda-aula1-testes .
podman run --rm b3-eda-aula1-testes mvn -B -pl aula1 -am verify
```

**Esperado na branch `aula-1`:** 11 testes passam: dois de domínio, três de casos de uso, quatro regras arquiteturais e dois de contrato HTTP. Testes de unidade não precisam dos brokers em execução. Na branch `aula-2`, `mvn verify` da raiz inclui a segunda aplicação e executa 30 testes; use `mvn -pl aula1 -am verify` para selecionar somente domínio + aula 1 (11 testes).

## 12. Discutir a janela de duplicação

Siga `OrderWorker` → `AcceptOrderUseCase` → `KafkaOrderEventPublisher`. O worker espera o envio ao Kafka terminar antes de retornar e permitir o ack RabbitMQ. Se o processo cair depois do envio e antes do ack da fila, o comando pode voltar e gerar outra publicação.

O ID de evento é estável sob essa reentrega do comando, mas os assinantes desta aula apenas registram logs; ainda não há Inbox persistente. O `enable.idempotence` do produtor Kafka não elimina todas as duplicações entre sistemas. Esse limite é a motivação para a aula 2.

## 13. Entregar e encerrar

Salve capturas e anotações fora de `.lab/` se quiser incluí-las no commit; essa pasta é ignorada pelo Git. Se o professor pedir evidências em arquivos, entregue-as junto da sua branch de exercício.

Entregue: classificação das mensagens; mapa dos contextos Ordens/Risco/Auditoria; evidência dos dois workers e dos três grupos; leitura histórica; trecho de código alterado; explicação da janela de duplicação.

```sh
git status
podman compose down
```

O encerramento preserva os volumes. Siga [AULA-2.md](AULA-2.md) para trocar para a próxima branch. Sua implementação continua salva em `exercicios/aula-1`.
