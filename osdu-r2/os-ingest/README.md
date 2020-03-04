# OSDU R2 Ingestion Service

## Contents

* [Introduction](#introduction)
* [System interactions](#system-interactions)
    * [Default ingestion workflow](#default-ingestion-workflow)
    * [OSDU ingestion workflow](#osdu-ingestion-workflow)
* [API](#api)

## Introduction

The OSDU R2 Ingestion service starts ingestion of OSDU documents on a per-file basis. The
implementation of the Ingestion service is a wrapper around the [OSDU R2 Workflow service] and
performs preliminary work such as fetching file location data or validating the manifest.

## System interactions

The Ingestion service in the OSDU R2 Prototype provides two ingestion workflows.

The _Default Ingestion_ workflow is designed to ingest files without metadata. Per each request,
only one file is ingested in the Default Ingestion flow.

The _OSDU Ingestion_ workflow is designed to ingest multiple files with metadata associated with
them. The metadata must be passed as an OSDU WorkProductLoadManifestStagedFiles manifest in the
request body and must contain an OSDU Work Product and associated Work Product Components and Files.

The following sections discuss the specifics of the workflows.

### Default Ingestion workflow

The Default Ingestion workflow is designed to ingest one file per request. Before submitting a file
for ingestion, the user needs to upload the file to the system. For that purpose, the user needs to
obtain a URL from the OSDU R2 File service. By the URL, the user will be able to upload their file.

For more information on uploading files to the system, consult the [OSDU R2 File service
documentation].

The Default Ingestion workflow starts upon a call to the `/submit` endpoint. The following diagram
shows this workflow at a high level.

![OSDU R2 IngestService submit](https://user-images.githubusercontent.com/21691607/75542671-ee371380-5a28-11ea-970b-6d9b93ac8f6f.png)

Upon a `/submit` request:

1. Validate the incoming request.
    * Verify the authorization token. Fail ingestion if the token is missing or invalid, and then
    respond with the HTTP error `401 Unauthorized`.
    * Verify the partition ID. Fail ingestion if the partition ID is missing or invalid, and then
    respond with the HTTP error `401 Unauthorized`.
    * Verify FileID. Respond with the `400 Bad request` status and the `Missing required field
    FileID` message if `FileID` isn't provided.
    * Verify DataType. Respond with the `400 Bad request` status and a message:
    `Missing required field DataType` if `DataType` isn't provided, or `Incorrect DataType field` if
    `DataType` is not "opaque" or "well_log".
2. Query the File service's `/getFileLocation` API endpoint to obtain a direct link to the file by
`FileID`. The File service will verify whether the `FileID` field exists in the database and will
fetch the file location data. The following flows are possible for File service:
    * Respond with the `400 Bad request` status and the `Missing required field FileID` message if
    an ID wasn't provided.
    * Respond with the Driver and Location for the requested `FileID`.
3. Query the Workflow service's `/startWorkflow` API endpoint with the workflow type "ingest". Pass
the file location in the context.
4. Receive the workflow ID from the Workflow service, and then return the ID to the user or app that
started ingestion.

### OSDU Ingestion workflow

The OSDU Ingestion workflow, unlike the Default Ingestion workflow, is designed to ingest only .las
files.

The OSDU ingestion workflow has a dedicated `/submitWithManifest` endpoint. The following diagram
shows the workflow at the high level.

![OSDU R2 IngestService submitWithManifest](https://user-images.githubusercontent.com/21691607/75542675-eecfaa00-5a28-11ea-91c5-eca66a7c43ce.png)

The workflow is the following:

1. Validate the incoming request.
    * Verify the authorization token. Fail ingestion if the token is missing or invalid, and
    then respond with the HTTP error `401 Unauthorized`.
    * Verify the partition ID. Fail ingestion if the partition is missing or invalid, and then
    respond with the HTTP error `401 Unauthorized`.
    * Validate the manifest. If the manifest doesn't correspond to the OSDU
    `WorkProductLoadManifestStagedFiles` schema stored in the database, fail ingestion and then
    respond with the HTTP error.
2. Query the Workflow service's `/startWorkflow` API endpoint with the "osdu" workflow type and the
manifest added in the request's Context property.
3. Return the workflow ID received from the Workflow service.

## API

The Ingest service's API includes the following endpoints in the OSDU R2 Prototype:

* `/submit`, external
* `/submitWithManifest`, external

General considerations related to querying the Ingestion API:

* Each endpoint must receive the authorization token in the "Authorization" header. Example:
`"Authorization": "Bearer {token}"`
* Each endpoint must receive the partition ID in the "Partition-ID" header. Example:
`"Partition-Id: "systemdefault8apre7h881noa9"`
* The request and response Content Type is always "application/json"
* The request to any endpoint doesn't use any URL parameters

### POST /submit

Starts a new ingestion process and carries out necessary operations depending on the file type. The
operations include obtaining file location data from the OSDU R2 File service.

The current implementation of the endpoint supports ingestion of any file types.

#### Incoming request

**Request body**

| Property   | Type     | Description                                                 |
| ---------- | -------- | ----------------------------------------------------------- |
| `FileID`   | `String` | Unique ID of the file                                       |
| `DataType` | `enum`   | Type of file. Supported data types: "well_log" and "opaque" |

#### Response

| Property     | Type     | Description                                                        |
| ------------ | -------- | ------------------------------------------------------------------ |
| `WorkflowID` | `String` | Unique ID of the workflow that was started by the Workflow service |

## Internal requests

Inside the OSDU R2, the Ingestion service queries the File service's `/getFileLocation` API. The
information retrieved from the API will be added to the Context and passed to the Workflow service
afterwards.

### POST /submitWithManifest

This API endpoint starts the OSDU ingestion process. Differently from the `/submit` endpoint, the
request body for `/submitWithManifest` doesn't need to contain `FileID` and `DataType`.

The list of file IDs must be added to the manifest's `Files` property. The `DataType` property
defaults to "well_log" for all requests.

The request body must contain the OSDU `WorkProductLoadManifestStagedFiles` manifest.

#### Incoming request

**Request body** with `WorkProductLoadManifestStagedFiles` manifest.

| Property                | Type     | Description                                                                                                                        |
| ----------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `WorkProduct`           | `Object` | OSDU Work Product with **ResourceTypeID**, **ResourceSecurityClassification**, **Data**, and **ComponentsAssociativeID** properties                |
| `WorkProductComponents` | `Array`  | List of OSDU Work Product Components. Each WPC contains at least **ResourceTypeID**, **ResourceSecurityClassification**, **AssociativeID**, **FileAssociativeIDs**, and **Data** properties |
| `Files`                 | `Array`  | List of OSDU Files. Each File contains at least **ResourceTypeID**, **ResourceSecurityClassification**, **AssociativeID**, and **Data** properties |

#### Response

| Property      | Type     | Description                                                       |
| ------------ | -------- | ------------------------------------------------------------------ |
| `WorkflowID` | `String` | Unique ID of the workflow that was started by the Workflow service |

## Validation

The Ingestion service's current implementation performs a general check of the validity of the
incoming authorization token and partition ID. Also, the service checks if the `FileID` property is
provided. For OSDU Ingestion workflow, the service also validates the manifest.

In OSDU R2 Prototype, the service doesn't perform any verification whether a file upload happened.

## GCP implementation

For development purposes, it's recommended to create a separate service account.
It's enough to grant the **Service Account Token Creator** role to the development service account.

Obtaining user credentials for Application Default Credentials isn't suitable in this case because
signing a blob is only available with the service account credentials. Remember to set the
`GOOGLE_APPLICATION_CREDENTIALS` environment variable. Follow the [instructions on the Google
developer's portal][application-default-credentials].

### Persistence layer

The GCP implementation contains two mutually exclusive modules to work with the persistence layer.
Presently, OSDU R2 connects to legacy Cloud Datastore for compatibility with the current OpenDES
implementation. In the future, Cloud Datastore will be replaced by the existing Cloud Firestore
implementation that's already available in the project.

•	The Cloud Datastore implementation is located in the provider/ingest-gcp-datastore folder.
•	The Cloud Firestore implementation is located in the provider/ingest-gcp folder.

[OSDU R2 Workflow service]: ../os-workflow/README.md
[OSDU R2 File service documentation]: ../os-file/README.md
[WorkProductLoadManifestStagedFiles]: https://gitlab.opengroup.org/osdu/open-test-data/blob/master/rc-1.0.0/3-schemas/WorkProductLoadManifestStagedFiles.json
