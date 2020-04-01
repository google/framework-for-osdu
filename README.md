# OSDU

The OSDU repository contains the OSDU Compatibility Layer and OSDU Release 2 projects.

## OSDU Compatibility Layer

The OSDU Compatibility Layer is an implementation of the OSDU standard and provides a subset of the functionality
available in OSDU Release 1. In particular, the compatibility layer can only ingest .las files. The implementation is
located under the **compatibility-layer** folder.

The compatibility layer consists of the following services:

* Search, provides an external API method to perform search for OSDU data 
* Delivery, provides an external API method to download OSDU data
* Ingest, provides external API methods to ingest .las files and learn the ingestion status 

## OSDU R2

The OSDU Release 2 is an implementation of a unified ingestion flow based on the ingestion flows of the OSDU Release 1 
and DELFI Data Ecosystem. The implementation is located under the **osdu-r2** folder.

The OSDU Release 2 consists of the following services:

* Workflow, handles any business process in the OSDU R2, in particular, the ingestion process
* Delivery, provides internal and external API endpoints to let the user or OSDU R2 services to request for file
location
* Ingest, provides external API endpoints to let the user to submit files for ingestion, and performs preliminary work
on the request before calling the OSDU Workflow service