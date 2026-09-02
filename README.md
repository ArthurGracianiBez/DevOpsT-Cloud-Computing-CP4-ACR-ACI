# Projeto Astra-AI (Checkpoint: Containers em Nuvem (ACR/ACI))

Deploy de uma API Java + banco Oracle em containers na Azure, usando **Azure
Container Registry (ACR)** e **Azure Container Instances (ACI)**, com
persistência em **Azure Storage Account** e segredos no **Azure Key Vault**.

> Grupo: Astra-AI · RM do representante: `rm561728` 

## Estrutura do repositório

```
.
├── .env.example                # modelo de variáveis (copie para .env)
├── .gitignore                  
├── docker/
│   ├── oracle-db/
│   │   ├── Dockerfile          # FROM gvenzl/oracle-xe:21-faststart + COPY init.sql
│   │   └── init.sql            # DDL + sequences + triggers + seed do schema RM565421
│   └── java-api/
│       └── Astra-Ai-Java/      # código-fonte da API Java + Dockerfile juntos na mesma pasta
│           ├── Dockerfile      # multi-stage (Maven -> JRE), roda como usuário "appuser" (não-root)
│           ├── pom.xml
│           ├── mvnw
│           └── src/
├── scripts/                    # scripts Azure CLI, em ordem de execução
│   ├── 00-check-env.sh
│   ├── 01-criar-acr.sh
│   ├── 02-build-e-push-imagens.sh
│   ├── 03-storage-account.sh
│   ├── 04-keyvault.sh
│   ├── 05-deploy-oracle-aci.sh
│   └── 06-deploy-java-aci.sh
├── sql/ddl.sql                  
└── tests/json/                  # exemplos de payloads para GET/POST/PUT/DELETE
```

## Pré-requisitos

- Azure CLI instalado e autenticado (`az login`)
- Docker instalado e rodando
- Acesso de Contributor na assinatura/resource group usado

## Como rodar (How To)

1. **Configure as variáveis (uma única vez):**
   ```bash
   cp .env.example .env
   # edite o .env com os dados do projeto (RM, senhas, API keys etc.)
   ```

2. **Código-fonte da API Java:**
   O projeto Java (`pom.xml`, `mvnw`, `src/`) já vive dentro deste repositório
   em `docker/java-api/Astra-Ai-Java/`, junto com o `Dockerfile`. 

3. **Dê permissão de execução aos scripts:**
   ```bash
   chmod +x scripts/*.sh
   ```

4. **Crie o Resource Group e o ACR:**
   ```bash
   ./scripts/01-criar-acr.sh
   ```

5. **Build local das imagens, teste local e push para o ACR:**
   ```bash
   ./scripts/02-build-e-push-imagens.sh
   ```
   Este script roda:
   - `docker build` da imagem do Oracle (`docker/oracle-db`, inclui o `init.sql`)
   - `docker build` da imagem da API Java a partir de `docker/java-api/Astra-Ai-Java`
     (Dockerfile e código-fonte na mesma pasta)

6. **Crie a Storage Account (volume do Oracle):**
   ```bash
   ./scripts/03-storage-account.sh
   ```

7. **Crie o Key Vault e armazene os segredos:**
   ```bash
   ./scripts/04-keyvault.sh
   ```

8. **Suba o container do banco Oracle no ACI:**
   ```bash
   ./scripts/05-deploy-oracle-aci.sh
   ```

9. **Suba o container da API Java no ACI:**
   ```bash
   ./scripts/06-deploy-java-aci.sh
   ```

10. **Teste os endpoints (evidências em `tests/json/`):**
   ```bash
   JAVA_IP=$(az container show -g rg-astra-hub -n <ACI_JAVA_NAME> --query ipAddress.fqdn -o tsv)
   curl http://$JAVA_IP:8080/rectennas
   curl -X POST http://$JAVA_IP:8080/rectennas -H "Content-Type: application/json" -d @tests/json/POST_rectenna.json
   curl -X PUT  http://$JAVA_IP:8080/rectennas/4 -H "Content-Type: application/json" -d @tests/json/PUT_rectenna_4.json
   curl -X DELETE http://$JAVA_IP:8080/rectennas/4
   ```
   Confirme a persistência direto no banco (Oracle SQL) com `SELECT * FROM AST_RECTENNA;`
   via `az container exec` no container do Oracle.

## Boas práticas de segurança aplicadas

- Nenhuma senha, chave de API ou string de conexão fica hardcoded nos scripts —
  tudo vem do `.env` (local, fora do Git) ou do Azure Key Vault.
- `.env` está no `.gitignore`; apenas `.env.example` (sem valores reais) vai
  para o GitHub.
- As credenciais do ACR não são mais impressas no terminal (`echo`) — ficam
  restritas às variáveis de ambiente do próprio Key Vault/CLI.
- O container da API Java roda com um usuário dedicado sem privilégios
  administrativos (`USER appuser` no Dockerfile), não como root.
- Recursos de nuvem são criados apenas via Azure CLI, versionados nestes
  scripts.

## Equipe

| Nome | RM |
| --- | --- |
| Arthur Graciani | RM561728 |
| Gustavo Oliveira | RM566358 |
| João Pedro Scarpin | RM565421 |
| Lucas Hideki | RM565355 |
| Wesley Andrade | RM563593 |

**Curso:** FIAP — Análise e Desenvolvimento de Sistemas
**Disciplina:** DevOps Tools & Cloud Computing
