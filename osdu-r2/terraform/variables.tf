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

variable "region" {
  type        = string
  default     = "us-central1"
  description = "GCP region"
}

variable "project" {
  type        = string
  description = "Your GCP project ID"
}

variable "gcr_project" {
  type        = string
  description = "GCP project hosting the Container Registry"
}

variable "credentials_file" {
  type        = string
  description = "Path to the service account credentials JSON file"
}

variable "enable_iam" {
  type        = bool
  default     = false
  description = "Enable management of IAM role bindings. Requires Project IAM Admin role or equivalent for Terraform service account"
}

variable "entitlement_service" {
  type        = string
  description = "Your OpenDES entitlements API URL, e.g. https://example.com/entitlements/v1"
}
