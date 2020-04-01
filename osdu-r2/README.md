# OSDU Release 2 Prototype

## Contents

* [Introduction](#introduction)
* [Terms and definitions](#terms-and-definitions)
* [Intention](#intention)
* [Technology stack](#technology-stack)
* [Auditing and logging](#auditing-and-logging)
* [OSDU R2 services and components](#osdu-r2-services-and-components)
* [Service definitions](#services-definitions)
    * [Ingestion service](#ingestion-service)
    * [Workflow service](#workflow-service)
    * [Delivery service](#delivery-service)
    * [Storage service](#storage-service)
* [Workflow Engine (Apache Airflow)](#workflow-engine-apache-airflow)
    * [Opaque Ingestion DAG](#opaque-ingestion-dag)
    * [Manifest Ingestion DAG](#manifest-ingestion-dag)
    * [Workflow Status Operator](#workflow-status-operator)
    * [Stale Jobs Scheduler](#stale-jobs-scheduler)
* [Google Cloud Platform implementation](#osdu-r2-google-cloud-platform-implementation)
    * [Cloud Firestore collections](#cloud-firestore-collections)

## Introduction

The OSDU Release 2 Prototype is an implementation of the OSDU standard and is designed to ingest and deliver upstream
oil and gas data.

The OSDU R2 is cloud-agnostic &mdash; it provides common implementations that can be deployed and orchestrated on any
cloud platform.

## Terms and definitions

The following table defines the terms introduced os used in OSDU R2.

| Property     | Description                                                                                             |
| ------------ | ------------------------------------------------------------------------------------------------------- |
| SRN          | Subsurface Data Universe Resource Number, an identifier of OSDU types that has the for `srn:namespace:type:unique_key:version`. |
| Manifest     | The JSON data that includes an OSDU Work Product and associated Work Product Components and Files. The manifest is validated with the OSDU WorkProductLoadManifestStagedFiles schema. |
| Work Product | A package of data items prepared by an application for upload to OSDU. A Work Product consists of Work Product metadata and one or more Work Product Components. |
| Work Product Component | A typed, smallest, independently usable unit of business data content transferred to OSDU as part of a Work Product. Each Work Product Component consists of one or more data content units known as OSDU Files. |
| File         |  A concrete file uploaded to the system. Files in the OSDU manifest are associated with a Work Product Component. |
| Landing zone | Location in a cloud platform where files for ingestion are loaded. Consists of the Driver and Location. |
| Driver       | Description of where a file was loaded by the user. Example: "GCS" (Google Cloud Storage).              |
| Location     | Direct URI to file in cloud platform storage, such as a GCS bucket.                                     |
| Workflow     | Unique business process to be carried out by the system. In OSDU R2, ingestion is one such process.     |
| Opaque data  | Opaque is used when referred to file types that are ingested by the system. If the system doesn't receive "well_log" as the data type during ingestion, the data is treated as "opaque". That is, "opaque" means any data type. |
| Airflow      | An orchestration platform to author, schedule, and monitor workflows.                                   |
| DAG          | Directed Acyclic Graph, a set of tasks that Apache Airflow runs to perform workflow steps.              |

> **Note**: The Driver and Location are used to allow direct access to the file to the internal OSDU R2 services.
> **Note**: The Location doesn't necessarily store the signed URL by which the user uploads their file to the system.

Additional terms introduced by OpenDES.

| Property | Description                                                                                            |
| -------- | ------------------------------------------------------------------------------------------------------ |
| ODES     | OpenDES, the open source version of the DELFI Data Ecosystem. Developed and supported by Schlumberger. |
| ACL      | A group of users that have access to the DELFI record. Part of the ODES record structure.              |
| Legal    | Consists of a list of legal tags associated with the record and a list of relevant data countries. Part of the ODES record structure. |

## Intention

The OSDU R2 Prototype focuses on the implementation of the OSDU-compatible ingestion process. More specifically, the
intent of the OSDU R2 Prototype is to:

* Provide a unified ingestion flow based on the ingestion flows from OSDU Release 1 and the DELFI Data Ecosystem
* Refactor the orchestration implementation of the DELFI Data Ecosystem
* Develop an orchestration basis for different kinds of OSDU workflows, including the interactions with OSDU, storage
and indexing workflows, and domain-specific ingestion workflows for different file types

The orchestration implementation is based on Apache Airflow, which allows for:

* Validation, refinement, and decision making on the required ingestion workflow characteristics and methods
* Provisioning of the basis for design documentation for the broader use cases, such as domain-specific workflows

Apache Airflow is an open source solution for workflow orchestration. The choice of Airflow for orchestrating OSDU R2
services is dictated by a set of functional and operational requirements listed below:

* **Functional requirements**
    * Support for both sequential and parallel execution of tasks
    * Support for both synchronous and asynchronous operations
    * Implementation of error handling features
    * State persistence
* **Operational requirements**
    * Available admin dashboard for viewing and handling tasks
    * Possibility to resume failed workflow jobs
    
## Technology stack

> OSDU R2 uses Maven's BOM to handle the versions of dependencies, which is why some libraries in the list are specified
> without versions. These dependencies are pulled out according to the Spring Boot 2.2.5 version.

The OSDU GCP R2 is based on the following technology stack:

* [Java 8]
* [Python 3.6+], used for the Apache Airflow DAGs and the Python SDK
* [Google Cloud SDK]
* [Terraform 0.12.8+]
* [Spring Boot 2.2.5]
* [Spring Boot Cloud Hoxton.SR3]
* [Project Lombok]
* [Jackson]
* [JavaX Inject 1]
* [Guava 28.2-jre]
* [GSON 2.8.5]
* [Lettuce 4.5.0.Final]
* [Swagger Core JAX RS Project Setup 1.5.X]
* [Google HTTP Client 1.31.0]
* [Auth0 Java JWT 3.8.1]
* [JSON Web Token 0.9.1]
* [Elasticsearch 6.6.2]
* [Elasticsearch REST Client 6.6.2]
* [Elasticsearch REST High Level Client 6.6.2]
* [JUnit]
* [Mockito 2.0.2-beta]
* [Powermock 2.0.2]
* [MapStruct 1.3.1 Final]

Java build dependencies:

* [Maven Checkstyle Plugin 3.1.0]
* [Maven PMD Plugin 3.12.0]
* [JaCoCo Maven Plugin 0.8.4]
* [Spotbugs Maven Plugin 3.1.12]
* [Maven Surefire PLugin]

OSDU GCP R2 extensively uses the following Google Cloud Platform services:

* [Cloud Run]
* [Cloud Storage]
* [Cloud Firestore]
* [Cloud Datastore], used only for compatibility reasons; Datastore is to be replaced by Firestore
* [Cloud SQL]
* [Cloud Functions]
* [Compute Engine]
* [App Engine]
* [Google Cloud Operations Suite] (Stackdriver)

## Auditing and logging

OSDU R2 uses Spring Boot default logging to record the application state and errors. Google Cloud Platform pulls out the
generated logs from the running application and publishes them to [Google Cloud Operations Suite] (formerly
Stackdriver). 

## OSDU R2 services and components

The OSDU R2 Prototype introduces the following services:

* [Delivery](#delivery-service)
* [Ingestion](#ingestion-service)
* [Workflow](#workflow-service)

Besides the core services necessary for ingestion, the OSDU R2 Prototype also introduces changes to the DELFI Storage
service in order to support the new ingestion flow.

The OSDU R2 Prototype orchestration implementation is available as a Workflow Engine, which encompasses the following
components:

* [Manifest Ingestion DAG](#manifest-ingestion-dag)
* [Opaque Ingestion DAG](#opaque-ingestion-dag)
* [Stale Jobs Scheduler](#stale-jobs-scheduler)
* [Workflow Status Operator](#workflow-status-operator)
* [Finished Workflow Sensor Operator](#finished-workflow-sensor-operator)

## Services definitions

The following sections discuss the implementation details of the services developed for the OSDU ingestion flow. The
general considerations of OSDU R2 services implementation are:

* Most services provide both external and internal API endpoints. The third-party applications can query only the
external API endpoints, while the internal API endpoints can only be queried by OSDU services.
* Each service's internal and external API endpoints need to receive a JSON Web Token (JWT). The future implementations
of the services might be based on the token exchange as part of the security model.
* Each service's internal and external APIs need to receive the DELFI partition ID to which the user has access.

### Delivery service

The Delivery service provides internal and external API endpoints to let OSDU services and third-party applications
query file location or request documents from the system.

The OSDU R2 Delivery service is based on the OSDU R1 Delivery with new endpoints for requesting file location data and a
reworked API for delivery of documents.

**Implementation**: [os-delivery](./os-delivery) <br>
**Description and workflow**: [OSDU R2 Delivery Service](./os-delivery/README.md)

#### Delivery API

**POST /delivery**

The `/delivery` API endpoint returns a list of OSDU data per SRN. If an SRN wasn’t found in the system, it’s returned in
the list of Unprocessed SRNs. The endpoint is open for external requests.

**POST /getLocation**

The `/getLocation` API endpoint returns a signed URL to the third-party application. The signed URL is the path to the
file to be uploaded to the landing zone. This endpoint is open for external requests.

**POST /getFileLocation**

The `/getFileLocation` API endpoint returns the file location information, which includes the Driver and Location. This
endpoint is closed for external requests.

**POST /getFileList**

The `/getFileList` API endpoint returns the paginated results of the file records from the database to let OSDU services
know whether a file was uploaded by the user or not. This endpoint is closed for external requests.

> **Note**: The `getFileList` endpoint isn't used in the OSDU R2 Prototype.

### Ingestion service

The OSDU R2 Prototype introduces two ingestion workflow types.

* **Manifest Ingestion workflow**. This OSDU R2 Ingestion service is partly based on the implementation of the OSDU R1
Ingestion service and targets at processing multiple files with metadata added as OSDU Work Product and Work Product
Components to the ingestion manifest. The Manifest Ingestion flow requires an OSDU manifest added to the request body
with WP, WPC. The Ingestion service only validates this manifest against the schema stored in the database.
* **Default (Opaque) Ingestion workflow**. This ingestion process aims at processing a single file per request. The
OSDU-compatible metadata (the OSDU WorkProduct manifest) isn't added to the request.

The Ingestion service provides two endpoints for submitting files for ingestion. This API carries out necessary
operations depending on the file type and then submits an ingestion workflow to the OSDU Workflow service.

**Implementation**: [ingest](./os-ingest) <br>
**Detailed information**: [OSDU R2 Ingestion Service](./os-ingest/README.md)

#### Ingestion API

The Ingestion service provides two endpoints for submitting files for ingestion.

**POST /submit**

The `/submit` API endpoint starts ingestion of a file, and then returns the workflow job ID to the user. This endpoint
is open for external requests.

**POST /submitWithManifest**

The `/submitWithManifest` API endpoint starts ingestion of multiple files with metadata added as a Work Product and Work
Product Component in the manifest. This endpoint is open for external requests.

### Workflow service

The Workflow service determines and configures any business workflow to run by the Workflow Engine (Apache Airflow).

In the OSDU R2 Prototype, the Workflow service queries Apache Airflow to start specific ingestion flows depending on the
workflow type and data type.

**Implementation**: [os-workflow](./os-workflow) <br>
**Description and workflow**: [OSDU R2 Workflow Service](./os-workflow/README.md)

#### Workflow API

**POST /startWorkflow**

The `/startWorkflow` API endpoint starts a new workflow of the specific type depending on the data added to the request.
This endpoint is closed for external requests.

**POST /getStatus**

The `/getStatus` API endpoint returns the current status of the workflow job stored in the database. This endpoint is
open for external requests.

**POST /updateWorkflowStatus**

The `/updateWorkflowStatus` API endpoint receives a workflow ID and the current workflow status, and then updates it in
the database. This endpoint is closed for external requests.

### Storage service

> The OSDU R2 Prototype doesn't change the Storage service implementation. The changes that this section talks through
> will be implemented in the future releases of OSDU.

The OSDU R2 Prototype Storage service is an extension of the DELFI Storage service and is designed to store extra
non-indexed metadata with key-value string parameters with each record.

In the OSDU R2 Prototype implementation, the Storage service's `/CreateRecord` endpoint adds the workflow and file IDs
to the file records in the database.

#### Storage API

**POST /CreateRecord**

The `/CreateRecord` API endpoint creates a record in the database for each uploaded file. This is an existing endpoint
and is updated to store extra non-indexing metadata fields with the records. This endpoint is closed for external
requests.

**POST /listRecords**

The `/listRecords` API endpoint is new and searches the existing records by metadata. This endpoint is closed for
external requests.

### Workflow Engine (Apache Airflow)

The OSDU R2 Workflow Engine is an implementation of Apache Airflow that handles pipeline processing in OSDU R2.

#### Manifest Ingestion DAG

The Manifest Ingestion DAG is partly based on the implementation of the [OSDU R1 Ingestion service].
The DAG carries out ingestion of OSDU Files with Work Product and Work Product Components metadata, all provided in the
manifest. The OSDU R2 Ingestion service performs manifest validation, and then the DAG runs the necessary tasks to
create new records (ingest) for files, WPCs, and WP.

#### Opaque Ingestion DAG

The Opaque Ingestion DAG carries out ingestion of the opaque data type. The DAG receives files for ingestion and creates
records for them in the database. The OSDU ingestion process, which may include extraction, categorization, enrichment,
quality assessment, and artifact generation, doesn't happen.

#### Stale Jobs Scheduler

The Stale Jobs Scheduler is an operator designed to run at an N minutes interval to verify the current status of the
submitted workflow. For the workflows that have a **submitted** or **running** status in the database but that have
failed during execution, the Stale Jobs Scheduler sets their status to **failed**.

#### Workflow Status Operator

The Workflow Status Operator is a custom Airflow operator that updates the status of the submitted workflows. This
operator queries the Workflow service to update the status.

### Finished Workflow Sensor Operator

The Finished Workflow Sensor operator is a custom Airflow operator that notifies the DAG that the current ingestion
process for a file has completed. The DAG starts ingestion of the next file in the list.

## OSDU R2 Prototype Ingestion workflow

The OSDU R2 Prototype implementation introduces two ingestion workflow types that both consist of the following phases:

1. File uploading
2. Ingestion preparation
3. Pipeline processing with Apache Airflow

### 1. File uploading

The first phase of OSDU R2 ingestion is uploading a file to the system. The user needs to obtain a signed URL from the
OSDU Delivery Service. The user fully controls file upload. In OSDU R2 Prototype, services do not verify whether a file
was uploaded to the landing zone or not.

File uploading workflow:

1. The user or application sends an HTTP request to the Delivery Service to get a file location.
2. The Delivery service creates a signed URL for the file.
    > In the GCP implementation, the Delivery service queries Google Cloud Storage to generate a signed URL.
3. The Delivery service creates a file upload record in the database.
4. The Delivery service returns a signed URL to the client.
5. The user or application uploads a file to the landing zone by the signed URL.
    > The Delivery Service does not verify whether the file was uploaded or not.

### 2. Ingestion

The ingestion phase consists of the following steps:

1. The user or application submits a request for ingestion to the OSDU R2 Ingestion Service.
    > **Note**: The ingestion request may contain a manifest with a list of files and the metadata added as OSDU Work
    > Product and Work Product Components. For the OSDU Ingestion workflow, Files are presented as a list of file IDs.
2. In the Opaque ingestion flow, the Ingestion service queries the Delivery service to obtain the files by the signed 
URLs. In the Manifest ingestion flow, the Ingestion service validates the manifest against the schema stored in the
project's database.
    * In the Opaque ingestion flow, the Delivery service returns the file location data &mdash; Driver and Location
    &mdash; to the Ingestion service.
4. The Ingestion service submits a new ingestion job with the context to the Workflow service. The context includes:
    * File location
    * Workflow ingestion type &mdash; "ingest" or "osdu"
    * Manifest if the workflow type is "osdu"
5. The Workflow service queries the database to understand what Airflow DAG should be started.
6. The Workflow service submits a new workflow job with the DAG type, file type, and context.
7. The Workflow service stores the workflow job ID in the database, and then returns the workflow job ID to the
Ingestion service.
8. The Ingestion service returns the workflow job ID to the user. The user or application can eventually submit the
workflow job ID to the Workflow service to learn the status of the current workflow.

### 3. Pipeline processing with Apache Airflow

During this step, Apache Airflow starts running the necessary DAGs. Depending on the workflow, user, and data type,
different DAGs perform different actions.
 
* During the Opaque ingestion flow, the Opaque Ingestion DAG only creates a record. 
* During the Manifest ingestion flow, the Manifest Ingestion DAG creates records for the Work Product, Work Product
Components, and Files.
    * A list of Work Product Components is retrieved from the manifest. For each Work Product Component, a list of
    associated files is ingested.
    * Once all Work Product Components are ingested, the DAG creates a record for the Work Product.

## OSDU R2 Google Cloud Platform implementation

The OSDU R2 Prototype uses the following Google Cloud Platform services:

* Cloud Firestore
* Cloud Datastore
    > **Note**: The OSDU GCP project uses Cloud Datastore for compatibility reasons to work with the ODES services. The
      current OSDU implementation includes code to work with Firestore or Datastore. However, in the future releases of
      OSDU, the Cloud Datastore implementation will be removed. Cloud Datastore creates the same collections and indexes
      as Cloud Firestore.
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

#### `schema-data`

The `schema-data` collection stores the OSDU manifest validation schemas. In OSDU R2, the collection stores only the
OSDU WorkProductLoadManifestStagedFiles JSON schema.

| Property  | Type    | Description                                                |
| --------- | ------- | ---------------------------------------------------------- |
| CreatedAt | String  | The timestamp when the record was created.                 |
| Schema    | String  | The OSDU [WorkProductLoadManifestStagedFiles] JSON schema. |
| Title     | Integer | The name of the manifest validation schema.                |

[Java 8]: https://java.com/en/download/faq/java8.xml
[Python 3.6+]: https://www.python.org/downloads/release/python-360/ 
[Google Cloud SDK]: https://cloud.google.com/sdk/install
[Terraform 0.12.8+]: https://www.terraform.io/downloads.html
[Spring Boot 2.2.5]: https://spring.io/blog/2020/02/27/spring-boot-2-2-5-released
[Spring Boot Cloud Hoxton.SR3]: https://spring.io/blog/2020/03/05/spring-cloud-hoxton-service-release-3-sr3-is-available
[Project Lombok]: https://projectlombok.org/
[Jackson]: https://github.com/FasterXML/jackson
[JavaX Inject 1]: https://mvnrepository.com/artifact/javax.inject/javax.inject/1
[Guava 28.2-jre]: https://github.com/google/guava
[GSON 2.8.5]: https://github.com/google/gson
[Lettuce 4.5.0.Final]: https://lettuce.io/
[Swagger Core JAX RS Project Setup 1.5.X]: https://github.com/swagger-api/swagger-core/wiki/Swagger-Core-JAX-RS-Project-Setup-1.5.X
[Google HTTP Client 1.31.0]: https://mvnrepository.com/artifact/com.google.http-client/google-http-client/1.31.0
[Auth0 Java JWT 3.8.1]: https://mvnrepository.com/artifact/com.auth0/java-jwt/3.8.1
[JSON Web Token 0.9.1]: https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt/0.9.1
[Elasticsearch 6.6.2]: https://www.elastic.co/guide/en/elasticsearch/reference/6.6/release-notes-6.6.2.html
[Elasticsearch REST Client 6.6.2]: https://mvnrepository.com/artifact/org.elasticsearch.client/elasticsearch-rest-client/6.6.2
[Elasticsearch REST High Level Client 6.6.2]: https://mvnrepository.com/artifact/org.elasticsearch.client/elasticsearch-rest-high-level-client/6.6.2
[JUnit]: https://junit.org/junit5/
[Mockito 2.0.2-beta]: https://mvnrepository.com/artifact/org.mockito/mockito-all/2.0.2-beta
[Powermock 2.0.2]: https://mvnrepository.com/artifact/org.powermock/powermock-core/2.0.2
[MapStruct 1.3.1 Final]: https://mapstruct.org/news/2019-09-29-mapstruct-1_3_1_Final-bug-fix-released/
[Maven Checkstyle Plugin 3.1.0]: https://blogs.apache.org/maven/entry/apache-maven-checkstyle-plugin-version
[Maven PMD Plugin 3.12.0]: https://maven.apache.org/plugins/maven-pmd-plugin/index.html
[JaCoCo Maven Plugin 0.8.4]: https://mvnrepository.com/artifact/org.jacoco/jacoco-maven-plugin/0.8.4
[Spotbugs Maven Plugin 3.1.12]: https://spotbugs.github.io/spotbugs-maven-plugin/
[Maven Surefire PLugin]: https://maven.apache.org/surefire/maven-surefire-plugin/
[Cloud Run]: https://cloud.google.com/run
[Cloud Storage]: https://cloud.google.com/storage
[Cloud Firestore]: https://cloud.google.com/firestore
[Cloud Datastore]: https://cloud.google.com/datastore
[Cloud SQL]: https://cloud.google.com/sql
[Cloud Functions]: https://cloud.google.com/functions/
[Compute Engine]: https://cloud.google.com/compute
[App Engine]: https://cloud.google.com/appengine
[Google Cloud Operations Suite]: https://cloud.google.com/products/operations
[Spring Boot default logging]: https://github.com/spring-projects/spring-boot/blob/2.2.x/spring-boot-project/spring-boot/src/main/resources/org/springframework/boot/logging/logback/defaults.xml#L11
[Open Group Community Wiki]: https://community.opengroup.org/osdu/documentation/-/wikis/OSDU-(C)/Design-and-Implementation/Ingestion-and-Enrichment-Detail/R2-Ingestion-Workflow-Orchestration-Spike
[OSDU R1 Ingestion service]: ../compatibility-layer/docs/OSDU%20Compatibility%20Layer%20Services.md
[WorkProductLoadManifestStagedFiles]: https://gitlab.opengroup.org/osdu/json-schemas/-/blob/master/WorkProductLoadManifestStagedFiles.json