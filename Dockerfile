# Build the WAR using the Java release configured in pom.xml.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Tomcat 11 implements Jakarta Servlet 6.1, which this application uses.
FROM tomcat:11-jre25-temurin

# Deploy as ROOT.war so the application is served from the service root.
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /workspace/target/employee-management-system.war /usr/local/tomcat/webapps/ROOT.war
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 10000
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
