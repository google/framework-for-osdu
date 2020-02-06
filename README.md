# OSDU

The OSDU repository contains the OSDU Compatibility Layer and OSDU Release 2 projects.

## OSDU Compatibility Layer

The OSDU Compatibility Layer is an implementation of the OSDU standard and provides a subset of the functionality
available in OSDU Release 1. In particular, the compatibility layer can only ingest .las files. The implementation is
located under the `osdu/compatibility-layer` folder.

The compatibility layer consists of the following services:

* Search, provides an external API method to perform search for OSDU data 
* Delivery, provides an external API method to download OSDU data
* Ingest, provides external API methods to ingest .las files and learn the ingestion status 

## OSDU R2

The OSDU Release 2 is an implementation of a unified ingestion flow based on the ingestion flows of the OSDU Release 1 
and DELFI Data Ecosystem. The implementation is located under the `osdu/osd-r2` folder.

The OSDU Release 2 consists of the following services:

* File, provides internal and external API methods to let the user or OSDU R2 services to request file location data
* Workflow, handles any business process in the OSDU R2, in particular, the ingestion process
* Ingest, provides external API methods to let the user to submit files for ingestion