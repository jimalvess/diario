# Estagio 1: Build da aplicação Java 17
FROM openjdk:17-jdk-slim AS build-env

# Instala o Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Define o diretorio de trabalho no container pro build
WORKDIR /app

# Copia o arquivo pom.xml pra que o Maven possa baixar as dependências
COPY pom.xml .

# Copia o restante do código fonte
COPY src ./src

# Gera o WAR
RUN mvn clean install -DskipTests

# Estagio 2: Roda a aplicação num Tomcat leve (9)
FROM tomcat:9.0-jdk11-openjdk

# Remove a aplicacao de exemplo que vem com o Tomcat
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copia o arquivo .war pro Tomcat
COPY --from=build-env /app/target/diario-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Expoe a porta 8080 padrão do Tomcat
EXPOSE 8080

# Inicia o Tomcat quando o container for executado
CMD ["catalina.sh", "run"]