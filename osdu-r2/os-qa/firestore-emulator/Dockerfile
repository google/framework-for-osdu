# based on https://github.com/maximelebastard/firestore-emulator-docker
ARG GCLOUD_SDK_VERSION=alpine

FROM google/cloud-sdk:$GCLOUD_SDK_VERSION

# Install Java 8 for Datastore emulator
RUN apk add --update --no-cache openjdk8-jre &&\
    gcloud components install cloud-firestore-emulator beta --quiet

COPY start-firestore .

ENTRYPOINT ["./start-firestore"]
