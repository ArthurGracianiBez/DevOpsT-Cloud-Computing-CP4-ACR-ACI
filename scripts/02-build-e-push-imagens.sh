#!/usr/bin/env bash
# =============================================================================
# chmod +x 02-build-e-push-imagens.sh && ./02-build-e-push-imagens.sh
# Faz: docker build (Oracle e Java API) -> login no ACR ->
#      docker tag -> docker push
# =============================================================================
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/00-check-env.sh"

PROJECT_ROOT="$SCRIPT_DIR/.."
JAVA_SRC_DIR="$PROJECT_ROOT/docker/java-api/Astra-Ai-Java"

if [ ! -f "$JAVA_SRC_DIR/pom.xml" ]; then
  echo "ERRO: não encontrei $JAVA_SRC_DIR/pom.xml"
  echo "Clone o repositório da API Java dentro de '$JAVA_SRC_DIR' antes de rodar este script"
  echo "(veja Astra-Ai-Java/CLONE_AQUI.md)."
  exit 1
fi

echo "==> 1) Build da imagem do banco (Oracle) a partir do Dockerfile"
# Contexto = docker/oracle-db (o Dockerfile faz "COPY init.sql ..." relativo a essa pasta)
docker build -t "$ORACLE_IMAGE_NAME:$IMAGE_TAG" "$PROJECT_ROOT/docker/oracle-db"

echo "==> 2) Build da imagem da API Java a partir do Dockerfile"
# Dockerfile fica em docker/java-api/, mas o contexto (COPY . .) é o código-fonte
# clonado em Astra-Ai-Java/ (onde estão pom.xml, mvnw e src/)
docker build -f "$PROJECT_ROOT/docker/java-api/Dockerfile" -t "$JAVA_IMAGE_NAME:$IMAGE_TAG" "$JAVA_SRC_DIR"

echo ""
echo "==> Imagens construídas localmente:"
docker image ls | grep -E "$ORACLE_IMAGE_NAME|$JAVA_IMAGE_NAME" || true

echo "==> 3) Login no ACR (usa o Azure AD do usuário logado no 'az login', sem senha em texto)"
az acr login --name "$ACR_NAME"

LOGIN_SERVER=$(az acr show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP" --query loginServer --output tsv)

echo "==> 4) Tag das imagens com o login server do ACR"
docker tag "$ORACLE_IMAGE_NAME:$IMAGE_TAG" "$LOGIN_SERVER/$ORACLE_IMAGE_NAME:$IMAGE_TAG"
docker tag "$JAVA_IMAGE_NAME:$IMAGE_TAG" "$LOGIN_SERVER/$JAVA_IMAGE_NAME:$IMAGE_TAG"

echo "==> 5) Push das imagens para o ACR"
docker push "$LOGIN_SERVER/$ORACLE_IMAGE_NAME:$IMAGE_TAG"
docker push "$LOGIN_SERVER/$JAVA_IMAGE_NAME:$IMAGE_TAG"

echo ""
echo "==> Repositórios registrados no ACR:"
az acr repository list --name "$ACR_NAME" --output table

echo ""
echo "==> Tags de cada repositório:"
az acr repository show-tags --name "$ACR_NAME" --repository "$ORACLE_IMAGE_NAME"
az acr repository show-tags --name "$ACR_NAME" --repository "$JAVA_IMAGE_NAME"

echo "[OK] Build e push concluídos."
