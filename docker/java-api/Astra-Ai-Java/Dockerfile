# ---- STAGE 1: Compilacao ----
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# ---- STAGE 2: Imagem final ----
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
COPY --from=builder /build/target/*.jar /app/app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
