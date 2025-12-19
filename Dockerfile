# Build-Stage bleibt gleich (Maven mit JDK 21)
FROM jelastic/maven:3.9.5-openjdk-21 AS build

WORKDIR /app

COPY src ./src
COPY pom.xml .

RUN mvn clean install

FROM payara/server-full:6.2024.6-jdk21

ENV PAYARA_HOME /opt/payara
ENV PATH $PAYARA_HOME/bin:$PATH
ENV AUTH0_DOMAIN "dev-rqhpuzb3altnalx3.us.auth0.com"
ENV AUTH0_CLIENTID "Y13a6s6w09e9UQfWQLlaQfy0Ts68oJcq"
ENV AUTH0_CLIENTSECRET "M8APPl2uISY_PMEFc-jEF4Cx8Bg_Pkv005bi5YkfmG2ud3Y42hoW0Y6UNgpKuD8Y"
ENV AUTH0_SCOPE "openid profile email"
ENV EMAIL_ADDRESS "jeewebshop@gmail.com"
ENV EMAIL_PASSWORD "buwy rwwp yyyh nqid"

WORKDIR /opt/payara

COPY --from=build /app/target/jee-webshop.war ./deployments
COPY postgresql-42.7.8.jar  $PAYARA_HOME/glassfish/lib/
COPY passwordfile .

RUN cd $PAYARA_HOME/glassfish/lib && ls -l

EXPOSE 4848 8080 8181 8081 9009

RUN asadmin start-domain && \
    asadmin -u admin --passwordfile passwordfile add-library glassfish/lib/postgresql-42.7.8.jar && \
    asadmin  -u admin --passwordfile passwordfile create-jdbc-connection-pool \
      --datasourceClassname=org.postgresql.ds.PGSimpleDataSource \
      --resType=javax.sql.DataSource \
      --property "DatabaseName=jee_webshop:Password=admin:PortNumber=5433:ServerName=host.docker.internal:User=postgres" \
      PostgresPool && \
    asadmin -u admin --passwordfile passwordfile create-jdbc-resource --enabled=true --poolName=PostgresPool postgres_resource && \
    asadmin -u admin --passwordfile passwordfile deploy --name jee-webshop /opt/payara/deployments/jee-webshop.war && \
    asadmin -u admin --passwordfile passwordfile stop-domain

CMD ["asadmin", "start-domain", "--verbose"]