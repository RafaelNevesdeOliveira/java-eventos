# Crie e observe sua primeira fila no OCI

Este guia é para quem nunca criou uma fila na Oracle Cloud Infrastructure, ou OCI. Você vai criar um espaço para o laboratório, criar uma fila, enviar uma mensagem e entender como acompanhar seu processamento.

**Você não precisa fazer isso para começar localmente.** [O primeiro README](README.md) ensina a usar Podman sem créditos OCI. Aqui os recursos ficam na sua conta Oracle ou no ambiente individual autorizado pela organização. Não copie as credenciais de outra pessoa.

Se a fila do laboratório já foi criada na sua conta, procure-a no Console e reutilize-a. Não crie outra só para repetir o guia. Ler este arquivo não cria recursos nem inicia consumidores.

## 1 Entenda os nomes antes de clicar

| Nome na OCI | Significado neste exercício |
| --- | --- |
| Tenancy | A conta/ambiente Oracle Cloud da sua organização ou da sua conta de estudo |
| Region | Local onde o recurso será criado; anote e use a mesma região na configuração |
| Compartment | Uma divisão lógica para organizar recursos e permissões |
| Queue | A fila que guarda comandos até um consumidor recebê-los |
| Message | O conteúdo enviado à fila; no nosso caso, um JSON de comando |
| Consumer ou worker | O programa Java que lê e processa comandos |
| OCID | O identificador de um recurso; não é uma senha |
| Messages endpoint | O endereço HTTPS para publicar, receber e confirmar mensagens |
| DLQ | Fila de erros para mensagens que excederam as tentativas de entrega |

A fila não executa o código Java. Ela guarda e entrega as mensagens; o worker precisa estar rodando para processá-las.

## 2 Entre no Console e confira a região

1. Abra o [Console Oracle Cloud](https://cloud.oracle.com/) e entre na própria conta.
2. Confira a região no topo da página. O laboratório validado usou **Brazil East Sao Paulo**, código `sa-saopaulo-1`. Use uma região disponível e autorizada na sua conta.
3. Anote a região escolhida. Queue, Streaming e a configuração das aplicações deste exercício devem usar a mesma região.
4. Se for um ambiente corporativo, confirme com o administrador o compartment e as permissões para criar e usar os recursos. Não é necessário dar acesso de administrador a todos os alunos.

Se não possui conta, o [cadastro Oracle](https://signup.oraclecloud.com/) é uma etapa pessoal. Verificações, dados de pagamento, senha e aceitação dos termos são feitos por você. Não envie esses dados para o repositório ou para colegas.

## 3 Crie ou selecione o compartment

Se a organização já forneceu um compartment, selecione-o e pule a criação.

Se sua conta permite criar um:

1. No Console, procure **Compartments** na busca. Também pode acessar Identity and Security → Compartments.
2. Escolha o local indicado para o laboratório e clique em **Create compartment**.
3. Preencha Name com `jornada-b3-laboratorios` e Description com `Laboratorios das aulas 1 e 2`.
4. Confira o compartment pai e confirme a criação.
5. Aguarde ele aparecer. Se não aparecer de imediato no seletor de outro serviço, atualize a lista.

**Esperado:** um compartment que você consegue selecionar. Isso ainda não é a fila. [Referência de criação de compartments](https://docs.oracle.com/en-us/iaas/Content/Identity/compartments/To_create_a_compartment.htm).

## 4 Crie a fila

Na busca do Console, procure **Queues**. Confira a região e selecione o compartment do laboratório. Se já existir `jornada-b3-aula-01-ordens`, abra essa fila e confira sua configuração.

Para uma fila nova, clique em **Create queue**, escolha configuração personalizada quando necessário e use:

| Campo | Valor para o laboratório | O que você está escolhendo |
| --- | --- | --- |
| Name | `jornada-b3-aula-01-ordens` | Nome para encontrar a fila |
| Compartment | O compartment do laboratório | Onde organizar o recurso |
| Maximum retention period | 1 dia, equivalente a 24 horas | Por quanto tempo uma mensagem não confirmada pode ficar retida |
| Visibility timeout | 30 segundos | Tempo padrão para ocultar uma mensagem que foi entregue a um consumidor |
| Dead letter queue | Habilitada, com limite de 5 tentativas de entrega | Destino de mensagens que não conseguem ser processadas |
| Encryption | Oracle-managed key | Criptografia gerenciada pela Oracle |

Mantenha as demais opções padrão. Este exemplo usa o consumo padrão da fila, sem precisar ativar os novos consumer groups do Queue. Os grupos Risco/Auditoria da aula são grupos **Kafka/Streaming**, não grupos Queue.

Confira os valores e clique em **Create queue**. Espere o estado **Active**. A retenção de Queue não pode ser alterada depois da criação; confira o valor antes de confirmar. [Referência da Oracle para criar a fila](https://docs.oracle.com/en-us/iaas/Content/queue/queue-create.htm).

**Active significa que a fila está disponível. Não significa que um worker Java está rodando.**

## 5 Guarde os dois dados usados pela aplicação

Nos detalhes da fila, copie:

- **OCID:** vai em `lab.oci.queue-id`.
- **Messages endpoint:** vai em `lab.oci.queue-endpoint`.

O endpoint deve ser o de mensagens mostrado para essa fila. Não substitua pelo endereço da página do Console nem pelo endpoint administrativo Queue. Os valores abaixo são apenas marcadores:

```properties
lab.oci.queue-id=COLE_O_OCID_DA_SUA_FILA
lab.oci.queue-endpoint=COLE_O_MESSAGES_ENDPOINT_DA_SUA_FILA
```

Guarde a configuração no arquivo privado descrito na etapa 9. O OCID identifica um recurso; tokens, chaves privadas e senhas são segredos que dão acesso. O exemplo do projeto não contém nenhum desses valores reais.

## 6 Envie uma mensagem de teste pelo Console

Para observar a mensagem esperando, deixe a aplicação Java OCI parada. Não é necessário iniciá-la para este primeiro teste.

1. Abra a fila no Console.
2. Clique em **Send message**.
3. No conteúdo da mensagem, cole este JSON de comando. Não adicione um campo externo chamado `content`.

```json
{"ordemId":"22222222-2222-4222-8222-222222222222","contaId":"iniciante-oci-001","ativo":"PETR4","quantidade":10}
```

4. Confirme o envio uma vez.
5. Abra **Monitoring** para acompanhar a quantidade de mensagens. Gráficos podem levar algum tempo para refletir a alteração.

Usei o formato que o Java da aula entende. Mensagens como `ola mundo` ou JSONs sem `ordemId` não são comandos válidos para esse worker. Se repetir o teste, use um novo UUID e outra conta.

O envio usa uma requisição do serviço. Não deixe um envio em laço ou repetição automática. [Publicação de mensagens pela OCI](https://docs.oracle.com/en-us/iaas/Content/queue/publish-messages.htm).

## 7 Veja a mensagem sem confundir consulta e confirmação

Na aba **Messages**, existe **Poll for messages**. Essa ação faz uma requisição para obter mensagens; não é apenas um gráfico de monitoramento.

Se quiser experimentar, faça uma consulta manual. A mensagem pode ficar invisível por um período, e a contagem de entregas aumenta. Depois do tempo de visibilidade, ela pode reaparecer se não foi confirmada. Consultar repetidamente pode levar a mensagem à DLQ ao ultrapassar o limite configurado.

**Visualizar uma mensagem no Console não confirma seu processamento pelo Java.** No fluxo da aula, o Java usa `DeleteMessage` para confirmar somente depois de publicar o evento no Streaming. A documentação distingue o polling do Console do consumo pela aplicação. [Como funciona a consulta de mensagens](https://docs.oracle.com/en-us/iaas/Content/queue/consume-messages-queue.htm).

Para acompanhar sem interferir nas entregas, prefira **Monitoring**:

| Métrica | O que observar |
| --- | --- |
| MessagesInQueueCount | Mensagens visíveis ou em voo, conforme o filtro escolhido |
| MessagesCount | Volume de mensagens por operação, como envio e recebimento |
| RequestSuccess | Sucesso das requisições por operação |
| ConsumerLag | Tempo de atraso das mensagens que aguardam processamento |

Um gráfico sem dados logo após a criação não prova erro. Confira o período selecionado, a região, o compartment e se já houve atividade. [Métricas oficiais Queue](https://docs.oracle.com/en-us/iaas/Content/queue/metrics.htm).

## 8 Para executar a aula completa também prepare Streaming

**Queue e Streaming são dois serviços diferentes.** A fila recebe comandos; o Streaming recebe eventos para Risco/Auditoria e para a projeção da aula 2. Criar somente a fila não basta para iniciar toda a integração Java.

Se o ambiente já tem um pool e streams autorizados, reutilize-os. Para um laboratório novo:

1. Procure **Streaming** no Console e selecione o mesmo compartment e região.
2. Em **Stream pools**, crie `jornada-b3-aulas-1-e-2`.
3. Para acesso do computador pela internet, escolha **Public endpoint** e criptografia gerenciada pela Oracle. O acesso continua exigindo autenticação; público não significa anônimo. Em ambiente corporativo, siga a rede aprovada pela organização.
4. Deixe **Auto create topics** desativado: vamos criar os dois streams explicitamente.
5. Dentro desse pool, crie `jornada-b3-aula-01-ordens-aceitas-v1` e `jornada-b3-aula-02-ordens-aceitas-v1`, cada um com **1 partição e 24 horas de retenção**.
6. Aguarde os recursos ficarem ativos.
7. Nas **Kafka Connection Settings** do pool, copie o bootstrap server e o usuário de conexão para o seu usuário. Não invente o domínio ou remova partes desse nome.

Escolha o serviço **OCI Streaming** usado neste projeto, não outro produto gerenciado de cluster Kafka. [Criação de stream pools](https://docs.oracle.com/en-us/iaas/Content/Streaming/Tasks/creating-stream-pools.htm) e [limites de partições e retenção](https://docs.oracle.com/en-us/iaas/Content/Streaming/Concepts/streamingoverview_topic-Limits_on_Streaming_Resources.htm).

Não publique JSONs de teste arbitrários diretamente nos streams das aulas. O consumidor da aula 2 espera o contrato CloudEvents emitido pela API Java. Para testar o fluxo, envie uma ordem para a API.

## 9 Configure os dois tipos de autenticação no computador

Esta etapa exige JDK 25, Maven e OCI CLI instalados. As instruções de instalação da CLI estão na [documentação oficial](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm).

### Sessão temporária para Queue

No terminal, autentique no perfil usado pelo projeto. O exemplo usa São Paulo; ajuste se seu ambiente estiver em outra região:

```sh
oci session authenticate --region sa-saopaulo-1 --profile-name JORNADA_B3
oci session validate --profile JORNADA_B3 --auth security_token
```

A CLI abre o navegador para você entrar. Se o perfil já existe, confira a sessão antes de autenticar novamente; siga a solicitação da CLI para renovação e não substitua um perfil de outra finalidade.

Para tentar renovar uma sessão ainda renovável:

```sh
oci session refresh --profile JORNADA_B3 --auth security_token
```

A senha do Console não vai no código. Queue usa a sessão temporária do perfil, lida pelo SDK Java. Ela tem validade limitada. [Sessões OCI CLI](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/clitoken.htm).

### Auth Token para Kafka e Streaming

Na área do seu usuário no Console, procure **Auth tokens**, seguindo o caminho de credenciais do seu domínio de identidade. Gere um token individual para o laboratório e guarde-o em local privado. Ele pode ser exibido apenas na criação; não coloque seu valor no Git, em prints ou em mensagens para a turma.

O Auth Token não é a senha de login do Console. Ele será usado pelo Kafka com `SASL_SSL` e `PLAIN`. O usuário SASL inclui a conta, o domínio/usuário e o stream pool; copie o formato indicado pelo Console. [Autenticação Kafka oficial](https://docs.oracle.com/en-us/iaas/Content/Streaming/Tasks/kafkacompatibility_topic-Configuration.htm).

### Arquivo privado do projeto

Na raiz do projeto, no Mac:

```sh
mkdir -p ~/.oci/java-eventos
cp -n config/oci-kafka.properties.example ~/.oci/java-eventos/kafka.properties
chmod 600 ~/.oci/java-eventos/kafka.properties
```

O `cp -n` preserva um arquivo de destino que já exista. Nesse caso, edite a configuração atual em vez de substituir suas credenciais.

No Windows PowerShell, este bloco também preserva uma configuração existente:

```powershell
New-Item -ItemType Directory -Force "$env:USERPROFILE\.oci\java-eventos"
$config = "$env:USERPROFILE\.oci\java-eventos\kafka.properties"
if (Test-Path -LiteralPath $config) {
    Write-Host 'Configuracao existente preservada; edite o arquivo atual.'
} else {
    Copy-Item .\config\oci-kafka.properties.example $config
}
```

Edite o arquivo de destino, fora do projeto. No Windows, restrinja a leitura ao próprio usuário nas propriedades de segurança. Preencha os campos do exemplo:

| Propriedade | Valor que você deve copiar |
| --- | --- |
| spring.kafka.bootstrap-servers | Bootstrap server do stream pool, com porta 9092 |
| username dentro de sasl.jaas.config | Usuário SASL indicado para sua identidade/pool |
| password dentro de sasl.jaas.config | Seu Auth Token, não a senha do Console |
| lab.oci.profile | JORNADA_B3 |
| lab.oci.queue-id | OCID da fila |
| lab.oci.queue-endpoint | Messages endpoint da fila |
| lab.oci.stream-aula1 | Nome do stream da aula 1 |
| lab.oci.stream-aula2 | Nome do stream da aula 2 |

Não deixe marcadores como `AUTH_TOKEN` no arquivo final. Preserve a estrutura JAAS do exemplo. Aspas e barras invertidas em valores precisam ser escapadas para JAAS e depois para o formato `.properties`; se tiver dúvida, peça apoio sem revelar o token.

**Não copie a pasta `.oci` para dentro do repositório.** [Detalhes técnicos da configuração](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/feat/oci-queue-streaming/docs/OCI.md).

## 10 Ligue o consumidor somente quando for fazer o experimento

No Mac, dentro do projeto:

```sh
./scripts/iniciar.sh 1 oci
```

Mantenha esse terminal aberto e espere `Started Application` e a atribuição das partições aos grupos. O processo começa a consultar a fila, inclusive se ela estiver vazia. Ele pode consumir a mensagem válida enviada anteriormente pelo Console.

Em outro Terminal do Mac ou Git Bash do Windows, envie uma nova ordem pela API OCI da aula 1:

```sh
curl -i -X POST http://localhost:8180/api/ordens \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"iniciante-oci-http-001","ativo":"PETR4","quantidade":10}'
```

**Esperado:** HTTP 202. No terminal Java, procure `OCI_QUEUE_CONFIRMADO` e o mesmo evento em `GRUPO=RISCO` e `GRUPO=AUDITORIA`. No Console, a quantidade de mensagens pode voltar rapidamente a zero porque o Java já confirmou a entrega.

Para iniciar as aplicações pelo PowerShell do Windows e para executar a segunda aula, siga os blocos específicos em [docs/OCI.md](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/feat/oci-queue-streaming/docs/OCI.md). A validação OCI desta versão foi feita no Mac; não foi feita execução OCI no Windows.

## 11 Deixe a fila sem consultas ao terminar

No terminal Maven OCI, pressione **Ctrl+C**. Faça isso em cada aplicação OCI que tiver iniciado. Feche também qualquer ferramenta de teste que esteja fazendo polling.

A fila pode continuar com estado Active no Console: isso indica disponibilidade do recurso, não um consumidor ligado. Abrir o Console não inicia o worker Java. Não existe neste projeto um serviço que ligue a aplicação automaticamente ao visitar a página.

Queue cobra por requisições, e consultas repetidas também contam; não deixe o Maven OCI aberto sem necessidade. Streaming tem cobrança separada por uso e armazenamento. Encerrar a aplicação interrompe seu polling, mas não apaga recursos nem eventos retidos. [Preços oficiais](https://www.oracle.com/cloud/price-list/).

Não exclua filas, mensagens ou streams para simular uma pausa. Quando quiser voltar ao laboratório, execute o comando de início novamente. Este guia não configura desligamento automático, limite financeiro rígido nem monitoramento agendado.

## Se não conseguir avançar

| Problema | Primeiro ponto a conferir |
| --- | --- |
| Não encontro a fila | Região e compartment selecionados |
| Não aparece Create queue ou há 403 | Permissões fornecidas pelo administrador |
| A fila está Active, mas não processa | O worker Java precisa estar rodando e ter Queue e Streaming configurados |
| A mensagem some e reaparece | Visibilidade venceu sem confirmação; observe logs e autenticação |
| A mensagem foi para a DLQ | Falhas repetidas ou consultas manuais aumentaram as entregas; corrija a causa antes de reenviar |
| A API retorna 202, mas não aparece o evento | 202 confirma o comando enfileirado; observe o worker e a publicação Streaming |
| Erro 401 no Queue | Sessão CLI expirada ou perfil/configuração incorretos |
| Kafka authentication failed | Confira Auth Token, domínio/usuário SASL e stream pool |
| Tópico não encontrado | O stream precisa existir no pool e ter exatamente o nome configurado |
| Pouca atividade no gráfico | Verifique a janela de tempo e aguarde a atualização; o consumo pode ter sido rápido |

Você concluiu o experimento quando consegue localizar a fila, explicar seus parâmetros, publicar um comando válido, observar o Java processá-lo e encerrar o consumidor ao terminar.
