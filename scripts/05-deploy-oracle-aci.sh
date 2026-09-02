#!/usr/bin/env bash
# =============================================================================
# chmod +x 05-deploy-oracle-aci.sh && ./05-deploy-oracle-aci.sh
# Sobe o container do banco Oracle no ACI.
# =============================================================================
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/00-check-env.sh"

az provider register --namespace Microsoft.ContainerInstance

az container create \
  --resource-group "$RESOURCE_GROUP" \
  --name "$ACI_ORACLE_NAME" \
  --image "$ACR_NAME.azurecr.io/$ORACLE_IMAGE_NAME:$IMAGE_TAG" \
  --cpu 2 \
  --memory 4 \
  --os-type Linux \
  --dns-name-label "$ACI_ORACLE_DNS_LABEL" \
  --ports 1521 \
  --registry-login-server "$ACR_NAME.azurecr.io" \
  --registry-username "$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name acr-username --query value -o tsv)" \
  --registry-password "$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name acr-password --query value -o tsv)" \
  --azure-file-volume-account-name "$STORAGE_ACCOUNT_NAME" \
  --azure-file-volume-account-key "$(az storage account keys list --resource-group "$RESOURCE_GROUP" --account-name "$STORAGE_ACCOUNT_NAME" --query "[0].value" --output tsv)" \
  --azure-file-volume-share-name "$FILE_SHARE_NAME" \
  --azure-file-volume-mount-path /var/lib/oracle \
  --environment-variables \
    ORACLE_PASSWORD="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name oracle-root-password --query value -o tsv)" \
    ORACLE_DATABASE="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name oracle-database --query value -o tsv)" \
    APP_USER="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name oracle-user --query value -o tsv)" \
    APP_USER_PASSWORD="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name oracle-password --query value -o tsv)" \
  --restart-policy Always

echo ""
echo "Logs do container:"
az container logs --resource-group "$RESOURCE_GROUP" --name "$ACI_ORACLE_NAME"

echo ""
echo " Para testar via sqlplus dentro do container:"
echo " az container exec --resource-group $RESOURCE_GROUP --name $ACI_ORACLE_NAME --exec-command \"sqlplus $APP_USER/<senha>@//localhost:1521/$ORACLE_DATABASE\""
