FROM maven:3-eclipse-temurin-25-noble AS build-step

WORKDIR /home/buildapp

COPY . .

RUN mvn package -DskipTests

FROM eclipse-temurin:25-jre-noble

RUN adduser --system --group --no-create-home newuser

WORKDIR /home/app

COPY --from=build-step /home/buildapp/target/app.jar .

EXPOSE 8080

USER newuser

ENTRYPOINT ["java","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar","app.jar"]
