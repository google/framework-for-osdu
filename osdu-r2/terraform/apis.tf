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
variable "apis" {
  type = set(string)
  default = [
    "run.googleapis.com",
    "datastore.googleapis.com",
    "composer.googleapis.com",
    "container.googleapis.com"
  ]
}

resource "google_project_service" "api" {
  project  = var.project
  for_each = var.apis
  service  = each.value

  disable_dependent_services = true
  disable_on_destroy         = false
}
