FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY parkutp/pom.xml ./pom.xml
COPY parkutp/src ./src
# Las pruebas con PostgreSQL se ejecutan antes de construir la imagen.
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 parkutp \
    && useradd --uid 10001 --gid parkutp --create-home parkutp
WORKDIR /app
COPY --from=build --chown=10001:10001 /build/target/parkutp-apf2.war /app/parkutp.war
RUN mkdir -p /app/.tomcat && chown -R 10001:10001 /app
USER 10001:10001
ENV PORT=8081 TOMCAT_BASE_DIR=/app/.tomcat
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
    CMD curl --fail --silent --output /dev/null http://127.0.0.1:${PORT}/login || exit 1
ENTRYPOINT ["java", "-jar", "/app/parkutp.war"]
