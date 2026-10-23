# Rodar as aulas no Podman ou na OCI

O modo padrão continua local, com Java 25, Maven e Podman. O perfil `oci` troca a mensageria,
preservando os casos de uso e os contratos HTTP. Não é necessário Docker Desktop.

## Comandos no Mac

Na raiz do projeto, escolha a aula e o modo. Execute cada aplicação em seu próprio terminal:

```sh
./scripts/iniciar.sh 1 local
./scripts/iniciar.sh 2 local
```

Com a configuração privada OCI pronta:

```sh
./scripts/iniciar.sh 1 oci
./scripts/iniciar.sh 2 oci
```

| Aula | Modo local | Modo OCI |
| --- | --- | --- |
| 1 | API 8080 → RabbitMQ → workers → Kafka → Risco e Auditoria | API 8180 → OCI Queue → polling → Streaming/Kafka SASL_SSL → Risco e Auditoria |
| 2 | API 8082 → PostgreSQL/Outbox → Kafka → Inbox/posição | API 8182 → PostgreSQL/Outbox → Streaming/Kafka SASL_SSL → Inbox/posição |

A aula 2 mantém o PostgreSQL no Podman nos dois modos. O perfil OCI usa o schema `oci_aulas`;
o local continua no schema padrão. O script cria o schema quando necessário e preserva os dados.
As portas podem ser substituídas pela variável `PORT`. Nenhum comando reinicializa a VM ou remove volumes.

## Autenticação e configuração privada

Queue e Streaming usam autenticações distintas:

- **Queue:** SDK Java oficial, sessão temporária do perfil `JORNADA_B3` da OCI CLI.
- **Streaming:** protocolo Kafka com `SASL_SSL`, mecanismo `PLAIN` e um Auth Token OCI.
  O token não é a senha do Console. A verificação dos certificados TLS permanece habilitada.

Confira ou renove a sessão:

```sh
oci session validate --profile JORNADA_B3 --auth security_token
oci session refresh --profile JORNADA_B3 --auth security_token
```

Se ela expirou e não puder ser renovada, faça login novamente com `oci session authenticate`,
selecionando a região e a tenancy da sua conta e o perfil `JORNADA_B3`.

O perfil Spring `oci` importa `~/.oci/java-eventos/kafka.properties`. Também é possível indicar
outro arquivo pela variável `OCI_KAFKA_CONFIG_FILE`. O exemplo sem segredos está em
[`config/oci-kafka.properties.example`](../config/oci-kafka.properties.example).

Para configurar outro Mac, copie o exemplo para fora do repositório e preencha com os dados da
própria conta. Copie o usuário SASL e o bootstrap server das configurações Kafka do stream pool
no Console. Use o `messagesEndpoint` da fila, não o endpoint administrativo Queue. A criação e a
permissão de acesso aos recursos devem ser feitas na conta OCI antes de iniciar as aplicações.

Restrinja o arquivo privado ao próprio usuário (`chmod 600`). Se o token contiver aspas ou barras
invertidas, escape primeiro para JAAS e depois para o formato Java `.properties`. Não envie esse
arquivo, `~/.oci`, chaves ou tokens a alunos, GitHub ou releases. Um token exposto deve ser revogado.

## Quem chama quem

Na aula 1, `OrderController` chama `SubmitOrderUseCase`, que depende de `OrderCommandPublisher`.
O Spring escolhe `RabbitOrderCommandPublisher` no modo local ou `OciOrderCommandPublisher` no OCI.

No OCI, o agendador chama `OciOrderWorker`, que recebe uma `Delivery` pela porta
`OrderCommandReceiver`. `OciOrderCommandReceiver`, em `infra`, traduz `GetMessages` para essa
porta. O worker chama `AcceptOrderUseCase`; esse caso de uso publica pelo `KafkaOrderEventPublisher`.
Somente após a confirmação do Streaming o worker confirma o comando via `DeleteMessage`.
Se houver falha, o comando permanece sem confirmação para reentrega. As assinaturas de Risco e
Auditoria usam grupos independentes.

Na aula 2, o caminho de negócio não muda: `LabController` → `CreateOrderUseCase` → ordem e Outbox
na mesma transação → `OutboxScheduler` → `PublishPendingEventsUseCase` → `KafkaOrderEventPublisher`
→ Streaming → `PositionConsumer` → `ApplyOrderAcceptedUseCase` → Inbox e posição na mesma transação.
O Outbox só é marcado após a confirmação do broker. Reenvios continuam deduplicados pela Inbox.

O domínio não importa OCI, Kafka, RabbitMQ ou Spring. Os controllers usam portas/casos de uso,
e os presenters mantêm os contratos externos. O SDK OCI fica em `infra`.

## Diferenças que importam na aula

- No OCI, os streams já devem existir. A aplicação não tenta criar tópicos nem alterar partições.
- O laboratório OCI usa uma partição por stream, enquanto a topologia local usa três.
- OCI Streaming não implementa transações Kafka nem produção idempotente. Somente o perfil OCI
  desativa `enable.idempotence`; confirmação do broker, entrega ao menos uma vez e Inbox continuam.
- O Queue não é AMQP: o adaptador usa HTTPS e confirma somente após publicar o evento.
  Na aula 1 ainda pode haver evento duplicado se ocorrer falha entre publicação e confirmação;
  a Inbox da aula 2 demonstra a proteção contra duplicatas.
- Os grupos OCI têm sufixo `-oci`, separados dos grupos locais. Grupos existentes retomam seus
  offsets confirmados; grupos novos leem o histórico retido. Use streams com eventos do contrato
  da respectiva aula. Mensagens técnicas de preparação não são CloudEvents de negócio.
- Neste Mac, os grupos foram inicializados após as mensagens técnicas da preparação e validados
  com eventos Java reais. Não altere os grupos para reler mensagens técnicas ainda retidas.
- Queue permite DLQ após o limite de entregas configurado. Mensagens inválidas não são descartadas
  silenciosamente pelo worker. Investigue a causa no Console antes de reenviar.

## Verificar as aplicações

```sh
mvn -B verify
python3 scripts/smoke.py --aula 1 --base-url http://localhost:8180/api
python3 scripts/smoke.py --aula 2 --base-url http://localhost:8182/api
```

Na aula 1, confira também os logs: o mesmo ID deve aparecer em `OCI_QUEUE_CONFIRMADO`,
`GRUPO=RISCO` e `GRUPO=AUDITORIA`. Na aula 2, o smoke testa rollback, Outbox pausado,
convergência e três reenvios sem aumentar novamente a posição. Para o modo local, omita `--base-url`.

Os testes criam dados de laboratório e não fazem limpeza destrutiva. Use Ctrl+C ao terminar as
aplicações; o polling Queue continua consumindo requisições enquanto a aula 1 OCI estiver aberta.
Queue tem franquia de requisições e Streaming cobra por uso; acompanhe créditos e custos no Console.

Referências oficiais: [Kafka na OCI](https://docs.oracle.com/en-us/iaas/Content/Streaming/Tasks/kafkacompatibility_topic-Configuration.htm),
[limitações do Streaming](https://docs.oracle.com/en-us/iaas/Content/Streaming/Tasks/kafkacompatibility.htm),
[mensagens Queue](https://docs.oracle.com/en-us/iaas/Content/queue/messages.htm),
[sessão CLI](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/clitoken.htm).
