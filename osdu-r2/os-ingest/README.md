# OSDU R2 Ingestion Service

## Contents

* [Introduction](#introduction)
* [System interactions](#system-interactions)
    * [Default ingestion workflow](#default-ingestion-workflow)
    * [OSDU ingestion workflow](#osdu-ingestion-workflow)
* [API](#api)

## Introduction

The OSDU R2 Ingestion service starts ingestion of OSDU documents, namely the OSDU Work Products,
Work Product Components, and Files. The Ingestion service is basically a wrapper around the [OSDU R2
Workflow service] and performs preliminary work before starting actual ingestion. Preliminary work
can include fetching file location data or validating the manifest.

## System interactions

The Ingestion service in the OSDU R2 Prototype provides two ingestion workflows.

The _Default Ingestion_ workflow is designed to ingest files without metadata. Per each request,
only one file is ingested.

The _OSDU Ingestion_ workflow is designed to ingest multiple files with metadata associated with
them. The metadata must be passed as an OSDU WorkProductLoadManifestStagedFiles manifest in the
request body and must contain an OSDU Work Product and associated Work Product Components and Files.

The following sections discuss the specifics of the workflows.

### Default Ingestion workflow

The Default Ingestion workflow is designed to ingest one file per request. Before submitting a file
for ingestion, the user needs to upload the file to the system. For that purpose, the user needs to
obtain a signed URL from the OSDU R2 Delivery service, and then upload their file by the URL.

For more information on uploading files to the system, consult the [OSDU R2 Delivery service
documentation].

The Default Ingestion workflow starts upon a call to the `/submit` endpoint. The following diagram
shows this workflow.

![OSDU_R2_Ingestion_Service_submit](https://user-images.githubusercontent.com/21691607/76414508-2252f280-63a0-11ea-83a3-237709c2fb0e.png)

Upon a `/submit` request:

1. Validate the incoming request.
    * Verify the authentication token. If the token is missing or invalid, and then respond with the
    `401 Unauthorized` status.
    * Verify the partition ID. If the partition ID is missing, invalid or doesn't have assigned user
    groups, and then respond with the `400 Bad Request` status.
    * Verify `FileID`. Respond with the `400 Bad request` status and the `Missing required field
    FileID` message if a `FileID` isn't provided.
    * Verify `DataType`. Respond with the `400 Bad request` status if the `DataType` is an empty
    string or consists of whitespaces.
    > `DataType` can contain any string. If the string is not "well_log", then the data type is
    > treated as "opaque". During the next steps in the ingestion flow, the Opaque Ingestion DAG
    > will run for any `DataType` but "well_log".
2. Query the Delivery service's `/getFileLocation` API endpoint to obtain a direct link to the file
by `FileID`. The Delivery service will verify whether the `FileID` field exists in the database and
will fetch the file location data. The following flows are possible for the Delivery service:
    * Respond with the `400 Bad request` status and the `Missing required field FileID` message if
    an ID wasn't provided.
    * Respond with the `Driver` and `Location` for the requested `FileID`.
3. Query the Workflow service's `/startWorkflow` API endpoint with the workflow type "ingest". Pass
the file location in the context.
4. Receive the workflow ID from the Workflow service, and then return the ID to the user or app that
started ingestion.

### OSDU Ingestion workflow

The OSDU Ingestion workflow is designed to ingest .las files.

The OSDU ingestion workflow has a dedicated `/submitWithManifest` endpoint. The following diagram
shows the workflow.

![OSDU_R2_Ingestion_Service_submitWithManifest](https://user-images.githubusercontent.com/21691607/76414553-372f8600-63a0-11ea-8aaa-1b7260d15cfc.png)

The workflow is the following:

1. Validate the incoming request.
    * Verify the authentication token. If the token is missing or invalid, and then respond with the
    `401 Unauthorized` status.
    * Verify the partition ID. If the partition ID is missing, invalid or doesn't have assigned user
    groups, and then respond with the `400 Bad Request` status.
    * Validate the manifest. If the manifest doesn't correspond to the OSDU
    `WorkProductLoadManifestStagedFiles` schema stored in the database, fail ingestion, and then
    respond with an HTTP error.
2. Query the Workflow service's `/startWorkflow` API endpoint with the "osdu" workflow type and the
manifest added in the request's `Context` property.
3. Return the workflow ID received from the Workflow service.

## API

The Ingest service's API includes the following endpoints in the OSDU R2 Prototype:

* `/submit`, external
* `/submitWithManifest`, external

General considerations related to querying the Ingestion API:

* Each endpoint must receive the authorization token in the "Authorization" header. Example:
`"Authorization": "Bearer {token}"`
* Each endpoint must receive the partition ID in the "Partition-ID" header. Example:
`"Partition-Id: "default_partition"`
* The request and response Content Type is always "application/json"

### POST /submit

Starts a new ingestion process and carries out necessary operations depending on the file type. The
operations include obtaining file location data from the OSDU R2 Delivery service.

The current implementation of the endpoint supports ingestion of any file types.

#### Incoming request

**Request body**

| Property   | Type     | Description                                                 |
| ---------- | -------- | ----------------------------------------------------------- |
| `FileID`   | `String` | Unique ID of the file                                       |
| `DataType` | `String` | Type of file. Supported data types: "well_log" and "opaque" |

> **Note**: `DataType` can be any string. If the `DataType` value is not "well_log", then it's
> treated as the "opaque" data type. `DataType` cannot contain only whitespaces.

#### Response

| Property     | Type     | Description                                                        |
| ------------ | -------- | ------------------------------------------------------------------ |
| `WorkflowID` | `String` | Unique ID of the workflow that was started by the Workflow service |

## Internal requests

Inside the OSDU R2, the Ingestion service queries the Delivery service's `/getFileLocation` API
endpoint. The information retrieved from the Delivery API will be added to the Context and passed to
the Workflow service afterwards.

### POST /submitWithManifest

The `/submitWithManifest` API endpoint starts the OSDU ingestion process for the OSDU Work Product,
Work Product Components, and Files passed in the OSDU `WorkProductLoadManifestStagedFiles` manifest.

Differently from the `/submit` endpoint, the request body for `/submitWithManifest` doesn't need to
contain a `FileID` and `DataType`.

The list of file IDs must be added to the manifest's `Files` property. The `DataType` property
defaults to "well_log" for all files.

#### Incoming request body

| Property                | Type     | Description                                                                                                                        |
| ----------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `WorkProduct`           | `Object` | OSDU Work Product with **ResourceTypeID**, **ResourceSecurityClassification**, **Data**, and **ComponentsAssociativeID** properties                |
| `WorkProductComponents` | `Array`  | List of OSDU Work Product Components. Each WPC contains at least **ResourceTypeID**, **ResourceSecurityClassification**, **AssociativeID**, **FileAssociativeIDs**, and **Data** properties |
| `Files`                 | `Array`  | List of OSDU Files. Each File contains at least **ResourceTypeID**, **ResourceSecurityClassification**, **AssociativeID**, and **Data** properties |

#### Response body

| Property      | Type     | Description                                                       |
| ------------ | -------- | ------------------------------------------------------------------ |
| `WorkflowID` | `String` | Unique ID of the workflow that was started by the Workflow service |

## Validation

The Ingestion service's current implementation performs a general check of the validity of the
incoming authorization token and partition ID. Also, the service checks if the `FileID` property is
provided. For OSDU Ingestion workflow, the service also validates the manifest.

In OSDU R2 Prototype, the service doesn't perform any verification whether a file upload happened.

## GCP implementation

For development purposes, it's recommended to create a separate GCP Identity and Access Management
service account. It's enough to grant the **Service Account Token Creator** role to the development
service account.

Obtaining user credentials for Application Default Credentials isn't suitable in this case because
signing a blob is only available with the service account credentials. Remember to set the
`GOOGLE_APPLICATION_CREDENTIALS` environment variable. Follow the [instructions on the Google
developer's portal][application-default-credentials].

### Persistence layer

The GCP implementation contains two mutually exclusive modules to work with the persistence layer.
Presently, OSDU R2 connects to legacy Cloud Datastore for compatibility with the current OpenDES
implementation. In the future OSDU releases, Cloud Datastore will be replaced by a Cloud Firestore
implementation that's already available in the project.

* The Cloud Datastore implementation is located in the **provider/ingest-gcp-datastore** folder.
* The Cloud Firestore implementation is located in the **provider/ingest-gcp** folder.

[OSDU R2 Workflow service]: ../os-workflow/README.md
[OSDU R2 Delivery service documentation]: ../os-delivery/README.md
[WorkProductLoadManifestStagedFiles]: https://gitlab.opengroup.org/osdu/open-test-data/blob/master/rc-1.0.0/3-schemas/WorkProductLoadManifestStagedFiles.json
[application-default-credentials]: https://developers.google.com/identity/protocols/application-default-credentials#calling
