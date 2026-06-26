FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/PROYECTO-ADOPTAPET-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \
    SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-d8vc9i6gvqtc73c2u57g-a/adoptapet_db_mflc \
    SPRING_DATASOURCE_USERNAME=adoptapet_db_mflc_user \
    SPRING_DATASOURCE_PASSWORD=51l6lzyTWzivsyqjv7u95a3nN9wTGb3z \
    CORS_ALLOWED_ORIGINS=https://adoptapet-frontend-cs1b.onrender.com
ENTRYPOINT ["java", "-jar", "app.jar"]
