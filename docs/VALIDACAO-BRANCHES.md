# Validação da organização por branches — 21/10/2026

> Registro histórico da entrega anterior. A validação da integração OCI e do modo local na versão 1.1.0 está em [VALIDACAO-OCI.md](VALIDACAO-OCI.md). O pacote atual usa a branch `feat/oci-queue-streaming` e inclui quatro branches dos alunos.

## aula-1

- Contém somente os módulos de domínio e da primeira aplicação; não há código do módulo `aula2`.
- Compose reduzido: somente RabbitMQ, Kafka e `aula1`; configuração aceita pelo provedor Compose.
- `mvn clean verify`: oito testes passando.
- Build da aplicação e início do ambiente isolado concluídos.
- `scripts/smoke.py --aula 1`: HTTP 202 para comando válido e HTTP 400 para entrada inválida.
- Logs confirmaram o mesmo evento recebido nos grupos Risco e Auditoria.
- Caminho de testes pela imagem intermediária, sem Java/Maven no host: oito testes passando.
- Containers encerrados preservando os volumes antes da transição para a próxima aula.

## aula-2

- Criada como evolução de `aula-1`, acrescentando `aula2`, PostgreSQL, Outbox, Inbox e contratos; o código da primeira aula é preservado.
- `mvn clean verify` da raiz: 21 testes passando.
- Compose e build da aula 2 validados com somente Kafka, PostgreSQL, aplicação `aula2` e Schema Registry em execução; RabbitMQ e `aula1` não foram iniciados.
- `scripts/smoke.py --aula 2`: validação, rollback físico de ordem + Outbox, pendência sob pausa, convergência na retomada e três reenvios sem efeito duplicado.
- `scripts/contratos.py`: campo opcional aceito sob BACKWARD e mudança incompatível rejeitada com HTTP 409.
- Testes pela imagem intermediária: domínio + aula 2, 15 testes passando.
- Containers encerrados preservando os volumes ao final. A pasta de trabalho ficou em `aula-2`.

## Método e limites

Os comandos do roteiro usam Podman. A validação funcional desta preparação foi executada em Docker com o mesmo Compose e Dockerfiles, por causa do erro de armazenamento da VM Podman registrado em `VALIDACAO.md`. Naquela preparação inicial, a execução integral com Podman nesta máquina não foi confirmada. A recuperação e a nova validação estão em `VALIDACAO.md`. Os comandos de instalação foram conferidos na documentação oficial e não são uma instalação automática na máquina de cada aluno.

Os guias das duas aulas ficam disponíveis para leitura; os fontes da aplicação da segunda aula só aparecem em `aula-2`. O arquivo Git bundle é entregue com as branches para permitir clone e troca sem uma hospedagem remota.


## Revisão para publicação no GitHub — 21/10/2026

Repositório da turma: [RafaelNevesdeOliveira/java-eventos](https://github.com/RafaelNevesdeOliveira/java-eventos).

- Estado inicial: `aula-2`, sem alterações pendentes, três branches locais e nenhum remoto configurado. O destino GitHub estava vazio.
- Revisados `COMECE-AQUI`, guias das duas aulas, README e orientações do professor contra os fontes, scripts e Compose existentes. Nenhuma mudança nos fontes Java ou na infraestrutura foi necessária.
- Acrescentados clone com `-b aula-1`, referências às duas aulas a partir de `main`, requisito de curl, distinção das mensagens apenas discutidas, instruções de entrega de evidências e commits do exercício da aula 2.
- Esclarecidos os totais de testes ao selecionar a aula 1 a partir de `aula-2` e o limite da validação Java em relação ao JSON Schema completo. Corrigida a seleção de aula no comando de smoke do professor.
- Java 25: `mvn verify` na raiz de `aula-1`, oito testes; na raiz de `aula-2`, 21 testes. Todos passaram.
- Docker, aula 1 isolada: build e Compose, smoke HTTP 202/400, fila com dois consumidores e o mesmo evento nos logs de Risco e Auditoria.
- Docker, aula 2 isolada: build e Compose, smoke com rollback de ordem + Outbox, pausa/retomada e três reenvios preservando quantidade 10; contratos com campo opcional aceito e mudança de tipo rejeitada com HTTP 409.
- Links Markdown locais e sintaxe Bash dos blocos de comandos revisados. As branches preservam a progressão `main` → `aula-1` → `aula-2`, sem reescrever os commits originais.

Esta revisão não repetiu todos os experimentos manuais, como a queda do Kafka ou a implementação individual dos exercícios. As evidências complementares anteriores permanecem registradas acima. Não houve nova validação integral com Podman nem alteração/reset da sua VM. Os containers usados na revisão foram encerrados preservando volumes.


## Revisão posterior com Podman

Em 21/10/2026, a VM existente foi recuperada sem reset nem exclusão de volumes. As duas aulas passaram no Podman 5.8.1: oito testes Java para domínio + aula 1, 15 para domínio + aula 2, smoke de ambas, fan-out e histórico Kafka, contratos e indisponibilidade/recuperação do broker. Consulte [VALIDACAO.md](VALIDACAO.md) para o diagnóstico, backups, preservação de dados e limites.
