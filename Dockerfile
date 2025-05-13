FROM azul/zulu-openjdk-alpine:21-jre

WORKDIR /app
COPY ../build/libs/yggdrasil-proxy-1.0-SNAPSHOT.jar /app/yggdrasil-proxy.jar
CMD ["java", "-jar", "yggdrasil-proxy.jar"]