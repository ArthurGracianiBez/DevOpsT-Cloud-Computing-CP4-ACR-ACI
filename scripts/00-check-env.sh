#!/usr/bin/env bash
# =============================================================================
# chmod +x 00-check-env.sh && ./00-check-env.sh
# Carrega o .env e valida se as variáveis essenciais existem.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

if [ ! -f "$ENV_FILE" ]; then
  echo "ERRO: arquivo .env não encontrado em $ENV_FILE"
  echo "Copie o .env.example para .env e preencha os valores: cp .env.example .env"
  exit 1
fi

# Carrega as variáveis do .env para o ambiente do shell (sem exibir no terminal)
set -a
# shellcheck source=/dev/null
source "$ENV_FILE"
set +a

REQUIRED_VARS=(RM RESOURCE_GROUP LOCATION ACR_NAME ORACLE_IMAGE_NAME JAVA_IMAGE_NAME IMAGE_TAG \
  STORAGE_ACCOUNT_NAME FILE_SHARE_NAME KEY_VAULT_NAME ORACLE_ROOT_PASSWORD ORACLE_DATABASE \
  APP_USER APP_PASSWORD ACI_ORACLE_NAME ACI_JAVA_NAME)

for var in "${REQUIRED_VARS[@]}"; do
  if [ -z "${!var:-}" ]; then
    echo "ERRO: variável $var não definida no .env"
    exit 1
  fi
done

echo "[OK] .env carregado e validado."
