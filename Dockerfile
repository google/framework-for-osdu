# Use the official maven/Java 8 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM maven:3-jdk-8-alpine AS builder

# Copy local code to the container image.
#create all the needed folders
WORKDIR /app
COPY pom.xml .
COPY common/ common/
COPY service/ service/

# Build a release artifact for the child project
RUN mvn package -DskipTests -B

# Use AdoptOpenJDK for base image.
# It's important to use OpenJDK 8u191 or above that has container support enabled.
# https://hub.docker.com/r/adoptopenjdk/openjdk8
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM adoptopenjdk/openjdk8:jdk8u202-b08-alpine-slim AS service
# Copy the jar to the production image from the builder stage.
ARG SERVICE_NAME
COPY --from=builder /app/service/${SERVICE_NAME}/target/osdu-gcp-service-${SERVICE_NAME}-*.jar /osdu-gcp-service-${SERVICE_NAME}.jar
#switch back to now start the resulting JAR
WORKDIR /app
# Run the web service on container startup.
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=${PORT}","-jar","/osdu-gcp-service-${SERVICE_NAME}.jar"]
