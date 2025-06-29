# Usa uma imagem docker base que ja tem Java 11 e Tomcat 9
FROM tomcat:9.0-jdk11-openjdk

# Remove a aplicacao de exemplo que vem no Tomcat e usa a minha:
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copia o arquivo .war praa pasta webapps do Tomcat.
# 'ROOT.war' faz com que a aplicacao fique acessivel diretamente na raiz da URL.
COPY target/diario-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Expoe a porta padrao do Tomcat pras requisicoes.
EXPOSE 8080

# Define o comando que vai ser executado quando o container for iniciado.
# 'catalina.sh run' inicia o Tomcat.
CMD ["catalina.sh", "run"]