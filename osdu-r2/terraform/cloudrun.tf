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

locals {
  limits = "${map(
    "memory", "512Mi",
    "cpu", "1000m",
  )}"
}

resource "google_cloud_run_service" "delivery" {
  name     = "os-delivery"
  location = var.region

  template {
    spec {
      containers {
        image = "gcr.io/${var.gcr_project}/os-delivery/delivery-gcp-datastore:latest"
        env {
          name  = "FILE_LOCATION_BUCKET_NAME"
          value = google_storage_bucket.osdu_file_bucket.name
        }
        env {
          name  = "FILE_LOCATION_USER_ID"
          value = "common-user"
        }
        env {
          name  = "OSDU_ENTITLEMENTS_URL"
          value = var.entitlement_service
        }
        resources {
          limits = local.limits
        }
      }
      service_account_name = google_service_account.osdu_service_account.email
    }

    metadata {
      labels = {
        app     = "osdu"
        service = "os-delivery"
      }
    }
  }
  depends_on = [google_project_service.api]
}

resource "google_cloud_run_service" "workflow" {
  name     = "os-workflow"
  location = var.region

  template {
    spec {
      containers {
        image = "gcr.io/${var.gcr_project}/os-workflow/workflow-gcp-datastore:latest"
        env {
          name  = "GCP_AIRFLOW_URL"
          value = google_composer_environment.osdu_gcp.config.0.airflow_uri
        }
        env {
          name  = "OSDU_ENTITLEMENTS_URL"
          value = var.entitlement_service
        }
        resources {
          limits = local.limits
        }
      }
      service_account_name = google_service_account.osdu_service_account.email
    }

    metadata {
      labels = {
        app     = "osdu"
        service = "os-workflow"
      }
    }
  }
  depends_on = [google_project_service.api]
}

resource "google_cloud_run_service" "ingest" {
  name     = "os-ingest"
  location = var.region

  template {
    spec {
      containers {
        image = "gcr.io/${var.gcr_project}/os-ingest/ingest-gcp-datastore:latest"
        env {
          name  = "OSDU_DELIVERY_SERVICE_URL"
          value = google_cloud_run_service.delivery.status[0].url
        }
        env {
          name  = "OSDU_WORKFLOW_SERVICE_URL"
          value = google_cloud_run_service.workflow.status[0].url
        }
        env {
          name  = "OSDU_ENTITLEMENTS_URL"
          value = var.entitlement_service
        }
        resources {
          limits = local.limits
        }
      }
      service_account_name = google_service_account.osdu_service_account.email
    }

    metadata {
      labels = {
        app     = "osdu"
        service = "os-ingest"
      }
    }
  }
  depends_on = [google_project_service.api]
}

resource "google_cloud_run_service_iam_member" "delivery_iam" {
  location = google_cloud_run_service.delivery.location
  project  = google_cloud_run_service.delivery.project
  service  = google_cloud_run_service.delivery.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_service_iam_member" "ingest_iam" {
  location = google_cloud_run_service.ingest.location
  project  = google_cloud_run_service.ingest.project
  service  = google_cloud_run_service.ingest.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_service_iam_member" "workflow_iam" {
  location = google_cloud_run_service.workflow.location
  project  = google_cloud_run_service.workflow.project
  service  = google_cloud_run_service.workflow.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

output "delivery_url" {
  value       = google_cloud_run_service.delivery.status[0].url
  description = "OS-Delivery Cloud Run URL"
}

output "ingest_url" {
  value       = google_cloud_run_service.ingest.status[0].url
  description = "OS-Ingest Cloud Run URL"
}

output "workflow_url" {
  value       = google_cloud_run_service.workflow.status[0].url
  description = "OS-Workflow Cloud Run URL"
}
