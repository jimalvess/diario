# Estagio 1: Build da aplicacao Java no Maven (Java 17):
FROM maven:3.9.6-openjdk-17 AS build

# Define o diretório de trabalho no container pro build
WORKDIR /app

# Copia o arquivo pom.xml pro Maven pra baixar as dependencias
COPY pom.xml .

# Copia o restante do código-fonte
COPY src ./src

# Faz o build do Maven e gera o WAR
# O '-DskipTests' faz builds mais rapidos em CI/CD
RUN mvn clean install -DskipTests

# Estagio 2: Roda a aplicacao num Tomcat leve
FROM tomcat:9.0-jdk11-openjdk

# Tira a aplicacao de exemplo que vem no Tomcat
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copia o arquivo .war pro Tomcat
COPY --from=build /app/target/diario-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Abre porta 8080 padrão do Tomcat
EXPOSE 8080

# Inicia o Tomcat quando o container for executado
CMD ["catalina.sh", "run"]