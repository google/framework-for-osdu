# Use the official maven/Java 8 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
#create all the needed folders
WORKDIR /app/service/delivery
#move back to the source one to copy all of the parent files
WORKDIR /app
COPY pom.xml .
COPY service/pom.xml ./service
COPY service/delivery/pom.xml ./service/delivery
COPY service/delivery/src ./service/delivery/src

# Build a release artifact for the child project
WORKDIR /app/service/delivery
RUN mvn package -DskipTests

# Use AdoptOpenJDK for base image.
# It's important to use OpenJDK 8u191 or above that has container support enabled.
# https://hub.docker.com/r/adoptopenjdk/openjdk8
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM adoptopenjdk/openjdk8:jdk8u202-b08-alpine-slim

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/service/delivery/target/osdu-gcp-service-delivery-*.jar /osdu-gcp-service-delivery.jar
#switch back to now start the resulting JAR
WORKDIR /app
# Run the web service on container startup.
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=${PORT}","-jar","/osdu-gcp-service-delivery.jar"]