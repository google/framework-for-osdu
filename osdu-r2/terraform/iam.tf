#   Copyright 2020 Google LLC
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

resource "google_service_account" "osdu_service_account" {
  account_id   = "osdu-gcp-sa"
  display_name = "OSDU-GCP Service Account"
}

resource "google_project_iam_member" "composer" {
  role   = "roles/composer.user"
  member = "serviceAccount:${google_service_account.osdu_service_account.email}"
  count  = var.enable_iam ? 1 : 0
}

resource "google_project_iam_member" "datastore" {
  role   = "roles/datastore.user"
  member = "serviceAccount:${google_service_account.osdu_service_account.email}"
  count  = var.enable_iam ? 1 : 0
}

resource "google_project_iam_member" "iap" {
  role   = "roles/iam.serviceAccountTokenCreator"
  member = "serviceAccount:${google_service_account.osdu_service_account.email}"
  count  = var.enable_iam ? 1 : 0
}
