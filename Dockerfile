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
RUN mvn -T2 package -DskipTests -B

# Use AdoptOpenJDK for base image.
# It's important to use OpenJDK 8u191 or above that has container support enabled.
# https://hub.docker.com/r/adoptopenjdk/openjdk8
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM openjdk:8-slim
WORKDIR /app
ARG SERVICE_NAME
ENV SERVICE_NAME $SERVICE_NAME
# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/service/${SERVICE_NAME}/target/osdu-gcp-service-${SERVICE_NAME}-*.jar osdu-gcp-service-${SERVICE_NAME}.jar
# Run the web service on container startup.
CMD java -Dhttps.protocols=TLSv1.1,TLSv1.2 -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -jar /app/osdu-gcp-service-${SERVICE_NAME}.jar
