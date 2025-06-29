# Estagio 1: Build em Java 17 e Maven:
FROM maven:3.9.6-sapmachine-17-full AS build # <--- ALTERADO AQUI

# Define o diretorio de trabalho no container para o build
WORKDIR /app

# Copia o arquivo pom.xml pro Maven possa baixar as dependências
COPY pom.xml .

# Copia o restante do código fonte
COPY src ./src

# Gera o WAR
RUN mvn clean install -DskipTests

# Estagio 2: Roda a aplicacao num Tomcat leve (Java 11 e Tomcat 9)
FROM tomcat:9.0-jdk11-openjdk

# Tira a aplicacao de exemplo que vem com o Tomcat
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copia o arquivo .war pro Tomcat
COPY --from=build /app/target/diario-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Expoe a porta 8080 padrão do Tomcat
EXPOSE 8080

# Iniciar o Tomcat quando o container for executado
CMD ["catalina.sh", "run"]
