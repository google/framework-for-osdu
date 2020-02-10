# OSDU Compatibility Layer - Services Overview

The OSDU Compatibility Layer consists of three core OSDU services &mdash; Ingestion, Delivery, and Search &mdash; and 
two supplementary services that handle common functionality such as querying the DELFI Data Ecosystem and Cloud 
Firestore.

## Contents

* [Ingestion service overview](#ingestion-service-overview)
* [Delivery service overview](#delivery-service-overview)
* [Search service overview](#search-service-overview)
* [Delfi-client](#delfi-client)
* [Srn-mapper](#srn-mapper)

## Ingestion service overview

The Ingestion service is designed to upload new data to DELFI. The current Ingestion service implementation supports the 
following OSDU types:

* Work Product Components
* Files with the following extensions: .las, .csv, .pdf, and .txt

Ingestion of Work Product isn't currently supported.

The service requires a Work Product load manifest to be added to each ingestion request. The manifest is a JSON 
description of what the client needs OSDU GCP to ingest &mdash; metadata of the WPC or File to be ingested. The manifest 
must be created according to the OSDU standard.

### Ingestion API

For information on the Ingestion service endpoints, consult the following document inside the project's docs folder:

* [Ingestion API]

### Ingestion service interactions

The Ingestion service uses the **delfi-client** service to authenticate the incoming ingestion requests and to send OSDU
types to DELFI for ingestion.

Additionally, the service uses the **srn-mapper** service to create new SRN to DELFI record mappings in Cloud Firestore 
when the incoming data or file is successfully ingested. The SRN to DELFI mappings are used by the [Delivery 
service](#delivery-service-overview) to retrieve Work Product Components and Files upon delivery requests.

The Ingestion service uses Google Cloud Storage (GCS) as a buffer for uploaded files.

### Ingestion service workflow

The basic workflow for the Ingestion service is to validate the incoming manifest and start the ingestion process. If 
files were added to the request, the service loads them to Google Cloud Storage before uploading them to DELFI. The file 
locations must be added as fields in the loaded manifest. The service stores files by the signed URLs received from 
DELFI. 

After ingestion is completed, the client receives a Universally Unique Identifier (UUID) of the ingestion job. The 
client can query the Ingestion service with the received UUID to learn the status of the current ingestion job.

Detailed ingestion flow:

1. Verify that the request contains a valid authorization token.
    * If the authorization token is valid, start the ingestion process.
    * If the authorization token is invalid, stop the workflow. Respond to the client with the **401 Unauthorized**
    message.
2. Validate the manifest.
    * Receive the manifest and compare it to the schema stored inside the OSDU Compatibility Layer.
        * If the manifest is not valid, fail ingestion and respond to the client with the FAILED status.
        * If the manifest is valid, continue the ingestion process.
3. Get the Work Product
    * Get Work Product Components from the received Work Product.
    * Get links to files.
4. Get the signed URL from DELFI. By the signed URLs, the received files are stored.
5. Submit files to DELFI for ingestion.
    * If file submitting isn't completed, create the failed file result.
        * Set the `record` property to `NULL`
        * Set the `srn` property to `NULL`
        * Set the `success` property to `FALSE`
        * Add the exception message to the `summaries` list
    * If submitting is completed, fetch the ID of the new DELFI record.
6. Generate an SRN for the file.
7. Get the file record from DELFI and enrich it with data from the manifest.
8. Validate the enriched record against the JSON schema stored in the project.
9. Create the SRN to DELFI record ID mapping entry in Cloud Firestore.
10. Create the result with the record, SRN, result of processing, and summary.
    * Check how the files were processed.
    * Collect the summaries of files processing.
    * Generate an SRN for WPC.
    * Create a DELFI record for WPC. Get the record ID and create an SRN to Record ID mapping entry in Firestore.
    * Validate the DELFI record against the JSON schema.
    * Create the resulting WPC with Files, SRN, WPC record, the result of processing (Files processed && WPC valid), and 
      summary.
    * Check how the WPCs were processed.
    * Collect the summaries of WPC processing.
    * Generate the SRN for WP.
    * Create a record for WP in DELFI.
    * Validate the record in DELFI using the JSON schema.
    * Create the resulting WP with WPCs, SRN, WP record, the result of processing (WPCs processed and WP valid), and 
      summary.
11. Check the result of processing WP.
    * If the result is successful, the resulted ingest job is created with the generated SRNs and the `COMPLETED` 
      status.
    * If the result is unsuccessful, all the newly created DELFI records are failed. 
        * Set the lifecycle status of the DELFI records to `RESCINDED`.
        * Create the resulting ingestion job with SRNs, the `FAILED` status, and a summary.
12. Save the resulting ingestion job to Cloud Firestore.
13. Return the inner ingestion job status to the client.

### Ingestion file types

The Ingestion service supports the following file types, as defined by the OSDU standard:

* .las
* .csv, .pdf, .txt as opaque files

Only the .las files are actually processed by DELFI. Other supported file types are only stored in DELFI without any 
prior processing.

For any file types not in the list, the Ingestion service sets the ingestion process will complete with exception and
the result of ingestion will be marked as `FAILED`.

### Ingestion processes
 
The Ingestion service consists of the following internal services:

| Service         | Description                                                                  |
| --------------- | ---------------------------------------------------------------------------- |
| Enrich          | Enriches Work Product Components records retrieved from DELFI                |
| Ingest          | Formats the incoming ingestion request and sends data for ingestion to DELFI |
| JSON validation | Validates the load manifest against the JSON schema stored in the project    |
| Storage         | Uploads the files sent for ingestion to the Google Cloud Storage bucket      |
| Submit          | Submits files to DELFI for processing                                        |

#### Enrich service workflow

The Enrich service is designed to eliminate the differences in record formats in DELFI and OSDU. 
For example, a DELFI record does not contain the `ResourceCurationStatus` field, which is required by OSDU.
 
The Enrich service's algorithm to update data:

1. Fetch a DELFI record by ID.
2. Put Work Product Component data from the load manifest to the DELFI record data.
3. Collect and put to the DELFI record data that corresponds to the OSDU record format.

The following OSDU fields are added by the enrichment service into the DELFI records for each OSDU Work Product 
Component:

| Additional fields               | Description                                                                          |
| ------------------------------  | ------------------------------------------------------------------------------------ |
| ResourceHomeRegionID            | The name of the home GCP region for the OSDU resource object                         |
| ResourceHostRegionIDs           | The name of the host GCP region(s) for the OSDU resource object                      |
| ResourceObjectCreationDateTime  | Timestamp of the time at which Version 1 of this OSDU resource object was originated |
| ResourceVersionCreationDateTime | Timestamp of the time when the current version of this resource entered the OSDU     |
| ResourceCurationStatus          | Describes the current Curation status. Possible values: CREATED, CURATING, CURATED   |
| ResourceLifecycleStatus         | Describes the current Resource Lifecycle status. Possible values - LOADING, RECEIVED, ACCEPTED, RESCINDED, DELETED |
| ResourceSecurityClassification  | Classifies the security level of the resources. Possible values = RESTRICTED, CLASSIFIED, CONFIDENTIAL, MOST-CONFIDENTIAL <br> **Always set to RESTRICTED in the current Ingestion service implementation** |

After the enrichment service adds OSDU fields to the DELFI record, this record is validated using the 
`WorkProductComponent` schema, which is stored in Cloud Firestore by the `ResourceTypeID` field. 

If validation fails for at least one Work Product Component, ingestion for all loaded resources is canceled. More 
specifically, in DELFI each new record's `ResourceLifecycleStatus` property is set to `RESCINDED`.

## Delivery service overview

The Delivery service fetches records with well data or records with links to files from DELFI. For input, the Delivery 
service expects a list of SRNs. 

The Delivery service in OSDU Compatibility Layer delivers Work Products, Work Product Components, and File types to the 
client. In case with files, the service responds with direct links to files and file records metadata.

### Delivery API

For information on the Delivery service endpoints, consult the following document inside the project's docs folder:

* [Delivery API]

### Delivery service interactions

The Delivery service uses the **delfi-client** service to authenticate the incoming delivery requests and send requests 
to DELFI.

The service also uses the **srn-mapper** service to search for SRN to DELFI record mappings, which are used to build 
requests to DELFI.

### Delivery service workflow

1. Verify that the request contains a valid authorization token.
    * If the authorization token is valid, start the delivery process.
    * If the authorization token is invalid, stop the workflow. Respond to the client with the **401 Unauthorized**
    message.
2. For each SRN:
    * Find the SRN to DELFI record ID mapping in Cloud Firestore. 
        * If there's no record for the current SRN in Cloud Firestore, set the processing result status to `NO_MAPPING`
        for this SRN, and then return the result.
    * From the found mapping, get the DELFI record ID for the current SRN, and then query DELFI with this ID.
    * Determine whether the record returned by DELFI contains _file_ or _data_. Perform one of the following processes 
    on the record:
        * If the record contains a URL to a GCS bucket, this record contains a file. Query DELFI to get the signed URL
        for the file. Extend the result object:
            * Set the processing result status to `FILE`.
            * Set the data retrieved from the DELFI record.
            * Set the `FileLocation` property to the signed URL generated by DELFI.
            * Return the result.
        * If the record doesn't contain a URL to a GCS bucket, this record only contains data. Extend the result object:
            * Set the data retrieved from the DELFI record.
            * Set the processing result status to `DATA`.
            * Return the result.
    * Add the record data that came from the DELFI record's `osdu` property to the response object.
    * Add the file URL from the record data to the `FileLocation` property of the response object.
    * Return the obtained data to the requester. The unprocessed SRNs are added to the `unprocessedSrns` field in the 
    response object.
    
## Search service overview

The Search service provides the functionality to find subsurface records in DELFI. The service accepts search terms such
as `fulltext`, `geospatial`, `metadata`, and `lineage`, and can return detailed data about the found item.

The search request to the service must come in the OSDU format. The service transforms the search object to the format
compatible with DELFI. The returned object from DELFI is mapped to the search result in the OSDU format and is then 
returned to the client.

### Search service API

For information on the Search service endpoints, consult the following document inside the project's docs folder:

* [Search API]

### Search service interactions

The Search service uses **delfi-client** to submit search requests to DELFI.

### Search service workflow

1. Verify that the request contains a valid authorization token.
    * If the authorization token is valid, start the search process.
    * If the authorization token is invalid, stop the workflow. Respond to the client with the **401 Unauthorized**.
2. Verify that the search request body includes at least one of the following search parameters: `fulltext`, `metadata`,
`geo_location`, or `geo_centroid`. Perform one of the following sub-steps:
    * If no parameters from the list `fulltext`, `metadata`, `geo_location`, and `geo_centroid` are present, respond to 
    the client with a successful message and an empty search result object. Stop the workflow.
    * If at least one parameter from `fulltext`, `metadata`, `geo_location`, or `geo_centroid` is present, map the 
    incoming search object to the DELFI search object. Continue to step 3.
3. Query DELFI for the given search request.
4. Receive the search result from DELFI and map it to the OSDU-compliant search result.
5. Return the result to the client.

### Mapping of OSDU and DELFI search terms

There's a divergence between the DELFI format for data and the OSDU standard in terms of how search queries are 
formatted, which is why certain OSDU search terms can't be fully mapped to a DELFI search query. The Search service in
the OSDU Compatibility Layer ignores such terms.

Consult the following table for more information on supported and ignored search terms.

| OSDU search term | Description                                                                                                           | Supported in DELFI |
| ---------------- | --------------------------------------------------------------------------------------------------------------------- | ------------------ |
| fulltext         | Single search expression                                                                                              | Yes                |
| geo_centroid     | A list of numbers                                                                                                     | Yes                |
| geo_location     | Object with distance, type, and coordinates properties                                                                | Yes                |
| metadata         | A list of string values                                                                                               | Yes                |
| facets           | An array of facet names                                                                                               | Yes                |
| full_results     | A boolean value that defines whether only indexed values should be returned <br> **Always `true` for queries to DELFI** | No                 |
| sort             | Object value to control sorting of search results                                                                     | Yes                |
| start            | The index of the first search result to be returned                                                                   | Yes                |
| count            | The number of search results to return for the current request                                                        | Yes                |
| map_aggregates   | Boolean value <br> **Ignored by the Search service**                                                                  | No                 |
| zoom_level       | Integer that represents the zoom level applied to geo queries <br> **Ignored by the Search service**                  | No                 |
| aggregates_count | Integer used for the size of facet queries <br> **Ignored by the Search service**                                     | No                 |

## OSDU Compatibility Layer helper services

The OSDU Compatibility Layer comes with two helper services that implement the common features required by the three
core services &mdash; Ingestion, Delivery, and Search.

### Delfi-client

Delfi-client is a service that performs two key functions:

* Validate the incoming requests. The service checks whether the authorization token and DELFI partition are correct.

* Send ingestion, delivery, and search queries to DELFI. The delfi-client service queries DELFI for data such as stored 
records and files and saves data and files to DELFI.

### Srn-mapper

The OSDU Compatibility Layer uses Cloud Firestore to store the mappings of SRNs to DELFI record IDs, and has a dedicated
service to communicate with Firestore.

The srn-mapper service is designed to get the stored SRN to DELFI record mappings and to store new mappings in case of 
ingestion.

[Ingestion API]: https://gitlab.osdu-gcp.dev/osdu/osdu/tree/develop/docs/API/Ingestion%20API.md
[Delivery API]: https://gitlab.osdu-gcp.dev/osdu/osdu/tree/develop/docs/API/Delivery%20API.md
[Search API]: https://gitlab.osdu-gcp.dev/osdu/osdu/tree/develop/docs/API/Search%20API.md
[OpenDES Contribution Wiki]: https://gitlab.opengroup.org/osdu/opendes-contribution-wiki/wikis/OSDU-(C)/Design-and-Implementation/Entity-and-Schemas/Comparing-OSDU-&-OpenDES-Schema-Semantics