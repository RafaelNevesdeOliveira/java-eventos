# Passos do professor para orientar os alunos

Roteiro para a aula 1 da Jornada B3

O histórico do Git foi revisado. Oriente a turma a baixar o ZIP novo e fazer um novo clone antes da aula, com internet e tempo para os downloads. Quem ainda não tinha o projeto começa no passo 2.

## 1 Oriente o backup e guarde a cópia antiga

Peça que copiem os exercícios para uma pasta de backup fora do projeto antigo. Se já executaram o laboratório, no terminal do projeto antigo, encerrem os containers preservando os dados:

```sh
podman compose --profile contratos down
```

Depois, fechem a IDE e renomeiem a pasta antiga para um nome disponível, como **laboratorios-eda-antigo**. Não apaguem essa cópia. **Não usem git pull nem mesclem o histórico antigo.**

## 2 Compartilhe o ZIP novo

[Baixar o ZIP atualizado das aulas](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Laboratorios_EDA_Aulas_1_e_2_Java25.zip)

Peça que extraiam o ZIP em uma pasta nova, abram **Entrega_EDA_Alunos** e mantenham todos os arquivos juntos. A mensagem para enviar à turma é **Mensagem_Alunos_Preparacao_Aula_1.docx**, dentro do ZIP.

## 3 Oriente o preparador para fazer um novo clone

O destino padrão **JornadaB3/laboratorios-eda**, dentro da pasta pessoal, precisa estar livre após renomear a cópia antiga.

**Windows:** na pasta **Entrega_EDA_Alunos**, clique na barra de endereço do Explorador, digite **powershell** e pressione Enter. Cole as duas linhas juntas e execute:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\preparar-windows.ps1
```

**Mac:** abra o **Terminal**, digite `cd ` com um espaço, arraste a pasta **Entrega_EDA_Alunos** para a janela e pressione Enter. Execute:

```bash
bash ./preparar-mac.sh
```

## 4 Acompanhe as permissões e o reinício

Se o Windows pedir administrador, reabra o PowerShell como administrador na mesma pasta e repita o comando. Se aparecer **REINICIO NECESSARIO**, reinicie o computador e repita o comando. Bloqueios corporativos devem ser tratados com a TI.

## 5 Confirme PRONTO e a retomada dos exercícios

Peça a confirmação de **PRONTO** e que abram na IDE o projeto novo indicado ao final. O teste HTTP da aula 1 já terá passado. Retomem somente os arquivos dos exercícios necessários, sem copiar **.git** nem substituir o projeto inteiro. Mantenham o backup até conferir tudo. Em caso de erro, peça a mensagem completa ou uma captura da tela antes da aula.

## 6 Comece a aula e encerre preservando os dados

No projeto novo, siga **docs/AULA-1.md**. O preparador já fez o novo clone; não é necessário repetir a instalação manual. Ao encerrar, no terminal da pasta do projeto, execute:

```sh
podman compose down
```

## Material para sua condução das aulas

- [Guia do professor e gabaritos](../docs/PROFESSOR.md).
- [Este roteiro em Word](Roteiro_Professor_Passos_Alunos_Aula_1.docx).
- [Cópia do ZIP nesta branch](../entrega/Laboratorios_EDA_Aulas_1_e_2_Java25.zip).
- [Aula 1 na branch de referência](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-1/docs/AULA-1.md).
- [Aula 2 na branch de referência](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-2/docs/AULA-2.md).
