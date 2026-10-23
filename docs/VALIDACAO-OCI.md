# Validação Java — OCI e Podman

Conferência realizada em 22/10/2026 em macOS, Java 25 e Maven, na região OCI São Paulo.

| Verificação | Local/Podman | OCI |
| --- | --- | --- |
| Aula 1: entrada válida HTTP 202 e inválida HTTP 400 | Passou | Passou |
| Aula 1: mesmo evento nos grupos Risco e Auditoria | Passou | Passou |
| Aula 1: comando confirmado após publicação | RabbitMQ | Queue/SDK Java |
| Aula 2: validação da entrada e rollback de ordem + Outbox | Passou | Passou |
| Aula 2: pausa/retomada do relay e convergência da posição | Passou | Passou |
| Aula 2: três reenvios mantendo a posição em 10 | Passou | Passou |
| Outbox sem eventos pendentes no final | Sim | Sim |
| Autenticação Streaming via Kafka SASL_SSL/PLAIN | Não se aplica | Passou |

`mvn -B install` concluiu **33 testes sem falhas**: 2 no domínio compartilhado, 12 na aula 1
e 19 na aula 2. Incluem limites da arquitetura, contratos HTTP, transações e confirmação Queue
somente após sucesso da publicação. A falha simulada no Streaming deixa o comando sem ack.

As aplicações foram iniciadas pelos quatro comandos de `scripts/iniciar.sh`, com as APIs locais
em 8080/8082 e OCI em 8180/8182. Os grupos Kafka OCI retomaram seus offsets depois de um reinício.
O perfil OCI da aula 1 inicializou sem RabbitMQ e sem a criação de tópicos locais.

O banco local continuou no schema `public`; o modo OCI usou `oci_aulas` no mesmo PostgreSQL/Podman.
Foram adicionados registros de teste, sem reset da VM, remoção de volumes ou exclusão de exercícios.

Uma conferência do segredo real contra os arquivos versionáveis não encontrou o Auth Token no
projeto. Os arquivos privados de autenticação no Mac têm permissão 600 e não fazem parte da entrega.

Esta é uma validação funcional de laboratório, sem teste de carga ou garantia de disponibilidade
da nuvem. O comportamento e os comandos para repetir os testes estão em [OCI.md](OCI.md).
