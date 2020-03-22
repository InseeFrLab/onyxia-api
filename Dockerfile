FROM maven:3-jdk-11 as build  
ADD . .
RUN mvn package  

FROM openjdk:11-jre-slim
COPY --from=build onyxia-api/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]