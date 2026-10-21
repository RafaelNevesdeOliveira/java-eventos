# Comece aqui — preparação do aluno

Este material acompanha a Jornada B3 de Arquitetura Orientada a Eventos. Você vai receber uma solução de referência em Java 25/Spring Boot 4.1.1, executá-la, seguir seu fluxo no código e implementar pequenas evoluções. Os dados e as ordens são fictícios. Não é preciso conta OCI nem Supabase para estes laboratórios locais.

## 1. O que instalar antes da aula

| Ferramenta | Para que será usada | Necessidade |
|---|---|---|
| curl | Enviar as requisições HTTP dos guias | Obrigatório para os exemplos no terminal |
| Git | Clonar o repositório e trocar de branch | Obrigatória |
| Podman Desktop/Podman | Executar aplicações, brokers e banco em containers | Obrigatória |
| Provedor Compose | Interpretar `compose.yaml` | Obrigatório; configure pelo Podman Desktop |
| VS Code ou IDE homologada | Navegar e alterar o código | Obrigatória para os exercícios de código |
| Python 3.10 ou superior | Checagem automatizada e exercício de contratos | Necessário para os scripts |
| Java 25 e Maven 3.6.3+ | Executar/testar pela IDE, fora de container | Opcionais no roteiro principal |

Instale o [Podman Desktop pelo guia do seu sistema](https://podman-desktop.io/docs/installation). Configure o [suporte a Compose](https://podman-desktop.io/docs/compose/setting-up-compose). Um provedor pode se chamar Docker Compose ou podman-compose: ele é quem lê o YAML, enquanto o motor continua sendo Podman. Docker Engine não é requisito.

Os blocos de comandos deste material usam **Bash**. No macOS, o Terminal com zsh também executa estes exemplos. No Windows, use **Git Bash** com Git e Podman disponíveis no PATH; não cole os blocos multilinha diretamente no PowerShell. Se o seu Python no Windows se chama `python`, substitua `python3` por `python` nos comandos. Você também pode executar as requisições de `requisicoes.http` por um cliente HTTP homologado.

Valide as instalações no terminal escolhido:

```sh
git --version
curl --version
podman --version
python3 --version
```

Feche e reabra o terminal depois da instalação se um comando não for encontrado. Reserve 4 GB de memória para a VM, quatro CPUs e espaço para imagens/volumes. Essa é uma recomendação para a turma, não uma garantia universal de consumo. O primeiro build precisa de internet para baixar imagens e dependências Maven.

## 2. Preparar o motor Podman

### macOS e Windows

Containers Linux usam uma VM. Primeiro veja se ela já existe:

```sh
podman machine list
```

**Se não houver nenhuma máquina**, crie e inicie:

```sh
podman machine init --memory 4096 --cpus 4
podman machine start
```

**Se a máquina já existir e estiver parada**, execute somente:

```sh
podman machine start
```

Se estiver em execução, siga adiante; não é necessário iniciar outra VM. Para alterar recursos de uma VM existente, use as opções suportadas pelo provedor no Podman Desktop e faça isso com ela parada. Não remova nem redefina a VM da sua empresa para este exercício.

### Linux

O Podman pode executar diretamente no sistema; pule a criação da VM. Use o motor configurado pela sua organização.

### Todos os sistemas

```sh
podman info
podman compose version
```

**Ponto de conferência:** `podman info` deve mostrar o servidor/motor sem erro de conexão, e o segundo comando deve informar a versão do provedor Compose. Resolva essa etapa antes de tentar subir a aplicação. Se o provedor não for encontrado, volte à configuração de Compose pelo Podman Desktop.

Referência: [máquinas Podman no macOS/Windows](https://docs.podman.io/en/latest/markdown/podman-machine-init.1.html).

## 3. Obter o repositório com as duas branches

O professor pode fornecer uma URL Git ou o arquivo **`Laboratorios_EDA_Aulas_1_e_2.bundle`**. Esse bundle contém o histórico e as branches; um ZIP comum de fontes não contém branches.

### Recebendo o bundle

Salve o arquivo em uma pasta, abra um terminal nessa pasta e execute:

```sh
git clone -b aula-1 ./Laboratorios_EDA_Aulas_1_e_2.bundle laboratorios-eda
cd laboratorios-eda
git branch -a
git branch --show-current
```

O clone deve terminar em `aula-1` e a listagem deve incluir `origin/aula-2`. O caminho do bundle é local, por isso esse clone não precisa de uma hospedagem Git. Ele não instala imagens nem dependências; o build ainda precisa dos downloads iniciais.

### Recebendo uma URL Git

O repositório oficial da turma é [java-eventos](https://github.com/RafaelNevesdeOliveira/java-eventos). Execute:

```sh
git clone -b aula-1 https://github.com/RafaelNevesdeOliveira/java-eventos.git laboratorios-eda
cd laboratorios-eda
git branch -a
git branch --show-current
```

**Esperado:** branch `aula-1`, com `origin/main`, `origin/aula-1` e `origin/aula-2` disponíveis. Não use `--single-branch`: a troca para a aula 2 depende da outra referência. O nome da pasta local é `laboratorios-eda`, mesmo que o repositório no GitHub se chame `java-eventos`.

O botão **Download ZIP** do GitHub baixa apenas os arquivos de uma branch, sem o histórico necessário para `git switch`. Prefira o clone acima ou o bundle fornecido pelo professor.

### Usando a pasta original do professor

Se você já recebeu a pasta com o repositório Git completo:

```sh
cd laboratorios-eda
git switch aula-1
git branch --show-current
```

A partir daqui, todos os comandos são executados **na raiz do repositório**, onde estão `pom.xml` e `compose.yaml`.

## 4. Como as branches estão organizadas

| Branch | Conteúdo | O que subir |
|---|---|---|
| `main` | Domínio inicial e orientação de entrada | Nenhuma aplicação |
| `aula-1` | `ordens-domain` + `aula1`; RabbitMQ e Kafka | `podman compose up -d --build aula1` |
| `aula-2` | Evolução da aula 1 com `aula2`, PostgreSQL e contratos | `podman compose up -d --build aula2` |

A branch `aula-2` preserva o código da primeira aula como referência. Usar o nome do serviço no comando de inicialização sobe somente aquela aplicação e suas dependências. Na aula 2, RabbitMQ e a aplicação da aula 1 não são necessários ao fluxo novo.

## 5. Entender os endereços

| Serviço | Acesso pelo computador do aluno | Credenciais locais |
|---|---|---|
| API aula 1 | `http://localhost:8080` | Sem autenticação neste laboratório |
| API aula 2 | `http://localhost:8082` | Sem autenticação neste laboratório |
| Painel RabbitMQ | `http://localhost:15673` | `aluno` / `aluno-local` |
| RabbitMQ para a IDE | `localhost:5673` | `aluno` / `aluno-local` |
| Kafka para a IDE | `localhost:19092` | Sem autenticação neste laboratório |
| PostgreSQL para a IDE | `localhost:5433`, banco `eda` | `aluno` / `aluno-local` |
| Schema Registry | `http://localhost:8085` | Sem autenticação neste laboratório |

Entre containers, os nomes são `rabbitmq:5672`, `kafka:9092` e `postgres:5432`. Dentro de um container, `localhost` se refere ao próprio container. A configuração do Compose já injeta os endereços corretos.

Não confunda RabbitMQ AMQP (`5673`) com seu painel web (`15673`). Não altere o Kafka para anunciar `localhost:9092`: o laboratório possui listeners interno e externo separados.

## 6. Como trabalhar sem perder suas alterações

Os guias das aulas indicam quando salvar as alterações do exercício em um commit. Se o Git pedir identidade, configure seu nome e e-mail reais no repositório antes de fazer o commit. Antes de trocar de branch, confira:

```sh
git status
```

Se houver trabalho pendente, salve-o na sua branch de exercício. Não use `git reset --hard` para conseguir trocar de aula. Branches de exercício com `git switch -c exercicios/aula-1` e `exercicios/aula-2` preservam as referências fornecidas pelo professor.

## 7. Encerrar antes de trocar de aula

Na aula 1:

```sh
podman compose down
```

Na aula 2, com o perfil de contratos:

```sh
podman compose --profile contratos down
```

Esses comandos encerram os containers/rede deste projeto, preservando os volumes. O nome do projeto Compose é o mesmo nas duas branches, então desligue uma aula **antes** de trocar para a outra. Branch Git não isola banco nem volumes: os dados persistem e cada exercício usa uma conta de exemplo própria.

Para apagar todos os dados **deste projeto**, o professor pode orientar o uso de `down -v`. Isso é diferente de apenas encerrar e não é necessário no percurso normal.

## 8. Se algo der errado

| Sintoma | Próximo passo |
|---|---|
| `podman: command not found` | Revise a instalação e reabra o terminal. |
| Erro conectando ao socket/servidor | Confira `podman machine list`, inicie a VM se necessário e repita `podman info`. |
| Compose não encontrado | Configure um provedor pelo Podman Desktop. |
| Porta já em uso | Encerre a instância anterior do laboratório antes de iniciar outra. Se estava em Docker, use `docker compose ... down` no mesmo projeto. |
| Imagem/dependência não baixa | Verifique internet, proxy, certificados e liberação dos registries com a organização. |
| Container encerra durante startup | Leia `podman compose logs --tail=100 NOME_DO_SERVICO`; confira memória disponível. |
| HTTP recusa conexão logo após `up` | A API ainda pode estar iniciando; acompanhe os logs até `Started Application`. |
| Erro de armazenamento `overlay` | Pare a atividade e peça apoio ao professor; não faça reset/prune global do Podman. |
| Troca de branch recusada | Leia `git status`, salve suas alterações e repita a troca. |

O registro de validação do professor está em [VALIDACAO.md da aula 2](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-2/docs/VALIDACAO.md). A VM Podman da preparação apresentou erro de armazenamento; as regressões funcionais foram executadas em Docker com o mesmo Compose. Isso não garante funcionamento em toda configuração corporativa. O roteiro do aluno usa Podman e exige a conferência do motor na etapa 2.

## Próximo passo

Abra [guia da aula 1](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-1/docs/AULA-1.md). Depois de concluir e salvar os exercícios, siga a transição descrita em [guia da aula 2](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-2/docs/AULA-2.md).

Depois de selecionar a branch da aula, os guias também ficam disponíveis na pasta `docs/` do clone. A branch `main` contém somente o domínio inicial e este guia de preparação.
