# ESTÁGIO 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia tudo para garantir que pegamos a pasta heimdex ou os arquivos na raiz
COPY . .

# Comando inteligente: se existir a pasta heimdex, entra nela. Depois roda o build.
RUN if [ -d "heimdex" ]; then cd heimdex && mvn clean package -DskipTests; \
    else mvn clean package -DskipTests; fi

# ESTÁGIO 2: Run
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Busca o arquivo JAR final onde quer que ele tenha sido gerado (raiz ou subpasta)
COPY --from=build /app/**/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
