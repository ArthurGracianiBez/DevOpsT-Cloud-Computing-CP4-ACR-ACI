#!/usr/bin/env bash
# =============================================================================
# chmod +x 04-keyvault.sh && ./04-keyvault.sh
# Cria o Key Vault e armazena os segredos vindos do .env (nunca hardcoded aqui)
# =============================================================================
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/00-check-env.sh"

az provider register --namespace Microsoft.KeyVault

if ! az keyvault show --name "$KEY_VAULT_NAME" --resource-group "$RESOURCE_GROUP" &>/dev/null; then
  az keyvault create --name "$KEY_VAULT_NAME" --resource-group "$RESOURCE_GROUP" --location "$LOCATION"
else
  echo "Key Vault '$KEY_VAULT_NAME' já existe no Grupo de Recurso '$RESOURCE_GROUP'."
fi

az role assignment create \
  --assignee "$(az account show --query user.name -o tsv)" \
  --role "Key Vault Administrator" \
  --scope "/subscriptions/$(az account show --query id -o tsv)/resourceGroups/$RESOURCE_GROUP/providers/Microsoft.KeyVault/vaults/$KEY_VAULT_NAME"

echo "Aguardando propagação da permissão RBAC..."
sleep 15

ACR_USERNAME=$(az acr credential show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP" --query username --output tsv)
ACR_PASSWORD=$(az acr credential show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP" --query passwords[0].value --output tsv)

az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name oracle-root-password --value "$ORACLE_ROOT_PASSWORD" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name oracle-database --value "$ORACLE_DATABASE" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name oracle-user --value "$APP_USER" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name oracle-password --value "$APP_PASSWORD" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name spring-datasource-url --value "$SPRING_DATASOURCE_URL" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name spring-datasource-username --value "$SPRING_DATASOURCE_USERNAME" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name spring-datasource-password --value "$SPRING_DATASOURCE_PASSWORD" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name connection-strings --value "$CONNECTIONSTRINGS" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name acr-username --value "$ACR_USERNAME" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name acr-password --value "$ACR_PASSWORD" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name spring-datasource-driver-class-name --value "$SPRING_DATASOURCE_DRIVER_CLASS_NAME" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name spring-jpa-database-platform --value "$SPRING_JPA_DATABASE_PLATFORM" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name api-clima-url --value "$API_CLIMA_URL" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name api-clima-key --value "$API_CLIMA_KEY" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name api-dotnet-url --value "$API_DOTNET_URL" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name spring-cloud-openfeign-client-config-default-connecttimeout --value "$SPRING_CLOUD_OPENFEIGN_CLIENT_CONFIG_DEFAULT_CONNECTTIMEOUT" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name spring-cloud-openfeign-client-config-default-readtimeout --value "$SPRING_CLOUD_OPENFEIGN_CLIENT_CONFIG_DEFAULT_READTIMEOUT" >/dev/null
az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name springdoc-enable-hateoas --value "$SPRINGDOC_ENABLE_HATEOAS" >/dev/null

echo "[OK] Segredos armazenados no Key Vault '$KEY_VAULT_NAME'."
