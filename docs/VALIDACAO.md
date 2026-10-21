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

## Histórico: limitação inicial do teste com Podman

Na validação inicial, o Podman construiu as imagens e iniciou a infraestrutura. Após reiniciar a VM, os containers apresentaram erro de armazenamento `readlink .../containers/storage/overlay: invalid argument`. A execução completa foi validada usando **Docker com o mesmo Compose**. Nessa etapa inicial, a execução integral na VM Podman desta máquina não foi confirmada; não foi feito reset/prune do armazenamento existente. A revisão Clean Architecture foi verificada no Docker.

A máquina Podman foi configurada com 4 GB para o laboratório. Os testes deixaram este projeto em execução no Docker. Antes de usar as mesmas portas no Podman, encerre o projeto no Docker, a partir da raiz do laboratório:

```sh
docker compose --profile contratos down
```

Esse comando preserva os volumes. A investigação posterior e a recuperação sem reset estão descritas na seção seguinte. Não use reset/prune como tratamento automático de erros de armazenamento.

## Limites das evidências

Estes são testes funcionais locais com um nó por serviço. Não demonstram tolerância à perda de disco, alta disponibilidade, exactly once global ou desempenho em produção.


## Recuperação e validação no Podman — 21/10/2026

Ambiente: macOS/Apple Silicon, Podman cliente e servidor 5.8.1, VM existente `podman-machine-default` com applehv, modo rootless e 4 GiB. Provedor Docker Compose 2.27.0 conectado à API do Podman; o Docker Engine não foi usado nestes testes.

### Diagnóstico e preservação

- Reproduzido o erro `readlink /var/home/core/.local/share/containers/storage/overlay: invalid argument` ao listar imagens.
- Identificadas três camadas com os arquivos `link` e `lower` de tamanho zero. Uma camada da imagem antiga da aula 1 também continha `/app/app.jar` de tamanho zero. Restaurar somente os links não recuperaria esse executável.
- A checagem `podman system check`, sem `--repair` nem `--force`, confirmou os danos. Durante a checagem, novas sessões do usuário `core` aguardaram o lock porque `/etc/profile.d/docker-host.sh` executa `podman info` na inicialização do shell. O acesso administrativo permitiu acompanhar o diagnóstico sem alterar esse perfil.
- Backup dos metadados, das três camadas afetadas e dos cinco volumes originais em `/var/home/core/podman-recovery-20261021-133648`, com cópia fora da VM. O arquivo `volumes-before.tar.gz` passou na conferência gzip.
- Com a API parada temporariamente e nenhum container em execução, os seis arquivos de links foram reconstruídos a partir dos symlinks e da cadeia de pais ainda existentes. As demais cadeias foram conferidas antes da gravação. Nenhum payload de volume foi alterado pelo reparo.
- As duas imagens das aplicações foram recompiladas com `podman compose build --no-cache`. Os containers das aplicações foram recriados. As quatro referências de imagens antigas ligadas ao JAR truncado foram removidas por `podman rmi`, sem `--force`, depois da substituição. A camada truncada deixou de existir.
- Não houve reset da VM, prune, `down -v` nem exclusão de qualquer volume original. O Elasticsearch de outro projeto permaneceu parado e sem intervenção.

O truncamento é compatível com uma interrupção de gravações, mas sua causa original não foi comprovada. A checagem também encontrou alteração de mtime de `/run` em uma camada de imagens antigas `ubi9/python-311` e `search-ia`, externas ao laboratório. Essas imagens foram preservadas; a validação das aulas não equivale a declarar todo o armazenamento da VM livre de alertas.

### Resultados das aulas

| Verificação | Resultado no Podman |
|---|---|
| Aula 1, build sem cache e startup | RabbitMQ, Kafka e aplicação iniciados |
| Aula 1, smoke | HTTP 202 válido e HTTP 400 inválido |
| Aula 1, mensageria | Dois consumidores RabbitMQ; mesmo ID nos grupos Risco e Auditoria |
| Aula 1, histórico Kafka | Novo grupo leu evento retido com partição e offset |
| Aula 1, testes pelo estágio `build` | Domínio + aula 1: oito testes passando |
| Aula 2, build sem cache e startup | PostgreSQL, Kafka, aplicação e Schema Registry iniciados |
| Aula 2, smoke | Rollback de ordem + Outbox; pausa/retomada; três reenvios mantendo quantidade 10 |
| Aula 2, contratos | Campo opcional aceito sob BACKWARD e troca de tipo rejeitada com HTTP 409 |
| Aula 2, testes pelo estágio `build` | Domínio + aula 2: 15 testes passando |
| Aula 2, Kafka parado/retomado | HTTP 201 e Outbox pendente; após recuperação, publicação e posição com quantidade 7 |

As execuções usaram as branches correspondentes, encerrando a aula 1 antes de iniciar a aula 2. Os dados fictícios novos dos testes foram acrescentados aos volumes existentes.

Depois dos testes, os containers foram encerrados e a VM foi parada e iniciada normalmente, sem reset. A API da aula 2 voltou a iniciar com as mesmas duas ordens, duas linhas de Outbox, duas identidades Inbox, nenhuma pendência e posições idênticas. Os JARs das duas aplicações permaneceram com conteúdo (aproximadamente 47 e 45 MB), e um novo grupo voltou a ler o evento da aula 1 em partição 0/offset 0. Todos os cinco volumes originais conservaram seus nomes; os volumes anônimos adicionais criados pelas imagens também foram preservados.

Referências da investigação: [relato no projeto Podman sobre arquivos de lower vazios](https://github.com/podman-container-tools/podman/issues/19090) e [semântica de `podman system check`](https://docs.podman.io/en/latest/markdown/podman-system-check.1.html). O reparo manual foi específico deste diagnóstico e não é uma etapa do roteiro dos alunos.

## Revisão das quatro camadas com Maven em 21 de outubro de 2026

Os pacotes foram reorganizados em controller, domain, infra e presenter, com Javadocs em cada classe, record e interface. Os casos de uso e portas ficam em domain; o presenter prepara os contratos e respostas. URLs, nomes de campos, tabelas e destinos de mensageria foram preservados.

`mvn clean verify`, com JDK 25 e Maven 3.9.9, passou: 2 testes em main; 11 em aula-1; 30 na raiz de aula-2 (2 de domínio, 9 do módulo aula1 e 19 do módulo aula2). O total selecionando domínio + aula2 é 21. Os testes novos verificam contratos HTTP e a separação entre controllers e infraestrutura, além da independência de domain e presenter. As contagens e validações anteriores acima descrevem as revisões em que foram executadas.

Na mesma revisão, as duas aplicações foram iniciadas com `mvn -pl aula1 spring-boot:run` e `mvn -pl aula2 spring-boot:run`, usando repositório Maven isolado. RabbitMQ, Kafka e PostgreSQL executaram na VM Podman rootless existente de 4 GiB. O smoke da aula 1 passou e os logs registraram o mesmo fluxo recebido por Risco e Auditoria. O smoke da aula 2 passou com entrada inválida, rollback de ordem + Outbox, relay pausado e retomado, convergência da projeção e três reenvios sem duplicar a posição. Não houve migração de tabelas, reset de VM ou exclusão de volumes. O teste de indisponibilidade do Kafka e o Schema Registry não foram reexecutados nesta revisão; suas evidências anteriores permanecem acima.
