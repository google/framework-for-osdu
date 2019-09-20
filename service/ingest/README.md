# Ingest service working with data in OSDU format

## Google cloud storage
Is used as a buffer for uploaded files. 
Service receives file location as a field in Load Manifest object.
Then upload it to cloud storage and then send it to signed Url location.
Setup [lifecycle rule](https://cloud.google.com/storage/docs/managing-lifecycles) to delete previously uploaded files from cloud storage that became redundant.
Example for lifecycle configuration rule file (gcs-lifecycle-config.json) location in src/resources folder
