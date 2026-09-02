#!/usr/bin/env bash
# =============================================================================
# chmod +x 01-criar-acr.sh && ./01-criar-acr.sh
# Cria o Resource Group e o Azure Container Registry (ACR)
# =============================================================================
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/00-check-env.sh"

echo "Criando/validando resource group '$RESOURCE_GROUP'..."
if ! az group show --name "$RESOURCE_GROUP" &>/dev/null; then
  az group create --name "$RESOURCE_GROUP" --location "$LOCATION"
else
  echo "Resource group '$RESOURCE_GROUP' já existe."
fi

echo "Criando ACR '$ACR_NAME'..."
if ! az acr show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP" &>/dev/null; then
  az acr create \
    --resource-group "$RESOURCE_GROUP" \
    --name "$ACR_NAME" \
    --sku "$ACR_SKU" \
    --location "$LOCATION" \
    --public-network-enabled true \
    --admin-enabled true
else
  echo "ACR '$ACR_NAME' já existe."
fi

LOGIN_SERVER=$(az acr show --name "$ACR_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --query loginServer --output tsv)

echo ""
echo "Login Server: $LOGIN_SERVER"
echo ""
