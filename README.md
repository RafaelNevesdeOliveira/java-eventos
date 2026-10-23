# Aulas 1 e 2 — início rápido

Java 25 + Maven + Podman. Comece localmente, sem usar créditos OCI.

## 1 Prepare o ambiente

[Baixe o ZIP completo](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.1.0/Laboratorios_EDA_Aulas_1_e_2_Java25.zip), extraia e abra o terminal na pasta **Entrega_EDA_Alunos**.

**Windows — PowerShell:**

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\preparar-windows.ps1
```

**Mac — Terminal:**

```sh
bash ./preparar-mac.sh
```

Aguarde **PRONTO** e entre na pasta do projeto indicada no final. Se já preparou, pule esta etapa. Use a branch `feat/oci-queue-streaming`, que contém as duas aulas.

**Comandos abaixo: Terminal do Mac ou Git Bash do Windows, dentro do projeto.**

## 2 Rode a aula 1

```sh
podman compose up -d --build aula1
podman compose logs -f aula1
```

Espere **Started Application**. Em outro terminal, envie uma ordem:

```sh
curl -i -X POST http://localhost:8080/api/ordens \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"aluno-001","ativo":"PETR4","quantidade":10}'
```

**Esperado:** HTTP 202. Nos logs, procure `WORKER=`, `GRUPO=RISCO` e `GRUPO=AUDITORIA`.

**Veja a fila:** http://localhost:15673 → usuário `aluno`, senha `aluno-local` → Queues and Streams → `aula1.ordens.comandos`. Ela pode esvaziar rapidamente após o processamento.

## 3 Rode a aula 2

Saia dos logs com Ctrl+C e execute:

```sh
podman compose stop aula1
podman compose up -d --build aula2
podman compose logs -f aula2
```

Espere **Started Application**. Em outro terminal:

```sh
curl -i -X POST http://localhost:8082/api/ordens \
  -H 'Content-Type: application/json' \
  -d '{"contaId":"aluno-002","ativo":"PETR4","quantidade":10}'
```

**Esperado:** HTTP 201 e `POSICAO_ATUALIZADA` nos logs. Após alguns segundos, abra:

- [Posições](http://localhost:8082/api/posicoes): resultado da ordem.
- [Outbox](http://localhost:8082/api/outbox): eventos e data de publicação.
- [Status](http://localhost:8082/api/lab/status): `pendentes` deve voltar a zero.

Se repetir o envio, use outra conta para observar o resultado separadamente.

## 4 Confira e encerre

`podman compose ps` mostra os serviços ativos. Ctrl+C sai dos logs; para encerrar os containers preservando os dados:

```sh
podman compose --profile contratos down
```

Se iniciou uma API pelo Maven, encerre-a também com Ctrl+C no terminal dela. Não use `-v` nem resete a VM.

## Próximos passos

- [Simular falhas, pausar processos, reenviar eventos e acompanhar tudo](docs/EXECUTAR-E-OBSERVAR.md).
- [Criar a fila OCI e configurar o acesso](README-OCI.md). Inicie o consumidor OCI somente durante o uso e encerre-o com Ctrl+C ao terminar.
