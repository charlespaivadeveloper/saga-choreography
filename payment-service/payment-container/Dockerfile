FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S sagagroup && adduser -S sagauser -G sagagroup

COPY target/payment-container-1.0.0.jar app.jar

RUN chown sagauser:sagagroup app.jar

USER sagauser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
