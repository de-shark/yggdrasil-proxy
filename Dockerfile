FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=build/libs/yggdrasil-proxy-*.jar
COPY ${JAR_FILE} /app/
CMD ["java", "-jar", "/app/yggdrasil-proxy-*.jar"]