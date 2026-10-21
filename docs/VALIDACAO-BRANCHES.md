# Validação da organização por branches — 21/10/2026

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

Os comandos do roteiro usam Podman. A validação funcional desta preparação foi executada em Docker com o mesmo Compose e Dockerfiles, por causa do erro de armazenamento da VM Podman registrado em `VALIDACAO.md`. A execução integral com Podman nesta máquina não foi confirmada. Os comandos de instalação foram conferidos na documentação oficial e não são uma instalação automática na máquina de cada aluno.

Os guias das duas aulas ficam disponíveis para leitura; os fontes da aplicação da segunda aula só aparecem em `aula-2`. O arquivo Git bundle é entregue com as branches para permitir clone e troca sem uma hospedagem remota.
