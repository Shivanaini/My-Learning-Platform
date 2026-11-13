# Build the jar first from the project root:  mvn clean package -DskipTests
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/discovery-server.jar app.jar
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.20.1/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "app.jar"]
