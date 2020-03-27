# OSDU R2 DAGs

## Contents

* [Introduction](#introduction)
* [Opaque Ingestion DAG](#opaque-ingestion-dag)
* [Manifest Ingestion DAG](#manifest-ingestion-dag)
* [DAG implementation details](#dag-implementation-details)
* [Workflow Status Operator](#workflow-status-operator)
* [Stale Jobs Scheduler](#stale-jobs-scheduler)
* [Workflow Finished Sensor operator](#workflow-finished-sensor-operator)

## Introduction

The OSDU R2 Prototype includes a Workflow Engine, an implementation of Apache Airflow, to orchestrate business
processes. In particular, the Workflow Engine handles ingestion of opaque and well log .las files in OSDU R2.

The Workflow Engine encompasses the following components:

* Opaque Ingestion DAG
* OSDU Ingestion DAG
* Workflow Status Operator
* Stale Jobs Scheduler
* Workflow Finished Sensor Operator

## Opaque Ingestion DAG

The Opaque Ingestion DAG performs ingestion of OSDU opaque data type. The following diagram shows the workflow of the
Opaque Ingestion DAG.

![OSDU R2 Opaque Ingestion DAG](https://user-images.githubusercontent.com/21691607/77777705-9c4dd180-7058-11ea-97c7-9e0deb9d2a87.png)

The Opaque Ingestion DAG flow:

1. Call the Workflow Status Operator with the **running** status.
    * Workflow Status Operator queries the Workflow service's **/updateWorkflowStatus** API endpoint with the
    **running** status, and then returns the control back to the Opaque Ingestion DAG.
2. Query the Storage service's **/createOrUpdateRecord** API endpoint to create a record for the file.
    * The ODES Storage service makes a call to ODES Indexer and returns to the DAG.
3. Call the Workflow Status Operator with the **finished** status.
    * The Workflow Status Operator queries the Workflow service's **/updateWorkflowStatus** endpoint to set the workflow
    status to **finished** in the database.

## Manifest Ingestion DAG

The Manifest Ingestion DAG ingests multiple files with their metadata provided in an OSDU manifest. The following
diagram demonstrates the workflow of the Manifest
Ingestion DAG.

![OSDU R2 Manifest Ingestion DAG](https://user-images.githubusercontent.com/21691607/77666377-8cb38780-6f89-11ea-97b4-57abf507ca5a.png)

Upon an execution request:

1. Invoke the Workflow Status Operator to set the new status for the workflow.
    * The Workflow Status Operator queries the Workflow service's **/updateWorkflowStatus** API endpoint with the
    **running** status.
2. Obtain the Work Product Components associated with the Work Product.
    * For each Work Product Component, find all associated OSDU Files. For each File in the manifest:
        * Start the **ingest** workflow. Call the Workflow service's **/startWorkflow** API endpoint the **ingest**
        workflow type.
        > The Workflow Finished Sensor operator polls the DAG execution and notifies the DAG to start ingestion of the
        > next file.
    * Once all Files for the current Work Product Component are ingested, query the Storage service's
    **/CreateOrUpdatedRecord** API endpoint to create a record for the current Work Product Component.
    * Once all Work Product Components and Files are ingested, switch to the third step.
3. Create a new record for the Work Product.
    * Query the Storage service's **/CreateOrUpdateRecord** API endpoint and pass it the Work Product.
4. Search the records by metadata.
    * Query the Storage service's **/listRecords** API to obtain the records by metadata.
5. Enrich the records with data from the manifest.
    * Query the Storage service's **/UpdateRecord** API endpoint and pass it the metadata from the manifest.
    > Only file records are updated.
6. Invoke the Workflow Status Operator with the **finished** job status.
    * The Workflow Status Operator queries the Workflow service to set the new workflow status.

## DAG implementation details

OSDU DAGs are cloud platform-agnostic by design. However, there are specific implementation requirements by cloud
platforms, and the OSDU R2 Prototype provides a dedicated Python SDK to make sure that DAGs are independent from the
cloud platforms. This Python SDK is located in a separate [os-python-sdk] folder.

## Workflow Status Operator

The Workflow Status Operator is an Airflow operator callable from each DAG. It's purpose is to receive the latest status
of a workflow job and then update the workflow record in the database. Each DAG in the system has to invoke the Workflow
Status Operator to update the workflow status.

This operator isn't designed to directly update the status in the database, and it queries the OSDU R2 Workflow
service's API endpoint. Once the operator sends a request to update status, it cedes control back to the DAG.

## Stale Jobs Scheduler

The Stale Jobs Scheduler is designed to query Apache Airflow to find out any stale workflow jobs, that is, the jobs that
failed during execution but which status wasn't updated to **failed** in the database.

This operator queries the Airflow API every N minutes to verify that the workflow jobs that do not have the _finished_
status are still running. If a workflow job has failed in Airflow, the Stale Jobs Scheduler will set this workflow job
status to **failed** in the database.

The Stale Jobs Scheduler workflow:

1. Query the database to find all workflow records with the _submitted_ or _running_ statuses.
2. Query Airflow to verify the status of the submitted or running workflow jobs.
3. If Airflow returns the failed status for a workflow job, query Firestore to set the workflow status to FAILED.

## Workflow Finished Sensor Operator

The Workflow Finished Sensor operator is a special type of operator that monitors ingestion of a file during the "osdu"
ingestion workflow. Once a file is ingested, this operator notifies the DAG, which then starts ingestion of the next
file in the manifest.

[os-python-sdk]: ../os-python-sdk