# Estágio de Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia tudo para garantir que pegamos a pasta heimdex se ela existir
COPY . .

# Verifica onde está o pom.xml e roda o build lá dentro
RUN if [ -f "pom.xml" ]; then \
        mvn clean package -DskipTests; \
    elif [ -f "heimdex/pom.xml" ]; then \
        cd heimdex && mvn clean package -DskipTests && cp target/*.jar ../target/; \
    fi

# Estágio de Execução
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Busca o JAR gerado (na raiz ou dentro de heimdex/target)
COPY --from=build /app/**/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
