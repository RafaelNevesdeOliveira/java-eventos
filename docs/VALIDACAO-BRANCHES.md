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

## Método e limites

Os comandos do roteiro usam Podman. A validação funcional desta preparação foi executada em Docker com o mesmo Compose e Dockerfiles, por causa do erro de armazenamento da VM Podman registrado em `VALIDACAO.md`. A execução integral com Podman nesta máquina não foi confirmada. Os comandos de instalação foram conferidos na documentação oficial e não são uma instalação automática na máquina de cada aluno.

A aula 2 será validada e registrada na sua própria branch. Os guias das duas aulas ficam disponíveis para leitura; os fontes da aplicação da segunda aula só aparecem em `aula-2`.
