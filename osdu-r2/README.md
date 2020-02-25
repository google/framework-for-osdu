# OSDU Release 2 Prototype

## Contents

* [Introduction](#introduction)
* [Terms and definitions](#terms-and-definitions)
* [Intention](#intention)
    * [Apache Airflow](#apache-airflow)
* [New services and implementations](#new-services-and-implementations)
* [Service definitions](#services-definitions)
    * [Ingestion service](#ingestion-service)
    * [Workflow service](#workflow-service)
    * [File service](#file-service)
    * [Storage service](#storage-service)
* [Workflow Engine](#workflow-engine-apache-airflow)
    * [Opaque Ingestion DAG](#opaque-ingestion-dag)
    * [OSDU Ingestion DAG](#osdu-ingestion-dag)
    * [Workflow Status Operator](#workflow-status-operator)
    * [Stale Jobs Scheduler](#stale-jobs-scheduler)
* [Google Cloud Platform implementation](#google-cloud-platform-implementation)
    * [Cloud Firestore collections](#cloud-firestore-collections)

## Introduction

The OSDU Release 2 Prototype is an implementation of the OSDU standard and focuses on the ingestion of oil and gas data.

The OSDU R2 is cloud-agnostic &mdash; it provides common implementations that can be deployed and orchestrated on any
cloud provider platform.

The high-level design of the OSDU R2 Prototype is available in the [Open Group Community Wiki].

## Terms and definitions

The following table defines the terms or clarifies the meaning of the words in this document.

| Property     | Description                                                                                          |
| ------------ | ---------------------------------------------------------------------------------------------------- |
| ODES         | The open source version of the DELFI Data Ecosystem that is developed and supported by Schlumberger. |
| Landing zone | The location in a cloud provider's platform where files for OSDU ingestion are loaded. The landing zone consists of the Driver and Location properties. |
| Driver       | A description of where a file was loaded by the user. The Driver is used in combination with the Location to allow direct access to the file. Example: "GCS" |
| Location     | A direct URI to file in storage, such as a GCS bucket. The Location might be different from the signed URL returned to the user, by which the user uploads a file to the landing zone. The Location is used with Driver to allow direct access to the file to the internal OSDU R2 services. |

## Intention

The OSDU R2 Prototype focuses on the implementation of the OSDU-compatible ingestion process. More specifically, the 
intent of the OSDU R2 Prototype is to:

* Provide a unified ingestion flows from OSDU Release 1 and the DELFI Data Ecosystem
* Refactor the orchestration implementation of the DELFI Data Ecosystem
* Develop a workflow orchestration basis for different kinds of OSDU workflows, including the interactions with OSDU, 
storage and indexing workflows, and domain-specific ingestion workflows for different file types

The Apache Airflow implementation of the orchestration will allow for:

* Validation, refinement, and decision making on the required ingestion workflow characteristics and methods
* Provisioning of the basis for design documentation for the broader use cases, such as domain-specific workflows

### Apache Airflow

Apache Airflow is an open source solution for workflow orchestration. The choice of Airflow for orchestrating OSDU R2 
services is dictated by a set of functional and operational requirements listed below:

* **Functional requirements**. The chosen orchestration technology needs to:
    * Support both sequential and parallel execution of tasks
    * Support both synchronous and asynchronous operations
    * Implement error handling features
    * Persist the state
* **Operational requirements**
    * Provide an admin dashboard
    * Provide an option to resume failed workflow jobs
    
## New services and implementations

The OSDU R2 Prototype introduces the following services:

* File
* Ingestion
* Workflow

Besides the core services necessary for ingestion, the OSDU R2 Prototype also introduces several changes to the DELFI 
Storage service in order to support the new ingestion flow.
    
The OSDU R2 Prototype orchestration implementation is available as a Workflow Engine, which encompasses the following 
implementations:

* OSDU Ingestion DAG
* Opaque Ingestion DAG
* Stale Jobs Scheduler
* Workflow Status Operator

## Services definitions

The following sections discuss the implementation details of the services developed for the OSDU ingestion flow.

The general preconditions of OSDU R2 services implementation are:

* Most services provide both external and internal API endpoints. The external API endpoints are implemented to let
third-party applications to query the API, while the internal API endpoints can only be queried by the OSDU services.
* Each service's external APIs need to receive a JSON Web Token (JWT). The future implementations of the services might
be based on the token exchange as part of the security model.
* Each service's external APIs need to receive the ID of a DELFI partition that the user has access to.

### Ingestion service

The OSDU R2 Prototype introduces two ingestion workflow types.

* **OSDU Ingestion workflow**. This ingestion process is based on the implementation of the OSDU R1 Ingestion service
and targets at processing multiple files with metadata added as OSDU Work Product and Work Product Components to the
ingestion manifest. The OSDU Ingestion DAG is discussed in a separate section.
* **Default Ingestion workflow**. This ingestion process aims at processing a single file per request. The 
OSDU-compatible metadata (the OSDU WorkProduct manifest) isn't added to the request.

**Implementation**: [ingest](./os-ingest)

**Detailed information**: [OSDU Ingestion Service](./os-ingest/README.md)

#### API

The Ingestion service provides two endpoints for submitting files for ingestion.

**POST /submit**

The `/submit` endpoint starts ingestion of a file, and then returns the workflow job ID to the user. Available for
external requests.

**POST /submitWithManifest**

The `/submitWithManifest` endpoint starts ingestion of multiple files with metadata added as a Work Product Component
manifest. Available for external requests.

### Workflow service

The Workflow service determines and configures any business workflow to run by Workflow Engine (Airflow).

In the OSDU R2 Prototype, the Workflow service queries Apache Airflow to start specific ingestion flows depending on the
workflow type and data (file) type.

**Implementation**: [os-workflow](./os-workflow)

**Description and workflow**: [ODES Workflow Service](./os-workflow/README.md)

#### API

**POST /startWorkflow**

The `/startWorkflow` endpoint starts a new workflow of the specific type depending on the data added to the request. 
Unavailable for external requests. Only internal OSDU services can query this API endpoint.

**POST /workflowID**

The `/workflowID` endpoint returns the current status of the workflow job stored in the database. Available for external
requests.

### File service

The File service provides internal and external APIs to let OSDU services and third-party applications query file 
location data.

**Implementation**: [os-file](./os-file)

**Description and workflow**: [OSDU R2 Prototype File Service](./os-file/README.md)

#### File service API

**POST /getLocation**

The `/getLocation` endpoint returns a signed URL to the third-party application. The signed URL is the path to the file
to be uploaded to the landing zone. This endpoint is available for both internal and external requests.

**POST /getFileLocation**

The `/getFileLocation` endpoint returns the file location information, which includes the Driver and Location. Only
internal OSDU services can query this endpoint.

**POST /getFileList**

The `/getFileList` endpoint returns the paginated results from the database. The API lets know whether a file was 
uploaded by the user or not. Only internal OSDU services can query this endpoint.

> The `getFileList` endpoint isn't implemented in the OSDU R2 Prototype.

### Storage service

The Storage service is an extension of the ODES Storage service designed to store extra non-indexed metadata with 
key-value string parameters with each record.

In the OSDU R2 Prototype implementation, the Storage service's `CreateRecord` endpoint adds the workflow and file IDs to
the file records in the database.

#### API

**POST /CreateRecord**

The `/CreateRecord` endpoint creates a record in the database for each uploaded file. This is an existing API and is
updated to store extra non-indexing metadata fields with the records. Unavailable for external requests.

**POST /listRecords**

The `/listRecords` endpoint searches the existing records by metadata. Unavailable for external requests.

### Workflow Engine (Apache Airflow)

#### Opaque Ingestion DAG

The Opaque Ingestion DAG carries out ingestion of the opaque data type. The DAG receives files for ingestion and creates
records in the database for new files.

#### OSDU Ingestion DAG

The OSDU Ingestion DAG is partly based on the implementation of the [OSDU R1 Ingestion service](../compatibility-layer).
The DAG carries out ingestion of OSDU Files with Work Product and Work Product Components metadata added to the request.
The manifest validation is performed in the Ingestion service, whereas the DAG runs the necessary tasks to ingest files
from the manifest.

#### Stale Jobs Scheduler

The Stale Jobs Scheduler is an operator designed to run at an N minutes interval to verify the current status of the
submitted workflows. For workflows that have a SUBMITTED or RUNNING status in the database but have failed during
execution, the Stale Jobs Scheduler sets their status to FAILED in the database.

#### Workflow Status Operator

The Workflow Status Operator is a custom Airflow operator that updates the status of the submitted workflows in the
database. This operator is called directly by the DAGs.

## OSDU R2 Prototype Ingestion workflow

The OSDU R2 Prototype implementation introduces two workflows that consist of the following phases:

1. File upload
2. Ingestion
3. Pipeline processing (DAG)

### 1. File upload

The first phase of OSDU R2 ingestion concerns file uploading to the system by a signed URL that the user obtained from
the OSDU File Service. The user fully controls file upload. In OSDU R2 Prototype, services do not verify whether a file
was uploaded to the landing zone or not.

File upload workflow:
1. The client application sends an HTTP request to the File Service to get a file location.
    * The File service creates a signed URL for the file.
    * In the GCP implementation, the File service queries Google Cloud Storage to generate a signed URL.
2. The File service creates a file upload record in the database.
3. The File service returns a signed URL to the client.
4. The client uploads a file to the landing zone by the signed URL. The File Service does not verify whether the file
was uploaded or not.

### 2. Ingestion

The ingestion phase consists of the following steps:

1. The client application submits data for ingestion to the OSDU Ingestion Service.

> The ingestion request may contain a manifest with a list of files and the metadata added as OSDU Work Product and Work 
> Product Components. For the OSDU Ingestion workflow, the Files are represented as a list of file IDs.

2. The Ingestion service queries the File service to obtain the files by the signed URLs. The Ingestion service request
contains a list of file locations.
3. The File service returns the file location data &mdash; Driver and Location &mdash; to the Ingest service.
4. Upon receiving a file location, the Ingestion service submits a new ingestion job with the context to the Workflow
service. The context includes:
    * File location from the File service.
    * Workflow ingestion type &mdash; "ingest" or "osdu".
    * Manifest for the "osdu" workflow type.
5. The Workflow service queries the database to understand what Airflow DAG should be started.
6. The Workflow service submits a new workflow job with the DAG data, file, and context.
7. The Workflow service stores the workflow job ID in the database, and then returns the workflow job ID to the 
Ingestion service.
8. The Ingestion service returns the workflow job ID to the user. The user can store and eventually submit the workflow
job ID to the Workflow service to learn the status of the current workflow.

## Google Cloud Platform implementation

The OSDU R2 Prototype uses the following Google Cloud Platform services:

* Cloud Firestore
* Cloud Compose
* Cloud Storage (GCS)

### Cloud Firestore collections

Cloud Firestore creates the following collections in the database to store various data required by OSDU R2 Prototype to
function.

#### `file-locations`

The `file-locations` collection stores the file documents by file ID. Each document stores the following information.

| Property  | Type     | Description                                                               |
| --------- | -------- | ------------------------------------------------------------------------- | 
| FileID    | `String` | Unique file ID used as key to store file data                             |
| Driver    | `String` | Description of the place where files are loaded                           |
| Location  | `String` | Direct URI to the file                                                    |
| CreatedAt | `String` | Timestamp when the record was created                                     |
| CreatedBy | `String` | User ID. The CreatedBy property isn't supported in the OSDU R2 Prototype. |

#### `dag-selection`

The `dag-selection` collection stores the data necessary to determine the Apache Airflow DAG to run for a specific
workflow.

| Property     | Type     | Description                                                                      |
| ------------ | -------- | -------------------------------------------------------------------------------- |
| WorkflowType | `String` | Supported workflow types. In OSDU R2 Prototype these are "ingest" or "osdu"      |
| DataType     | `String` | Supported data types. In OSDU R2 Prototype these are "well_log" or "opaque"      |
| UserID       | `String` | ID of the user that requested file ingestion. Not supported in OSDU R2 Prototype |
| DAGName      | `String` | The name of an Airflow DAG to run                                                |

#### `workflow-status`

The `workflow-status` collection stores the current status and some additional properties of a started workflow job.

| Property     | Type     | Description                                                                   |
| ------------ | -------- | ----------------------------------------------------------------------------- | 
| WorkflowID   | `String` | ID of the started workflow                                                    |
| AirflowRunID | `String` | ID of the Airflow process                                                     |
| Status       | `String` | Current status of a workflow &mdash; SUBMITTED, RUNNING,  FINISHED, or FAILED |
| SubmittedAt  | `String` | Timestamp when the workflow was submitted to Airflow                          |
| SubmittedBy  | `String` | User ID. The SubmittedBy property isn't supported in the ODES R2 Prototype    |

[Open Group Community Wiki]: https://community.opengroup.org/osdu/documentation/-/wikis/OSDU-(C)/Design-and-Implementation/Ingestion-and-Enrichment-Detail/R2-Ingestion-Workflow-Orchestration-Spike