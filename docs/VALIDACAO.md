# Registro de validação — 21/10/2026

Este registro descreve o estágio completo disponível em `aula-2`. A separação por branches e a validação de cada etapa estão em [VALIDACAO-BRANCHES.md](VALIDACAO-BRANCHES.md).

## Revisão Clean Architecture

- Maven `clean verify` em Java 25: três módulos compilados (`ordens-domain`, `aula1`, `aula2`), com **21 testes passando**.
- Testes de invariantes do domínio, decisões dos casos de uso, conversão CloudEvents e dependências arquiteturais.
- ArchUnit comprova que domínio depende somente de Java e domínio; aplicação depende somente de Java, aplicação e domínio; adaptadores de entrada não chamam implementações de saída diretamente.
- Criação testada com repositórios dentro da mesma fronteira transacional; falha simulada propaga sem commit. Publicação testada com confirmação anterior à marcação e sem marcação sob falha do broker. Evento duplicado não aciona atualização de posição.
- Imagens de ambas as aplicações reconstruídas com o módulo de domínio compartilhado e iniciadas com o mesmo Compose em Docker.
- `scripts/smoke.py` passou na versão reorganizada: HTTP 202 na aula 1, HTTP 400 para entrada inválida, rollback físico de ordem e outbox sob HTTP 503, evento pendente durante pausa, convergência após retomada e três reenvios sem alterar a quantidade 10.
- `scripts/contratos.py` passou contra o Schema Registry real: campo opcional aceito sob BACKWARD e troca de tipo rejeitada com HTTP 409.
- Dados, tabelas e mensagens da versão anterior foram preservados. As consultas continuam lendo os registros existentes.

Os testes de unidade da fronteira transacional verificam a coordenação das operações; a prova de rollback físico está no cenário HTTP contra o PostgreSQL real.

## Histórico: verificações adicionais na versão inicial

Antes da reorganização, também foram verificados: dois consumidores na fila RabbitMQ e distribuição entre workers; evento recebido por Risco e Auditoria; grupo Kafka novo lendo histórico com partição/offset; cabeçalho Kafka `content-type: application/cloudevents+json`; Kafka parado com ordem aceita e evento pendente, seguido de convergência após recuperação. Esses cenários complementares não foram todos repetidos nesta revisão; os fluxos de regressão executados agora estão listados acima.

## Limitação do teste com Podman

Na validação inicial, o Podman construiu as imagens e iniciou a infraestrutura. Após reiniciar a VM, os containers apresentaram erro de armazenamento `readlink .../containers/storage/overlay: invalid argument`. A execução completa foi validada usando **Docker com o mesmo Compose**. A execução integral na VM Podman desta máquina não foi confirmada; não foi feito reset/prune do armazenamento existente. A revisão Clean Architecture foi verificada no Docker.

A máquina Podman foi configurada com 4 GB para o laboratório. Os testes deixaram este projeto em execução no Docker. Antes de usar as mesmas portas no Podman, encerre o projeto no Docker, a partir da raiz do laboratório:

```sh
docker compose --profile contratos down
```

Esse comando preserva os volumes. Resolver o armazenamento da VM Podman exige investigação separada; evite resetá-la sem avaliar os demais containers existentes.

## Limites das evidências

Estes são testes funcionais locais com um nó por serviço. Não demonstram tolerância à perda de disco, alta disponibilidade, exactly once global ou desempenho em produção.
