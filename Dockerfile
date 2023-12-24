
################################
#      FOR RENDER.COM          #
################################

#
# Build stage
#
FROM maven:3.8.2-jdk-11 AS build
COPY . .
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM openjdk:11-jdk-slim
COPY --from=build /target/*.jar app.jar
# ENV PORT=8888
EXPOSE 8888
ENTRYPOINT ["java","-jar","app.jar"]