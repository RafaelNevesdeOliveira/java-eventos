#!/bin/sh
# Inicia uma aula via Maven e preserva a VM, os volumes e os dados existentes.
set -eu
cd "$(dirname "$0")/.."
aula=${1:-1}
modo=${2:-local}
case "$aula" in 1|2) ;; *) echo 'Uso: ./scripts/iniciar.sh 1|2 local|oci'; exit 1;; esac
case "$modo" in local|oci) ;; *) echo 'Uso: ./scripts/iniciar.sh 1|2 local|oci'; exit 1;; esac

if [ "$modo" = oci ]; then
  configuracao=${OCI_KAFKA_CONFIG_FILE:-$HOME/.oci/java-eventos/kafka.properties}
  if [ ! -f "$configuracao" ]; then
    echo 'Falta a configuração privada OCI. Consulte docs/OCI.md.'
    exit 1
  fi
  oci session validate --profile "${OCI_PROFILE:-JORNADA_B3}" --auth security_token || {
    echo 'Renove o login OCI antes de iniciar a aula. Consulte docs/OCI.md.'
    exit 1
  }
  if [ "$aula" = 1 ]; then export PORT=${PORT:-8180}; else export PORT=${PORT:-8182}; fi
fi

if [ "$modo" = local ] || [ "$aula" = 2 ]; then
  if ! podman info >/dev/null 2>&1; then
    if [ "$(uname -s)" = Darwin ]; then
      podman machine start # Reutiliza a máquina existente; não faz init nem reset.
    else
      echo 'Podman indisponível. Inicie o serviço Podman e tente novamente.'
      exit 1
    fi
  fi
  if [ "$modo" = oci ]; then
    podman compose up -d --wait postgres
    # Schema separado impede o envio de eventos locais pendentes para a nuvem.
    podman compose exec -T postgres psql -U aluno -d eda \
      -c 'CREATE SCHEMA IF NOT EXISTS oci_aulas AUTHORIZATION aluno'
  elif [ "$aula" = 1 ]; then
    podman compose up -d --wait rabbitmq kafka
  else
    podman compose up -d --wait postgres kafka
  fi
fi

# Instala apenas o módulo compartilhado, necessário quando o clone ainda não foi compilado.
mvn -B -pl ordens-domain -am install -DskipTests
echo "Iniciando aula $aula no modo $modo. Ctrl+C encerra apenas a aplicação."
exec mvn -B -pl "aula$aula" spring-boot:run "-Dspring-boot.run.profiles=$modo"
