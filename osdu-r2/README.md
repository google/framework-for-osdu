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
    * [Delivery service](#delivery-service)
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
cloud platform.

The high-level design of the OSDU R2 Prototype is available in the [Open Group Community Wiki].

## Terms and definitions

The following table defines the terms or clarifies the meaning of the words in this document.

| Property     | Description                                                                                                        |
| ------------ | ------------------------------------------------------------------------------------------------------------------ |
| ODES         | OpenDES, the open source version of the DELFI Data Ecosystem that is developed and supported by Schlumberger.      |
| Landing zone | Location in a cloud platform where files for ingestion are loaded. Consists of the Driver and Location properties. |
| Driver       | Description of where a file was loaded by the user. Example: "GCS" (Google Cloud Storage)                          |
| Location     | Direct URI to file in cloud platform storage, such as a GCS bucket.                                                |

> **Note**: The Driver and Location are used to allow direct access to the file to the internal OSDU R2 services.
> **Note**: The Location doesn't necessarily store the signed URL by which the user uploads their file to the system.

## Intention

The OSDU R2 Prototype focuses on the implementation of the OSDU-compatible ingestion process. More specifically, the 
intent of the OSDU R2 Prototype is to:

* Provide a unified ingestion flow based on the ingestion flows from OSDU Release 1 and the DELFI Data Ecosystem
* Refactor the orchestration implementation of the DELFI Data Ecosystem
* Develop an orchestration basis for different kinds of OSDU workflows, including the interactions with OSDU, storage
and indexing workflows, and domain-specific ingestion workflows for different file types

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

* Delivery
* Ingestion
* Workflow

Besides the core services necessary for ingestion, the OSDU R2 Prototype also introduces changes to the DELFI Storage
service in order to support the new ingestion flow.
    
The OSDU R2 Prototype orchestration implementation is available as a Workflow Engine, which encompasses the following:

* OSDU Ingestion DAG
* Opaque Ingestion DAG
* Stale Jobs Scheduler
* Workflow Status Operator
* Airflow Workflow Sensor Operator

## Services definitions

The following sections discuss the implementation details of the services developed for the OSDU ingestion flow.

The general preconditions of OSDU R2 services implementation are:

* Most services provide both external and internal API endpoints. The third-party applications can query only the
external API endpoints, while the internal API endpoints can only be queried by OSDU services.
* Each service's external APIs need to receive a JSON Web Token (JWT). The future implementations of the services might
be based on the token exchange as part of the security model.
* Each service's external APIs need to receive the DELFI partition ID to which the user has access.

### Ingestion service

The OSDU R2 Prototype introduces two ingestion workflow types.

* **OSDU Ingestion workflow**. This ingestion process is based on the implementation of the OSDU R1 Ingestion service
and targets at processing multiple files with metadata added as OSDU Work Product and Work Product Components to the
ingestion manifest.
* **Default Ingestion workflow**. This ingestion process aims at processing a single file per request. The 
OSDU-compatible metadata (the OSDU WorkProduct manifest) isn't added to the request.

**Implementation**: [ingest](./os-ingest) <br>
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

The Workflow service determines and configures any business workflow to run by the Workflow Engine (Apache Airflow).

In the OSDU R2 Prototype, the Workflow service queries Apache Airflow to start specific ingestion flows depending on the
workflow type and data type.

**Implementation**: [os-workflow](./os-workflow) <br>
**Description and workflow**: [OSDU Workflow Service](./os-workflow/README.md)

#### API

**POST /startWorkflow**

The `/startWorkflow` endpoint starts a new workflow of the specific type depending on the data added to the request. 
Unavailable for external requests. Only internal OSDU services can query this API endpoint.

**POST /workflowID**

The `/workflowID` endpoint returns the current status of the workflow job stored in the database. Available for external
requests.

### Delivery service

The Delivery service provides internal and external APIs to let OSDU services and third-party applications query file 
location data.

**Implementation**: [os-delivery](./os-delivery) <br>
**Description and workflow**: [OSDU R2 Prototype Delivery Service](./os-delivery/README.md)

#### Delivery service API

**POST /getLocation**

The `/getLocation` endpoint returns a signed URL to the third-party application. The signed URL is the path to the file
to be uploaded to the landing zone. This endpoint is available for both internal and external requests.

**POST /getFileLocation**

The `/getFileLocation` endpoint returns the file location information, which includes the Driver and Location. Only
internal OSDU services can query this endpoint.

**POST /getFileList**

The `/getFileList` endpoint returns the paginated results of the file records from the database to let OSDU services 
know whether a file was uploaded by the user or not. Only internal OSDU services can query this endpoint.

> **Note**: The `getFileList` endpoint isn't used in the OSDU R2 Prototype.

### Storage service

The OSDU R2 Prototype Storage service is an extension of the DELFI Storage service designed to store extra non-indexed
metadata with key-value string parameters with each record.

In the OSDU R2 Prototype implementation, the Storage service's `/CreateRecord` endpoint adds the workflow and file IDs
to the file records in the database.

#### API

**POST /CreateRecord**

The `/CreateRecord` endpoint creates a record in the database for each uploaded file. This is an existing API and is
updated to store extra non-indexing metadata fields with the records. Unavailable for external requests.

**POST /listRecords**

The `/listRecords` endpoint searches the existing records by metadata. Unavailable for external requests.

### Workflow Engine (Apache Airflow)

The Workflow Engine is basically an implementation of Apache Airflow that handles pipeline processing.

#### Opaque Ingestion DAG

The Opaque Ingestion DAG carries out ingestion of the opaque data type. The DAG receives files for ingestion and creates
records for them in the database.

#### OSDU Ingestion DAG

The OSDU Ingestion DAG is partly based on the implementation of the [OSDU R1 Ingestion service](../compatibility-layer).
The DAG carries out ingestion of OSDU Files with Work Product and Work Product Components metadata. The manifest
validation is performed in the Ingestion service, whereas the DAG runs the necessary tasks to ingest files from the
manifest.

#### Stale Jobs Scheduler

The Stale Jobs Scheduler is an operator designed to run at an N minutes interval to verify the current status of the
submitted workflow. For the workflows that have a **submitted** or **running** status in the database but that have
failed during execution, the Stale Jobs Scheduler sets their status to **failed**.

#### Workflow Status Operator

The Workflow Status Operator is a custom Airflow operator that updates the status of the submitted workflows in the
database. This operator is called directly by the Airflow DAGs.

## OSDU R2 Prototype Ingestion workflow

The OSDU R2 Prototype implementation introduces two ingestion workflow types that both consist of the following phases:

1. File upload
2. Ingestion
3. Pipeline processing (DAG)

### 1. File upload

The first phase of OSDU R2 ingestion concerns file uploading to the system by a signed URL that the user obtains from
the OSDU Delivery Service. The user fully controls file upload. In OSDU R2 Prototype, services do not verify whether a
file was uploaded to the landing zone or not.

File upload workflow:

1. The client application sends an HTTP request to the Delivery Service to get a file location.
    * The Delivery service creates a signed URL for the file.
    * In the GCP implementation, the Delivery service queries Google Cloud Storage to generate a signed URL.
2. The Delivery service creates a file upload record in the database.
3. The Delivery service returns a signed URL to the client.
4. The client uploads a file to the landing zone by the signed URL. The Delivery Service does not verify whether the
file was uploaded or not.

### 2. Ingestion

The ingestion phase consists of the following steps:

1. The client application submits data for ingestion to the OSDU Ingestion Service.

    > **Note**: The ingestion request may contain a manifest with a list of files and the metadata added as OSDU Work
    > Product and Work Product Components. For the OSDU Ingestion workflow, Files are presented as a list of file IDs.

2. The Ingestion service queries the Delivery service to obtain the files by the signed URLs. The Ingestion service
request contains a list of file IDs.
3. The Delivery service returns the file location data &mdash; Driver and Location &mdash; to the Ingestion service.
4. Upon receiving a file location, the Ingestion service submits a new ingestion job with the context to the Workflow
service. The context might include:
    * File location
    * Workflow ingestion type &mdash; "ingest" or "osdu"
    * Manifest if the workflow type is "osdu"
5. The Workflow service queries the database to understand what Airflow DAG should be started.
6. The Workflow service submits a new workflow job with the DAG type, file type, and context.
7. The Workflow service stores the workflow job ID in the database, and then returns the workflow job ID to the 
Ingestion service.
8. The Ingestion service returns the workflow job ID to the user. The user can eventually submit the workflow job ID to
the Workflow service to learn the status of the current workflow.

## Google Cloud Platform implementation

The OSDU R2 Prototype uses the following Google Cloud Platform services:

* Cloud Firestore
* Cloud Datastore
    > **Note**: The OSDU GCP project uses Cloud Datastore for compatibility reasons to work with the ODES services. The
      current OSDU implementation includes code to work with Firestore or Datastore. However, in the future releases of
      OSDU, the Cloud Datastore implementation will be removed.
    > **Note**: Cloud Datastore creates the same collections and indexes as Cloud Firestore.
* Cloud Compose
* Cloud Storage (GCS)

### Cloud Firestore collections

Cloud Firestore creates the following collections in the database to store various data in OSDU R2 Prototype.

#### `file-locations`

The `file-locations` collection stores the file documents by file ID. Each document stores the following information.

| Property  | Type     | Description                                     |
| --------- | -------- | ----------------------------------------------- | 
| FileID    | `String` | Unique file ID used as key to store file data   |
| Driver    | `String` | Description of the place where files are loaded |
| Location  | `String` | Direct URI to the file                          |
| CreatedAt | `String` | Timestamp when the record was created           |
| CreatedBy | `String` | User ID, not supported in the OSDU R2 Prototype |

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

| Property     | Type     | Description                                                                  |
| ------------ | -------- | ---------------------------------------------------------------------------- | 
| WorkflowID   | `String` | ID of the started workflow                                                   |
| AirflowRunID | `String` | ID of the Airflow process                                                    |
| Status       | `String` | Current status of a workflow &mdash; submitted, running, finished, or failed |
| SubmittedAt  | `String` | Timestamp when the workflow was submitted to Airflow                         |
| SubmittedBy  | `String` | User ID, isn't supported in the OSDU R2 Prototype                            |

[Open Group Community Wiki]: https://community.opengroup.org/osdu/documentation/-/wikis/OSDU-(C)/Design-and-Implementation/Ingestion-and-Enrichment-Detail/R2-Ingestion-Workflow-Orchestration-Spike