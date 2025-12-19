# build stage with maven
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

# built war-file is copied into deloyments folder
# --> will be autodeployed by payara

COPY --from=build /app/target/jee-webshop.war ./deployments

# add postgres driver
COPY postgresql-42.7.8.jar  $PAYARA_HOME/glassfish/lib/

# add authentication file for post boot commands
COPY passwordfile .

EXPOSE 4848 8080 8181 8081 9009

# copy post boot script into designated folder
# --> will be executed automatically after boot by payara

USER root
COPY post-boot-commands.asadmin $POSTBOOT_COMMANDS
RUN chown payara $POSTBOOT_COMMANDS

