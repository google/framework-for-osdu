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

resource "google_composer_environment" "osdu_gcp" {
  name   = "osdu-gcp"
  region = var.region
  config {
    software_config {
      python_version = "3"
    }
  }
  depends_on = [google_project_service.api]
}

resource "google_storage_bucket_object" "dags" {
  for_each = fileset("${path.module}/../os-dags/", "**")
  name     = "dags/${each.value}"
  source   = "${path.module}/../os-dags/${each.value}"
  bucket   = element(split("/", google_composer_environment.osdu_gcp.config.0.dag_gcs_prefix), 2)
}

resource "google_storage_bucket_object" "osdu_api" {
  for_each = fileset("${path.module}/../os-python-sdk/osdu_api/", "**")
  name     = "dags/osdu_api/${each.value}"
  source   = "${path.module}/../os-python-sdk/osdu_api/${each.value}"
  bucket   = element(split("/", google_composer_environment.osdu_gcp.config.0.dag_gcs_prefix), 2)
}
