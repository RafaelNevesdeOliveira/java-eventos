# Orientações do professor

## Preparação

Faça o download e o build antes do encontro. Em máquina corporativa, confirme autorização de execução das imagens, acesso aos registries e portas livres. Cada dupla usa sua própria instância local. Verifique `podman compose ps` e execute `python3 scripts/smoke.py --aula 1` em `aula-1` ou `python3 scripts/smoke.py --aula 2` em `aula-2`. Para o primeiro bloco da aula 2, ative o perfil `contratos` e execute `python3 scripts/contratos.py`.

As aulas têm três blocos planejados de 60 minutos. Se os intervalos precisam caber nas três horas corridas, desconte-os do tempo de exposição e reserve a maior parte do último bloco para execução. Os fontes estão completos como referência; conduza a leitura progressivamente e cobre as extensões dos roteiros como implementação do aluno.

## Leitura da arquitetura

Apresente `ordens-domain`, os casos de uso em `application`, as implementações em `adapters` e a composição em `infrastructure`. Use [ARQUITETURA.md](ARQUITETURA.md) como mapa. As transações são delimitadas pelos casos de uso via `TransactionBoundary` e executadas por `SpringTransactionBoundary`, mantendo domínio e aplicação sem Spring.

## Gabarito da aula 1

`CriarOrdem` e `CancelarOrdem`: comandos. `OrdemAceita` e `OrdemRejeitada`: eventos. `ConsultarPosicao`: query. O nome sozinho não define a semântica, mas expressa a intenção/fato/consulta neste contexto.

Fila: os dois workers competem pelas entregas. Kafka: grupos diferentes mantêm offsets próprios e recebem o log independentemente. Dentro de um mesmo grupo, cada partição tem um consumidor ativo; três partições limitam o paralelismo útil daquele grupo. Um tópico com três partições não produz ordenação global. Chaves iguais são roteadas à mesma partição com o particionador padrão e o número de partições constante; aumentar partições pode mudar esse mapeamento.

O worker confirma o envio Kafka antes de retornar e permitir o ack RabbitMQ. Pode morrer entre os dois passos. Isso pode duplicar eventos, mesmo com `enable.idempotence=true` no produtor Kafka. A fila é durável e os comandos usam persistência padrão do `RabbitTemplate`; a infraestrutura é de um nó e não simula tolerância à perda do disco.

O envio ao RabbitMQ usa publisher confirm e retorno obrigatório: HTTP 202 só sai após confirmação e ausência de retorno de roteamento. Isso confirma aceitação pelo broker, não conclusão da ordem. Timeout HTTP pode ocorrer com comando já aceito; a aula 1 ainda não implementa idempotência na API.

## Gabarito da aula 2

`ordens` e `outbox` são gravadas na mesma transação PostgreSQL. O relay consulta lote com `FOR UPDATE SKIP LOCKED`, envia aguardando ack e atualiza `publicada_em`. Se falhar antes do commit SQL, as linhas continuam pendentes; um envio já aceito pode ser repetido. A transação mantém locks durante I/O Kafka por simplicidade didática; em produção, avaliar claim/lease, timeout, controle de tentativas, métricas, retenção, CDC e custo de polling.

O consumer grava a inbox com restrição única, atualiza a posição e confirma a transação SQL antes do retorno do listener. Kafka pode reenviar após um crash: a inbox impede aplicar novamente o mesmo evento naquele consumidor. `source + id` é a identidade CloudEvents; o nome do consumidor permite efeitos independentes.

A garantia é **at least once na entrega, com efeito idempotente na projeção SQL**. Não é exactly once global. Idempotência no produtor Kafka não torna o par PostgreSQL/Kafka uma transação única. Chamadas HTTP externas e outros efeitos não transacionais precisam de tratamento próprio. A API de criação também não deduplica requisições do cliente: nova chamada cria uma ordem nova.

O consumer não descarta silenciosamente mensagens após um número pequeno de tentativas. Erros operacionais são retentados; mensagens inválidas podem bloquear partições. Tratamento de poison messages e DLQ é matéria da aula 6. O laboratório usa o mesmo banco para escrita, inbox e leitura para reduzir infraestrutura, mas as tabelas têm papéis distintos; separação física não é requisito para introduzir projeções.

Event Carried State Transfer: campos suficientes para o efeito local, sem callback ao produtor. Notification: referência/identidade leva o consumidor a consultar mais dados. A consulta pode observar estado posterior ao evento, além de criar dependência de disponibilidade.

Schema Registry: o exercício configura BACKWARD e usa um modelo fechado (`additionalProperties: false`) no v1. Adicionar propriedade opcional é compatível para o novo esquema ler dados antigos; o consumidor antigo pode rejeitar o campo novo. Não alegar FULL. O script verifica a resposta real do registry para os exemplos. A aplicação verifica campos e valores em Java, sem executar integralmente o JSON Schema v1, e não carrega schemas do registry automaticamente.

## Evidências para avaliação

| Aula | Evidência | Resultado esperado |
|---|---|---|
| 1 | Classificação e mapa de contextos | Intenção, fato e consulta diferenciados |
| 1 | Logs e painel RabbitMQ | Dois consumidores, uma entrega normal por comando |
| 1 | Risco, Auditoria e novo grupo | Assinatura independente e leitura histórica |
| 1 | Partição/offset/chave | Ordenação explicada no escopo correto |
| 2 | Registro e evolução de contrato | Mudança compatível aceita e incompatível rejeitada |
| 2 | Falha simulada | Ordem e outbox revertidas juntas |
| 2 | Relay pausado e retomado | Escrita antes da leitura; convergência observável |
| 2 | Reenvio com o mesmo ID | Inbox única e quantidade preservada |
| 2 | Discussão de falhas | Janelas de duplicação e limite de exactly once reconhecidos |

## Diagnóstico

- Podman sem conexão: confirme `podman machine list` e inicie a VM no macOS/Windows.
- Falta de provedor: instale Docker Compose ou podman-compose e confira `podman compose version`.
- Containers reiniciando: leia logs de `kafka`, `rabbitmq`, `postgres` e do app. Confirme memória disponível na VM.
- IDE não conecta ao Kafka: use `localhost:19092`; entre containers, `kafka:9092`.
- Porta ocupada: pare a aplicação correspondente no host/container antes de iniciar a outra forma de execução.
- Projeção ainda não apareceu: aguarde o relay, veja `/api/lab/status`, logs e saúde Kafka.
- Reenvio retornou 404: use o ID do evento, não o ID da ordem.
- Registry não responde: confirme perfil `contratos`, saúde Kafka e logs `schema-registry`.
