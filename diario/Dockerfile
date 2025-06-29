# Usa uma imagem base que já tem Java e Tomcat
# Java 11 e Tomcat 9:
FROM tomcat:9.0-jdk11-openjdk

# Remove a aplicação de exemplo que vem pré-instalada com o Tomcat.
# Aí fica só essa a rodar na raiz do servidor.
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copia o .war compilado pra pasta webapps do Tomcat.
# A pasta 'target/' é onde o Maven/Gradle geralmente coloca o .war após a compilação.
# O nome 'seu-backend.war' será substituído pelo nome real do seu arquivo .war.
# 'ROOT.war' faz com que sua aplicação seja acessível diretamente na raiz da URL.
COPY target/seu-backend.war /usr/local/tomcat/webapps/ROOT.war

# Expõe a porta 8080. Porta padrão do Tomcat
EXPOSE 8080

# Define o comando que será executado quando o container for iniciado.
# 'catalina.sh run' é o comando pra iniciar o Tomcat.
CMD ["catalina.sh", "run"]