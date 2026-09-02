#!/usr/bin/env bash
# =============================================================================
# chmod +x 06-deploy-java-aci.sh && ./06-deploy-java-aci.sh
# Sobe o container da API Java no ACI.
# =============================================================================
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/00-check-env.sh"

ORACLE_PUBLIC_IP=$(az container show --resource-group "$RESOURCE_GROUP" --name "$ACI_ORACLE_NAME" --query ipAddress.ip --output tsv)

az provider register --namespace Microsoft.ContainerInstance

az container create \
  --resource-group "$RESOURCE_GROUP" \
  --name "$ACI_JAVA_NAME" \
  --image "$ACR_NAME.azurecr.io/$JAVA_IMAGE_NAME:$IMAGE_TAG" \
  --cpu 1 \
  --memory 1.5 \
  --os-type Linux \
  --dns-name-label "$ACI_JAVA_DNS_LABEL" \
  --ports 8080 \
  --registry-login-server "$ACR_NAME.azurecr.io" \
  --registry-username "$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name acr-username --query value -o tsv)" \
  --registry-password "$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name acr-password --query value -o tsv)" \
  --environment-variables \
    SPRING_APPLICATION_NAME="$SPRING_APPLICATION_NAME" \
    SERVER_PORT="$SERVER_PORT" \
    SPRING_DATASOURCE_URL="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name spring-datasource-url --query value -o tsv | sed "s/oracledb/$ORACLE_PUBLIC_IP/")" \
    SPRING_DATASOURCE_USERNAME="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name spring-datasource-username --query value -o tsv)" \
    SPRING_DATASOURCE_PASSWORD="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name spring-datasource-password --query value -o tsv)" \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name spring-datasource-driver-class-name --query value -o tsv)" \
    SPRING_JPA_DATABASE_PLATFORM="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name spring-jpa-database-platform --query value -o tsv)" \
    API_CLIMA_URL="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name api-clima-url --query value -o tsv)" \
    API_CLIMA_KEY="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name api-clima-key --query value -o tsv)" \
    API_DOTNET_URL="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name api-dotnet-url --query value -o tsv)" \
    SPRING_CLOUD_OPENFEIGN_CLIENT_CONFIG_DEFAULT_CONNECTTIMEOUT="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name spring-cloud-openfeign-client-config-default-connecttimeout --query value -o tsv)" \
    SPRING_CLOUD_OPENFEIGN_CLIENT_CONFIG_DEFAULT_READTIMEOUT="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name spring-cloud-openfeign-client-config-default-readtimeout --query value -o tsv)" \
    SPRINGDOC_ENABLE_HATEOAS="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name springdoc-enable-hateoas --query value -o tsv)" \
  --restart-policy Always

echo ""
echo "Logs do container:"
az container logs --resource-group "$RESOURCE_GROUP" --name "$ACI_JAVA_NAME"
