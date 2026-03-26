FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx256m", "-XX:MaxMetaspaceSize=128m", "-jar", "target/payae-0.0.1-SNAPSHOT.jar"]