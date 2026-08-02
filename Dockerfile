FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline
# Copia o código
COPY src ./src
RUN mvn clean package -DskipTests

# Execução da aplicação
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copia o JAR
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]