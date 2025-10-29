FROM azul/zulu-openjdk-alpine:21-jre

WORKDIR /app
COPY ../build/libs/yggdrasil-proxy-1.0.1.jar /app/yggdrasil-proxy.jar
CMD ["java", "-jar", "yggdrasil-proxy.jar"]