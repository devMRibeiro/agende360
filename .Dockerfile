# -------- STAGE 1: build --------
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Primeiro só dependências (cache inteligente)
COPY pom.xml .
RUN mvn dependency:go-offline

# Depois código
COPY src ./src

RUN mvn clean package -DskipTests

# -------- STAGE 2: runtime --------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copia JAR específico (ajuste o nome se quiser)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]