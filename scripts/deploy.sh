#!/bin/bash
WORKDIR="$(dirname "$(readlink -e "$0")")/.."
cd "$WORKDIR" || exit 0

[[ -z $1 ]] && cat << EOF
Usage: $0 app [service [gcp-region]]
Build and deploy a container to Cloud Run

  app       name of the microservice to build
  service   Cloud Run service name (default: same as app)
  region    Google Cloud region (default: us-central1)
  
EOF

APP=$1
SERVICE=$2
REGION=$3
[[ -z $REGION ]] && REGION=us-central1
[[ -z $SERVICE ]] && SERVICE=$APP


if [[ -z $GOOGLE_CLOUD_PROJECT ]]; then
  echo "Enter your GCP project ID (default - a2ba07aca58-energy-osdu):"
  read -r GOOGLE_CLOUD_PROJECT
fi

[[ -z $GOOGLE_CLOUD_PROJECT ]] && GOOGLE_CLOUD_PROJECT="a2ba07aca58-energy-osdu" && CACHE_BUCKET=osdu-gcp-gitlab-cache
gcloud config set project $GOOGLE_CLOUD_PROJECT

COMMIT_SHA=$(git rev-parse --short HEAD 2>/dev/null)
[[ -z $COMMIT_SHA ]] && COMMIT_SHA=latest
gcloud builds submit --substitutions=_SERVICE_NAME="$APP",_SHORT_SHA="$COMMIT_SHA",_CACHE_BUCKET="$CACHE_BUCKET"

gcloud beta run deploy "$SERVICE" --image gcr.io/${GOOGLE_CLOUD_PROJECT}/osdu-gcp-"${APP}":"${COMMIT_SHA}" --platform managed --region "$REGION"
