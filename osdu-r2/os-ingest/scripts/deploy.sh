# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# !/bin/bash
WORKDIR=$(cd "$(dirname "$0")"/..; pwd)
cd "$WORKDIR" || exit 0

if [[ -z $1 ]]; then
  cat << EOF
Usage: $0 provider mvn-user mvn-password cache-bucket [gcp-region]
Build and deploy a container to Cloud Run

  provider      provider name
  mvn-user      Maven repository user
  mvn-password  Maven repository password
  cache-bucket  GCS bucket for caching Cloud Build results
  region        Google Cloud region (default: us-central1)

EOF
  exit 1
fi

PROVIDER=$1
MAVEN_REPO_USER=$2
MAVEN_REPO_PASS=$3
CACHE_BUCKET=$4
REGION=$5
[[ -z $REGION ]] && REGION=us-central1

if [[ -z $GOOGLE_CLOUD_PROJECT ]]; then
  echo "Enter your GCP project ID:"
  read -r GOOGLE_CLOUD_PROJECT
fi

gcloud config set project "$GOOGLE_CLOUD_PROJECT"

if [[ -z $MAVEN_REPO_USER ]]; then
  echo "Enter Maven user name:"
  read -r MAVEN_REPO_USER
fi

if [[ -z $MAVEN_REPO_PASS ]]; then
  echo "Enter Maven user passwrod:"
  read -r MAVEN_REPO_PASS
fi

if [[ -z $CACHE_BUCKET ]]; then
  echo "Enter the GCS bucket for caching Cloud Build results"
  read -r CACHE_BUCKET
fi

COMMIT_SHA=$(git rev-parse --short HEAD 2>/dev/null)
[[ -z $COMMIT_SHA ]] && COMMIT_SHA=latest
gcloud builds submit --config "${WORKDIR}"/cloudbuild.yaml --substitutions=_PROVIDER_NAME="$PROVIDER",_SHORT_SHA="$COMMIT_SHA",_CACHE_BUCKET="$CACHE_BUCKET",_MAVEN_REPO_USER="$MAVEN_REPO_USER",_MAVEN_REPO_PASS="$MAVEN_REPO_PASS"

gcloud run deploy os-ingest --image gcr.io/"${GOOGLE_CLOUD_PROJECT}"/os-ingest/ingest-"${PROVIDER}":"${COMMIT_SHA}" --platform managed --region "$REGION"
