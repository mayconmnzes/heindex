# ESTÁGIO 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia todos os arquivos do repositório
COPY . .

# Entra na pasta heimdex e gera o JAR
RUN cd heimdex && mvn clean package -DskipTests

# ESTÁGIO 2: Execução (Imagem leve)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copia o JAR do caminho específico onde o Maven o criou
COPY --from=build /app/heimdex/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
