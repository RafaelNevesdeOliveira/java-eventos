# Passos do professor para orientar os alunos

Antes da aula 1 da Jornada B3, peça que os alunos preparem o computador com internet e tempo para os downloads. A confirmação final é a mensagem **PRONTO**.

## Alunos que já tinham uma cópia

Reenvie a mensagem atualizada que está no ZIP. Oriente a turma a salvar os exercícios fora do projeto antigo, encerrar seus containers sem apagar volumes, renomear a pasta antiga e baixar o ZIP novo. O preparador deve fazer um novo clone com o destino padrão livre. Depois de PRONTO, retomem somente os arquivos de exercícios necessários, sem copiar `.git`, fazer `git pull` na cópia antiga ou mesclar o histórico anterior. A mensagem em Word reúne esses passos e os comandos do Windows e Mac em uma página.

## 1 Compartilhe o material

Envie o [link direto do ZIP completo](https://github.com/RafaelNevesdeOliveira/java-eventos/releases/download/aulas-1-e-2-v1.0.0/Laboratorios_EDA_Aulas_1_e_2_Java25.zip). Esse único download inclui o projeto, os scripts e os guias.

Uma [cópia do mesmo ZIP está nesta branch](../entrega/Laboratorios_EDA_Aulas_1_e_2_Java25.zip). No GitHub, abra o arquivo e clique em **Download raw file** para baixá-lo.

## 2 Peça que extraiam o ZIP

Oriente a turma a abrir a pasta **Entrega_EDA_Alunos** já extraída e manter todos os arquivos juntos. A mensagem curta para repassar aos alunos é **Mensagem_Alunos_Preparacao_Aula_1.docx**, dentro dessa pasta.

## 3 Oriente cada aluno pelo sistema que usa

**Windows:** dentro de **Entrega_EDA_Alunos**, clique na barra de endereço do Explorador, digite `powershell` e pressione Enter. Cole as duas linhas juntas e execute:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\preparar-windows.ps1
```

**Mac:** abra o **Terminal**, digite `cd ` com um espaço, arraste a pasta **Entrega_EDA_Alunos** para a janela e pressione Enter. Execute:

```bash
bash ./preparar-mac.sh
```

## 4 Acompanhe as solicitações de instalação

Peça que leiam e confirmem as permissões. Se o Windows solicitar administrador, o aluno deve reabrir o PowerShell como administrador na mesma pasta e repetir o comando. Se aparecer **REINICIO NECESSARIO**, deve reiniciar o computador e executar o mesmo comando novamente. Bloqueios corporativos devem ser tratados com a TI.

## 5 Confirme quem está pronto

Peça a cada aluno que confirme se apareceu **PRONTO**. Essa mensagem indica que o projeto foi preparado e o teste HTTP da aula 1 passou. Em caso de erro, peça a mensagem completa ou uma captura da tela para atendimento antes da aula.

## 6 Comece a aula pelo projeto preparado

Oriente os alunos a abrir na IDE a pasta indicada ao final do preparo e seguir **docs/AULA-1.md** dentro do projeto. Não precisam clonar novamente nem repetir a configuração manual.

Ao encerrar, execute no terminal da pasta do projeto:

```sh
podman compose down
```

Esse encerramento preserva os dados do laboratório.

## Material para sua condução das aulas

- [Guia do professor com gabaritos, avaliação e diagnóstico das duas aulas](../docs/PROFESSOR.md).
- [Este roteiro em Word](Roteiro_Professor_Passos_Alunos_Aula_1.docx).
- [Aula 1 na branch de referência](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-1/docs/AULA-1.md).
- [Aula 2 na branch de referência](https://github.com/RafaelNevesdeOliveira/java-eventos/blob/aula-2/docs/AULA-2.md).
