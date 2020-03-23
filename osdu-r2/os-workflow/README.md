# OSDU Workflow Service

## Table of contents

* [Introduction](#introduction)
* [System interactions](#system-interactions)
* [API](#api)
    * [POST /startWorkflow](#post-startworkflow)
    * [POST /getStatus](#post-getstatus)
    * [POST /updateWorkflowStatus](#post-updateworkflowstatus)
* [Service Provider Interfaces](#service-provider-interfaces)
* [GCP implementation](#gcp-implementation)
    * [Persistence layer](#persistence-layer)
* [Firestore](#firestore)

## Introduction

The OSDU R2 Workflow service is designed to start business processes in the system. In the OSDU R2
prototype phase, the service only starts ingestion of OSDU data.

The Workflow service provides a wrapper functionality around the Apache Airflow functions and is
designed to carry out preliminary work with files before running the Airflow Directed Acyclic Graphs
(DAGs) that will perform actual ingestion of OSDU data.

In OSDU R2, depending on the types of data, workflow, and user, the Workflow service starts the
necessary workflow such as well log ingestion or opaque ingestion.

## System interactions

The Workflow service in the OSDU R2 Prototype defines the following workflows:

* Ingestion of new files
* Delivery of an ingestion workflow status
* Update of the workflow status

### Start ingestion

The ingestion workflow starts by a call to the `/startWorkflow` API endpoint. The following diagram
shows the workflow.

![OSDU R2 WorkflowService startWorkflow](/uploads/d2122ae7e53a234d92b87552e5d6b5b1/OSDU_R2_Workflow_Service_startWorkflow_API.png)

Upon a `/startWorkflow` request:

1. Validate the incoming request.
    * Verify the authorization token. Fail ingestion if the token is missing or invalid, and then
    respond with the `401 Unauthorized` status.
    * Verify the partition ID. Fail ingestion if the partition ID is missing, invalid or doesn't
    have assigned user groups, and then respond with the `400 Bad Request` status.
    * Check that the workflow type is "ingest" or "osdu".
    * Check that the data type is "well_log" or "opaque".
        > The `DataType` property can actually be any string value. If the `DataType` value is not
        > "well_log", then it's treated as the "opaque" data type.
2. Query the database to obtain a DAG suitable for the current request. The Workflow service
decides which DAG to run by the following three parameters:
    * `WorkflowType`
    * `DataType`
    * `UserType`
3. Submit a new ingestion job to the OSDU R2 Workflow Engine (Apache Airflow).
4. Create a workflow data record in the database with the **submitted** status.
5. Respond with the workflow ID to the Ingestion service.

### Get workflow status

Upon a `/getWorkflow` request:

1. Validate the incoming request.
    * Verify the authorization token. If the token is missing or invalid, respond with the `401
    Unauthorized` status.
    * Verify the partition ID. If the partition ID is missing, invalid or doesn't have assigned user
    groups, respond with the `400 Bad Request` status.
2. Query the database with the workflow ID received from the client.
    * Respond with the **404 Not Found** status if the requested workflow ID isn't found.
3. Return the workflow job status to the user or application.

### Update workflow status

Once an ingestion workflow has started and a file is ingested, the Apache Airflow DAGs send a new
status to the Workflow service. The ingestion workflow status can be set to **running**,
**finished**, or **failed**.

Upon an `/updateWorkflowStatus` request:

1. Validate the incoming request.
    * Verify the authorization token. Fail workflow status update if the token is missing or
    invalid, and then respond with the `401 Unauthorized` status.
    * Verify the partition ID. Fail workflow status update if the partition ID is missing, invalid
    or doesn't have assigned user groups, and then respond with the `400 Bad Request` status.
    * Fail the request if the workflow ID or status is not provided.
    * Fail the request if the workflow status is not **running**, **finished**, or **failed**.
2. Update the workflow status in the database.
    * Fail the update if the workflow ID is not found in the database.
    * Fail the update if there's more than one workflow found by the workflow ID.
    * Fail the update if the stored status and the incoming status are the same.
3. Return the workflow ID and the workflow status to the requester.

## API

The OSDU R2 Workflow API includes the following endpoints:

* `/startWorkflow`, internal
* `/getStatus`, external
* `/updateWorkflowStatus`, internal

General considerations related to querying the Workflow API:

* Each endpoint must receive the authorization token in the "Authorization" header. Example:
`"Authorization": "Bearer {token}"`
* Each endpoint must receive the partition ID in the "Partition-ID" header. Example:
`"Partition-Id: "default_partition"`
* The request and response Content Type is "application/json"

### POST /startWorkflow

The `/startWorkflow` API endpoint starts a new workflow. This endpoint isn't available for external
requests.

The `/startWorkflow` endpoint is a wrapper around the Airflow invocation, and is designed to
reconfigure the default workflows. For each combination of user, data, and workflow types, the API
identifies a suitable DAG and then calls Airflow.

For OSDU R2 Prototype, this API doesn't reconfigure the workflows and only queries the database to
determine which DAG to run.

#### Request body

| Property     | Type     | Description                                                     |
| ------------ | -------- | --------------------------------------------------------------- |
| WorkflowType | `String` | Type of workflow job to run &mdash; "osdu" or "ingest"          |
| DataType     | `String` | Type of data to be ingested &mdash; "well_log" or "opaque"      |
| Context      | `List`   | Data required to run a DAG, provided as list of key-value pairs |

> The Context may include a file location, ACL and legal tags, and the Airflow run ID. The
> `/startWorkflow` passes the Context to Airflow without modifying it.

#### Response body

| Property   | Type     | Description                   |
| ---------- | -------- | ----------------------------- |
| WorkflowID | `String` | Unique ID of the workflow job |

### POST /getStatus

The `/getStatus` API endpoint returns the current status of a workflow job. This endpoint is
available for external requests.

#### Request body

| Property   | Type     | Description                 |
| ---------- | -------- | --------------------------- |
| WorkflowID | `String` | Unique ID of a workflow job |

#### Response body

If the workflow ID is found in the database, the following response is returned to the user.

| Property | Type     | Description                                                                  |
| -------- | -------- | ---------------------------------------------------------------------------- |
| Status   | `String` | Current status of a workflow &mdash; submitted, running, finished, or failed |

If the workflow ID isn't found in the database, the `404 Not Found` status is returned.

### POST /updateWorkflowStatus

The `/updateWorkflowStatus` API endpoint updates the status of a workflow job. This endpoint is not
available for external requests. This endpoint is necessary to let Apache Airflow DAGs update the
workflow status.

#### Request body

| Property   | Type     | Description                                      |
| ---------- | -------- | ------------------------------------------------ |
| WorkflowID | `String` | Unique ID of a workflow that needs to be updated |
| Status     | `String` | New status of the workflow                       |

Request body example:

```json
{
    "WorkflowID": "2b905e77-7e04-4c04-8581-7b4c224164dd",
    "Status": "finished"
}
```

#### Response body

| Property   | Type     | Description                              |
| ---------- | -------- | ---------------------------------------- |
| WorkflowID | `String` | Unique ID of a workflow that was updated |
| Status     | `String` | The latest status of the workflow        |

Response body example:

```json
{
    "WorkflowID": "2b905e77-7e04-4c04-8581-7b4c224164dd",
    "Status": "finished"
}
```

## Service Provider Interfaces

The Workflow service has several Service Provider Interfaces that the classes need to implement.

| Interface                   | Obligatory / Optional   | Path                                                                         |
| --------------------------- | ----------------------- | ---------------------------------------------------------------------------- |
| AuthenticationService       | Obligatory to implement | `workflow-core/src/main/.../provider/interfaces/AuthenticationService`       |
| IngestionStrategyRepository | Obligatory to implement | `workflow-core/src/main/.../provider/interfaces/IngestionStrategyRepository` |
| IngestionStrategyService    | Optional to implement   | `workflow-core/src/main/.../provider/interfaces/IngestionStrategyService`    |
| SubmitIngestService         | Obligatory to implement | `workflow-core/src/main/.../provider/interfaces/IngestionStrategyService`    |
| ValidationService           | Optional to implement   | `workflow-core/src/main/.../provider/interfaces/IngestionStrategyService`    |
| WorkflowService             | Optional to implement   | `workflow-core/src/main/.../provider/interfaces/IngestionStrategyService`    |
| WorkflowStatusRepository    | Obligatory to implement | `workflow-core/src/main/.../provider/interfaces/IngestionStrategyService`    |
| WorkflowStatusService       | Optional to implement   | `workflow-core/src/main/.../provider/interfaces/IngestionStrategyService`    |

## GCP implementation

The GCP Identity and Access Management service account for the Workflow service must have the
**Composer User** and **Cloud Datastore User** roles.

Obtaining user credentials for Application Default Credentials isn't suitable for the development
purposes because signing a blob is only available with the service account credentials. Remember to
set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable. Follow the [instructions on the
Google developer's portal][application-default-credentials].

### Persistence layer

The GCP implementation contains two mutually exclusive modules to work with the persistence layer.
Presently, OSDU R2 connects to legacy Cloud Datastore for compatibility with the current OpenDES
implementation. In the future OSDU releases, Cloud Datastore will be replaced by the existing Cloud
Firestore implementation that's already available in the project.

* The Cloud Datastore implementation is located in the **provider/workflow-gcp-datastore** folder.
* The Cloud Firestore implementation is located in the **provider/workflow-gcp** folder.

To learn more about available collections, follow to the [Firestore collections](#collections)
section.

## Firestore

Upon an ingestion request, the Workflow service needs to determine which DAG to run. To do that, the
service queries the database with the workflow type and data type.

The GCP-based implementation of the Workflow service uses Cloud Firestore with the following
`ingestion-strategy` and `workflow-status` collections.

> The Cloud Datastore implementation in OSDU R2 uses the same collections as Cloud Firestore.

### Collections

#### `ingestion-strategy`

The database stores the following information to help determine a DAG.

| Property     | Type     | Description                                         |
| ------------ | -------- | --------------------------------------------------- |
| WorkflowType | `String` | Supported workflow types &mdash; "osdu" or "ingest" |
| DataType     | `String` | Supported data types &mdash; "well_log" or "opaque" |
| UserID       | `String` | Unique identifier of the user group or role         |
| DAGName      | `String` | Name of the DAG                                     |

> The OSDU R2 Prototype doesn't support the **UserID** property. When the security system is
> finalized, the **UserID** property will store the ID of the user group or role.

#### `workflow-status`

After a workflow starts, the Workflow service stores the following information in the database.

| Property     | Type     | Description                                                                  |
| ------------ | -------- | ---------------------------------------------------------------------------- |
| WorkflowID   | `String` | Unique workflow ID                                                           |
| AirflowRunID | `String` | Unique Airflow process ID generated by the Workflow service                  |
| Status       | `String` | Current status of a workflow &mdash; submitted, running, finished, or failed |
| SubmittedAt  | `String` | Timestamp when the workflow job was submitted to Workflow Engine             |
| SubmittedBy  | `String` | ID of the user role or group. Not supported in OSDU R2                       |

[application-default-credentials]: https://developers.google.com/identity/protocols/application-default-credentials#calling
