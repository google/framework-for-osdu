# OSDU R2 Ingestion Service

## Contents

* [Introduction](#introduction)
* [System interactions](#system-interactions)
    * [Default ingestion workflow](#default-ingestion-workflow)
    * [OSDU ingestion workflow](#osdu-ingestion-workflow)
* [API](#api)

## Introduction

The OSDU R2 Ingestion service performs ingestion of OSDU documents on a per-file basis. The
implementation of the Ingestion service is a wrapper around the [OSDU R2 Workflow service] and
performs preliminary work such as fetching file location data or validating the manifest.

The current implementation of the Ingestion service provides two API endpoints for submitting files
for ingestion.

## System interactions

The Ingestion service in the OSDU R2 Prototype provides two ingestion workflows. The _default
ingestion workflow_ is designed to ingest files without metadata. The _OSDU ingestion workflow_ is
designed to ingest files with metadata added to the ingestion request as a manifest with OSDU Work
Product, Work Product Components, and Files.

The following sections discuss the specifics of the workflows.

### Default Ingestion workflow

The Default Ingestion workflow is designed to ingest one file per `/submit` request. Before
submitting a file for ingestion, the user needs to upload the file to the system. For that purpose,
the user needs to obtain a URL from the OSDU R2 File service. By thte URL, the user will be able to
upload their file.

For more information on uploading files to the system, consult the [OSDU R2 File service
documentation].

The Default Ingestion workflow starts upon a call to the `/submit` endpoint. The following diagram
shows this workflow at a high level.

![OSDU_R2_Prototype_Default_Ingestion_Service](https://gitlab.osdu-gcp.dev/odes/os-ingest/uploads/cfd9c49e0b767372cd575475435405d3/OSDU_R2_Prototype_Default_Ingestion_Service.png)

Upon a `/submit` request:

1. Validate the incoming request.
    * **Verify the authorization token**. Fail ingestion if the token is missing or invalid, and
    then respond with the HTTP error `401 Unauthorized`.
    * **Verify the partition ID**. Fail ingestion if the partition ID is missing or invalid, and
    then respond with the HTTP error `401 Unauthorized`.
    * **Verify FileID**. Respond with the `400 Bad request` status and the `Missing required field
    FileID` message if `FileID` isn't provided.
    * **Verify DataType**. Respond with the `400 Bad request` status and a message:
    `Missing required field DataType` if `DataType` isn't provided, or `Incorrect DataType field` if
    `DataType` is not "opaque" or "well_log".
2. Query the File service's `/getFileLocation` API to obtain a direct link to the file by `FileID`.
The File service will verify whether the `FileID` field exists in the database and will
fetch the file location data. The following flows are possible for File service:
    * Respond with the `400 Bad request` status and the `Missing required field FileID` message if
    an ID wasn't provided.
    * Respond with the Driver and Location for the requested `FileID`.
3. Query the Workflow service's `/startWorkflow` API with the workflow type "ingest". Pass the file
location in the context.
4. Receive the workflow ID from the Workflow service, and then return the ID to the user or app that
started ingestion.

### OSDU Ingestion workflow

The OSDU Ingestion workflow, unlike the Default Ingestion workflow, is designed to ingest .las files.

The OSDU ingestion workflow has a dedicated `/submitWithManifest` endpoint. The following diagram
shows the workflow at the high level.

![OSDU_R2_OSDU_Ingestion](https://gitlab.osdu-gcp.dev/odes/os-ingest/uploads/a7e99f08b11f8d28970b8e7e91302ba9/OSDU_R2_OSDU_Ingestion.png)

The workflow is the following:

1. Validate the incoming request.
    * **Verify the authorization token**. Fail ingestion if the token is missing or invalid, and
    then respond with the HTTP error `401 Unauthorized`.
    * **Verify the partition ID**. Fail ingestion if the partition is missing or invalid, and then
    respond with the HTTP error `401 Unauthorized`.
    * **Validate the manifest**. If the manifest doesn't correspond to the OSDU
    `WorkProductLoadManifestStagedFiles` schema, fail ingestion and then respond with the HTTP
    error.
2. Query the Workflow service's `/startWorkflow` API with the "osdu" workflow type and the manifest
added in the request's Context.
3. Return the workflow ID that the Workflow service returned.

## API

### POST /submit

Starts a new ingestion process and carries out necessary operations depending on the file type. The
operations include obtaining file location data from the OSDU R2 File service.

The current implementation of the endpoint supports ingestion of any file types.

**URL parameters**: none <br/>
**Description**: The request must contain the authorization token in the header as `Authorization:
Bearer {token}` and the DELFI partition ID. The request body must contain the `FileID` and
`Partition-Id` properties.
**HTTP Content Type**: application/json
**HTTP Return Content Type**: application/json

#### Incoming request

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
request body for `/submitWithManifest` requests doesn't need to contain `FileID` and `DataType` as
properties.

The list of file IDs must be added to the manifest's `Files` property. The `DataType` property
defaults to "well_log" for all requests.

**URL parameters**: none
**Description**: The request header must contain the authorization token and partition ID. The
request body must contain the OSDU `WorkProductLoadManifestStagedFiles` manifest.
**HTTP Content Type**: application/json
**HTTP Return Content Type**: application/json

#### Incoming request

**Headers**

| Property      | Description                                                    |
| ------------- | -------------------------------------------------------------- |
| Authorization | JWT as `Bearer {token}`                                        |
| Partition-Id  | DELFI partition ID, for example, `systemdefault8apre7h881noa9` |

**Request body** (`WorkProductLoadManifestStagedFiles` manifest)

| Property                | Type     | Description                                                                                                                        |
| ----------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `WorkProduct`           | `Object` | OSDU Work Product with ResourceTypeID, ResourceSecurityClassification, Data, and ComponentsAssociativeID properties                |
| `WorkProductComponents` | `Array`  | List of OSDU Work Product Components. Each WPC contains at least ResourceTypeID, ResourceSecurityClassification, AssociativeID, FileAssociativeIDs, and Data properties |
| `Files`                 | `Array`  | List of OSDU Files. Each File contains at least ResourceTypeID, ResourceSecurityClassification, AssociativeID, and Data properties |

#### Response

| Property      | Type     | Description                                                       |
| ------------ | -------- | ------------------------------------------------------------------ |
| `WorkflowID` | `String` | Unique ID of the workflow that was started by the Workflow service |

## Validation

The Ingestion service's current implementation performs a general check of the validity of the
incoming authorization token and partition ID. Also, the service checks if the `FileID` property is
provided. For OSDU Ingestion workflow, the service also validates the manifest.

In the OSDU R2, the service doesn't perform any verification whether a file upload happened.

[OSDU R2 Workflow service]: https://gitlab.osdu-gcp.dev/odes/os-workflow
[OSDU R2 File service documentation]: https://gitlab.osdu-gcp.dev/odes/os-file/blob/develop/README.md
[WorkProductLoadManifestStagedFiles]: https://gitlab.opengroup.org/osdu/open-test-data/blob/master/rc-1.0.0/3-schemas/WorkProductLoadManifestStagedFiles.json
