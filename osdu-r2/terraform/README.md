## OSDU-GCP installation

This guide assumes the following:

* Google Cloud SDK installed
* Bash or a compatible shell
* Python 3 environment
* Terraform 0.12.8+ installed
* Basic familiarity with Google Cloud Platform and Terraform

Tested on Google Cloud Shell.

1. Set the `GOOGLE_CLOUD_PROJECT` environment variable to your GCP project ID:

```bash
export GOOGLE_CLOUD_PROJECT=<your-project-id>
```

2. Ensure that the service containers are built and pushed to the GCP project's Container Registry.

If you want to use another project's registry, first complete the steps outlined in the [Deploying images from other
Google Cloud projects] documentation.

Run the following gcloud command from the directory where application source code is stored:

```bash
gcloud builds submit . --substitutions=_PROVIDER_NAME=gcp-datastore,_SHORT_SHA=$(git rev-parse --short HEAD)
```

3. [Create an instance of Cloud Firestore in Datastore mode]
4. Create a service account which will be used by Terraform, e.g.:

```bash
gcloud iam service-accounts create terraform
```

5. Create a service account key and store it securely:

```bash
gcloud iam service-accounts keys create terraform.json --iam-account=terraform@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com
```

6. Assign Project Editor, Cloud Run Admin, Datastore Index Admin, and Storage Object Admin roles to the account. You can
remove them along with the service account once the deployment is complete.

```bash
gcloud projects add-iam-policy-binding $GOOGLE_CLOUD_PROJECT \
    --member=serviceAccount:terraform@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com \
    --role=roles/editor
gcloud projects add-iam-policy-binding $GOOGLE_CLOUD_PROJECT \
    --member=serviceAccount:terraform@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com \
    --role=roles/datastore.indexAdmin
gcloud projects add-iam-policy-binding $GOOGLE_CLOUD_PROJECT \
    --member=serviceAccount:terraform@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com \
    --role=roles/storage.objectAdmin
gcloud projects add-iam-policy-binding $GOOGLE_CLOUD_PROJECT \
    --member=serviceAccount:terraform@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com \
    --role=roles/run.admin
```

If you want Terraform to configure IAM roles for Cloud Run service account, add Project IAM Admin role as well:

```bash
gcloud projects add-iam-policy-binding $GOOGLE_CLOUD_PROJECT \
    --member=serviceAccount:terraform@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com \
    --role=roles/resourcemanager.projectIamAdmin
```

7. Run the script to seed Cloud Datastore values:

```bash
pip3 install google-cloud-datastore # Already installed in Cloud Shell
export GOOGLE_APPLICATION_CREDENTIALS="<path to the service account key, e.g. terraform.json>"
python3 datastore-import/datastore_import.py
 ```

8. Create a Terraform variables file (e.g. `terraform.tfvars`) with the following contents:

```bash
project             = "<GCP project ID>"
gcr_project         = "<project ID of the Container Registry where images reside>"
credentials_file    = "<path to the service account key, e.g. terraform.json>"
region              = "<your GCP region>"
enable_iam          = false # Set to true if Terraform service account has Project IAM Admin role
entitlement_service = "<your OpenDES entitlements API URL, e.g. https://example.com/entitlements/v1>"

```

9. Initialize a Terraform project and run `plan` to preview the infrastructure that will be created:

```bash
terraform init
terraform plan --var-file=terraform.tfvars --out=terraform.tfplan
```

10. Apply the saved Terraform plan

```bash
terraform apply
```

At the end of this step, it will output the URLs of Cloud Run services.

11. If you didn't apply IAM changes via Terraform, configure the following roles for the service account used for OS
services:

```bash
gcloud projects add-iam-policy-binding $GOOGLE_CLOUD_PROJECT \
    --member=serviceAccount:osdu-gcp-sa@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com \
    --role=roles/composer.user
gcloud projects add-iam-policy-binding $GOOGLE_CLOUD_PROJECT \
    --member=serviceAccount:osdu-gcp-sa@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com \
    --role=roles/datastore.user
gcloud projects add-iam-policy-binding $GOOGLE_CLOUD_PROJECT \
    --member=serviceAccount:osdu-gcp-sa@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com \
    --role=roles/iam.serviceAccountTokenCreator
```

[Deploying images from other Google Cloud projects]: https://cloud.google.com/run/docs/deploying#other-projects
[Create an instance of Cloud Firestore in Datastore mode]: https://cloud.google.com/datastore/docs/quickstart
