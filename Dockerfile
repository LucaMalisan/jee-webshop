FROM jelastic/maven:3.9.5-openjdk-21 AS build

WORKDIR /app

COPY src ./src
COPY passwordfile .
COPY pom.xml .

RUN mvn clean install

FROM payara/server-full:6.2024.6-jdk21

ENV PAYARA_HOME /opt/payara
ENV PATH $PAYARA_HOME/bin:$PATH

# WAR einfach in das deployments-Verzeichnis kopieren (automatisches Deployment!)
COPY --from=build /app/target/jee-webshop.war /opt/payara/deployments/

# PostgreSQL Driver
COPY postgresql-42.7.8.jar $PAYARA_HOME/glassfish/lib/

# Falls du Konfiguration (z.B. Pool und Resource) persistent machen willst:
# Erstelle eine post-boot-commands.asadmin-Datei und kopiere sie
# (siehe unten für Inhalt)

# Optional: Post-Boot-Commands für JDBC-Pool etc.
COPY post-boot-commands.asadmin $PAYARA_HOME/config/post-boot-commands.asadmin

EXPOSE 4848 8080 8081 8181 9009