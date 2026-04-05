FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache fontconfig ttf-dejavu

WORKDIR /app
COPY target/droptables-1.0-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
