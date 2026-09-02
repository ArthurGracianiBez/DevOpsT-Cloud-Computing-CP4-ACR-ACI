#!/usr/bin/env bash
# =============================================================================
# chmod +x 03-storage-account.sh && ./03-storage-account.sh
# Cria a Storage Account e o File Share usados como volume persistente do Oracle
# =============================================================================
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/00-check-env.sh"

az provider register --namespace Microsoft.Storage

if ! az storage account show --name "$STORAGE_ACCOUNT_NAME" --resource-group "$RESOURCE_GROUP" &>/dev/null; then
  az storage account create --resource-group "$RESOURCE_GROUP" \
    --name "$STORAGE_ACCOUNT_NAME" \
    --location "$LOCATION" \
    --sku Standard_LRS
else
  echo "A conta de armazenamento '$STORAGE_ACCOUNT_NAME' já existe"
fi

CONNECTION_STRING=$(az storage account show-connection-string \
  --name "$STORAGE_ACCOUNT_NAME" --resource-group "$RESOURCE_GROUP" \
  --query connectionString --output tsv)

if ! az storage share exists --name "$FILE_SHARE_NAME" --account-name "$STORAGE_ACCOUNT_NAME" \
    --connection-string "$CONNECTION_STRING" | grep -q true; then
  az storage share create --name "$FILE_SHARE_NAME" --account-name "$STORAGE_ACCOUNT_NAME" \
    --connection-string "$CONNECTION_STRING"
else
  echo "O compartilhamento de arquivos '$FILE_SHARE_NAME' já existe"
fi

echo "[OK] Storage Account e File Share prontos."
