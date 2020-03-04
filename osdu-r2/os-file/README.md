# OSDU R2 Prototype File Service

## Table of contents

* [Introduction](#introduction)
* [System interactions](#system-interactions)
* [Validations](#validations)
* [API](#api)
    * [POST /getLocation](#post-getlocation)
    * [POST /getFileLocation](#post-getfilelocation)
    * [POST /getFileList](#post-getfilelist)
* [Service Provider Interfaces](#service-provider-interfaces)
* [GCP implementation](#gcp-implementation)
    * [Persistence layer](#persistence-layer)
* [Datastore](#datastore)
* [Firestore](#firestore)

## Introduction

The OSDU R2 Prototype File service provides internal and external APIs to request for file location
data. For example, users can request generation of an individual signed URL per file. Using the
signed URL, OSDU R2 users will be able to upload their files for ingestion to the system.

The current implementation of the File service supports only cloud platform-specific locations. The
future implementations might allow the use of on premises locations.

## System interactions

The File service defines three workflows &mdash; file upload, file location delivery, and file list
delivery.

### File upload

The file upload workflow is defined for the `/getLocation` API endpoint. The following diagram
illustrates the workflow.

![OSDU R2 FileService getLocation](https://user-images.githubusercontent.com/21691607/75542516-a912e180-5a28-11ea-9613-6e4418e82f79.png)

Upon a request to get a location for a file:

1. Verify the incoming request.
    * Verify the authentication token. Fail signed URL generation if the token is missing or
    invalid, and then respond with the `401 Unauthorized` status.
    * Verify the partition ID. Fail signed URL generation if the partition ID is missing or invalid
    or doesn't have assigned user groups. Respond with the `400 Bad Request` status.
    * Verify the file ID (only if it's passed in the request body). Fail signed URL generation if
    the file ID is invalid or if this ID was already created. Respond with the `400 Bad Request`
    status and `Location for fileID {ID} already exists` message.
2. Generate a new Universally Unique Identifier (UUID) for the file if a file ID isn't provided.
3. Create an empty object in Google Cloud Storage, and then generate a signed URL with the write
access for that object.
    > By the signed URL, the user or application will upload their file for ingestion.
    > The generated signed URL has the maximum duration of 7 days.
4. Create a record with file data in the database. The record will contain a key-value pair with the
file ID as the key and object as the value. For more information on the record, consult the
[Firestore](#firestore) section.
5. Return the signed URL and file ID to the application or user.

### File location delivery

The file location delivery workflow is defined for the `/getFileLocation` API endpoint. The
following diagram demonstrates the workflow.

![OSDU R2 FileService getFileLocation](https://user-images.githubusercontent.com/21691607/75542644-e11a2480-5a28-11ea-9e0c-732ce57a818e.png)

Upon request from an OSDU R2 service:

1. Validate the incoming request.
    * Verify the authentication token. Fail signed URL generation if the token is missing or
    invalid, and then respond with the `401 Unauthorized` status.
    * Verify the partition ID. Fail signed URL generation if the partition ID is missing, invalid or
    doesn't have assigned user groups, and then respond with the `400 Bad Request` status.
2. Get the `FileID` value from the incoming request.
3. Query the database with `FileID` to get the file record.
4. Return the `Location` and `Driver` from the record to the calling service.

### File list delivery

The file list delivery workflow is defined for the `/getFileList` API endpoint.

Upon request from another OSDU R2 service:

1. Verify the incoming request.
    * Verify the authentication token. Fail signed URL generation if the token is missing or
    invalid, and then respond with the `401 Unauthorized` status.
    * Verify the partition ID. Fail signed URL generation if the partition ID is missing, invalid or
    doesn't have assigned user groups, and then respond with the `400 Bad Request` status.
    * Validate the file list request.
3. Obtain the requested files from the database.
4. Return the result to the caller.

## Database interactions

During each workflow, the File service queries the database. For more information about the file
records in the database, consult the [file-locations collection](#collections) section.

## Validations

The File service's current implementation performs a general check of the validity of the
authorization token and DELFI partition ID before the service starts generation of a location.

However, the File service in the OSDU R2 Prototype doesn't perform any verification whether a file
upload happened or whether the user started ingestion after uploading a file. In future OSDU
implementations, the File service will be able to check if file uploads did happen.

## API

The File service's API includes the following endpoints in the OSDU R2 Prototype:

* `/getLocation`, external
* `/getFileLocation`, internal
* `/getFileList`, internal

General considerations related to querying the File API:

* Each endpoint must receive the authorization token in the "Authorization" header. Example:
`"Authorization": "Bearer {token}"`
* Each endpoint must receive the partition ID in the "Partition-ID" header. Example:
`"Partition-Id: "systemdefault8apre7h881noa9"`
* The request and response Content Type is "application/json"
* The request to any endpoint doesn't use any URL parameters

### POST /getLocation

Creates a new location in the landing zone, such as a GCS bucket.

#### Request body

| Property      | Description           | Provided in  | Required/Optional | Type     |
| ------------- | --------------------- | ------------ | ----------------- | -------- |
| FileID        | Unique ID of the file | Request body | Optional          | `String` |

> If **FileID** isn't provided in the request, the File service generates a Universally Unique
Identifier (UUID) to be stored in `FileID`. If `FileID` is provided and is already registered in the
system, an error is returned.
> **FileID** must correspond to the regular expression: `^[\w,\s-]+(\.\w+)?$`.

**Example**:

```sh
curl --location --request POST 'https://{path}/getLocation' \
    --header 'Authorization: {token}' \
    --header 'Content-Type: application/json' \
    --header 'Partition-Id: {DELFI partition ID}' \
    --data-raw '{
        "FileID": "8900a83f-18c6-4b1d-8f38-309a208779cc"
    }'
```

#### Response

| Property  | Type     | Description                                                            |
| --------- | -------- | ---------------------------------------------------------------------- |
| FileID    | `String` | ID of the file to be ingested                                          |
| Location  | `Object` | Key-value pairs with cloud provider details to access the landing zone |
| SignedURL | `String` | Signed URL by which the file to be ingested is stored                  |

> Landing zone is a location in a cloud provider's platform where the user uploaded files for OSDU
> ingestion. The landing zone consists of the `Driver` and `Location` properties, which are stored
> in the database for each file upload request.

Example:

```json
{
    "FileID": "3362ddd9-687f-4cc2-92c0-0ea885ab91fd",
    "Location": {
        "SignedURL": "https://storage.googleapis.com/{path}/{user}/{date}/3362ddd9-687f-4cc2-92c0-0ea885ab91fd?{path params}&X-Goog-Signature=1f750362d545e2e7e2b9af1931e42e275734e4673f1b9eaabb2cea32f010d4c3d7206d5ad13c5cb87d09dd9a39aa22c5ffcdcd0f289a4648bf46857ffc0ac82b154e8d8d2489a4d8b9a6ec7fd51b6621e603b6764e1a167048a16be63f8617d226e037999f2db0d0e26af753f293b3d3f4aa79f4fb27e722e7c44a4efceccf6704e723b4a6c2d1edb0f412101ce8568529b15653780555aa6008e21bcdbf6e40e2379614a949e60cc4b5c18bc474db1a9efc38809d60e5404f359567c3a5f8cbc0b8357af9a85ccf349fb0831d349e1726be40e4d845cde1838b8c5d0ee7"
    }
}
```

### POST /getFileLocation

The `/getFileLocation` API endpoint works similarly to `/getLocation`, but is internal and returns
the landing zone &mdash; `Location` and `Driver` &mdash; of a particular file.

Once the OSDU security model is formulated and finalized, the `/getFileLocation` API endpoint will
not be returning files that belong to other users.

#### Request

| Property | Type     | Description                   | Provided in  |
| -------- | -------- | ----------------------------- | ------------ |
| FileID   | `String` | ID of the file to be ingested | Request body |

Example:

```sh
curl --location --request POST 'https://{path}/getFileLocation' \
    --header 'Authorization: {token}' \
    --header 'Content-Type: application/json' \
    --header 'Partition-Id: {DELFI partition ID}' \
    --data-raw '{
        "FileID": "8900a83f-18c6-4b1d-8f38-309a208779cc"
    }'
```

#### Response

| Property | Type     | Description                                         |
| -------- | -------- | --------------------------------------------------- |
| Driver   | `String` | Description of the storage where the file is stored |
| Location | `String` | Direct URI to the file in storage                   |

Example:

```json
{
    "Driver": "GCS",
    "Location": "gs://{gcs-bucket}/{user}/1583341980529-2020-03-03-13-25-00-529/3362ddd9-687f-4cc2-92c0-0ea885ab91fd"
}
```

### POST /getFileList

The `/getFileList` API endpoint allows auditing the attempted file uploads. The method is
unavailable for third-party applications.

The ingestion process depends on whether the client application uploaded a file or not. The
`/getFileList` API endpoint is designed to let other OSDU services to inspect which user uploaded a
file, whether the file was uploaded to the landing zone, and whether the user started ingestion
after the file upload.

#### Request

| Property | Type       | Description                                 |
| -------- | ---------- | ------------------------------------------- |
| TimeFrom | `datetime` | Timestamp                                   |
| TimeTo   | `datetime` | Time interval for the CreatedAd filter      |
| PageNum  | `integer`  | The page number to return paginated results |
| Items    | `short`    | Pagination of the result                    |
| UserID   | `String`   | The ID of the user role or group            |

> `UserID` is not supported in the OSDU R2 Prototype.

Example:

```sh
curl --location --request POST 'https://{path}/getFileList' \
    --header 'Authorization: {token}' \
    --header 'Partition-Id: {DELFI partition ID}' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "PageNum": 0,
        "TimeFrom": "2020-01-01T16:21:00.552Z",
        "UserID": "common-user",
        "TimeTo": "2020-02-15T16:28:44.220Z",
        "Items": 2
    }'
```

### Response

A paginated result of the records stored in the database.

| Property         | Type      | Description                                      |
| ---------------- | --------- | ------------------------------------------------ |
| Content          | `List`    | List of file records retrieved from the database |
| Number           | `integer` | Number                                           |
| NumberOfElements | `integer` | The amount of the returned records               |
| Size             | `short`   | The size of the Content list                     |

Each file record contains the following properties: `FileID`, `Driver`, `Location`, `CreatedAt`,
`CreatedBy`. For more information the returned properties, consult the [Firestore
collections](#collections) section.

Response example:

```json
{
    "Content": [
        {
            "FileID": "30a1ace6-1c8f-4f08-9982-2e9c5df8e878",
            "Driver": "GCS",
            "Location": "gs://{gcs-bucket}/common-user/1580461525198-2020-02-12-05-23-25-198/30a1ace6-1c8f-4f08-9982-2e9c5df8e878",
            "CreatedAt": "2020-02-12T05:24:25.198+0000",
            "CreatedBy": "common-user"
        },
        {
            "FileID": "da057da3-0fdb-41e4-afdc-3b63b812d484",
            "Driver": "GCS",
            "Location": "gs://{gcs-bucket}/common-user/1580461525198-2020-02-13-12-19-14-205/da057da3-0fdb-41e4-afdc-3b63b812d484",
            "CreatedAt": "2020-02-13T12:19:14.205+0000",
            "CreatedBy": "common-user"
        }
    ],
    "Number": 0,
    "NumberOfElements": 2,
    "Size": 2
}
```

## Service Provider Interfaces

The File service has several Service Provider Interfaces that the classes need to implement.

| Interface              | Obligatory/Optional     | Path                                                                     |
| ---------------------- | ----------------------- | ------------------------------------------------------------------------ |
| AuthenticationService  | Optional to implement   | `file-core/src/main/java/.../provider/interfaces/AuthenticationService`  |
| FileListService        | Optional to implement   | `file-core/src/main/java/.../provider/interfaces/FileListService`        |
| FileLocationRepository | Optional to implement   | `file-core/src/main/java/.../provider/interfaces/FileLocationRepository` |
| FileService            | Optional to implement   | `file-core/src/main/java/.../provider/interfaces/FileService`            |
| LocationMapper         | Obligatory to implement | `file-core/src/main/java/.../provider/interfaces/LocationMapper`         |
| LocationService        | Optional to implement   | `file-core/src/main/java/.../provider/interfaces/LocationService`        |
| StorageRepository      | Obligatory to implement | `file-core/src/main/java/.../provider/interfaces/StorageRepository`      |
| StorageService         | Obligatory to implement | `file-core/src/main/java/.../provider/interfaces/StorageService`         |
| ValidationService      | Optional to implement   | `file-core/src/main/java/.../provider/interfaces/ValidationService`      |

## GCP implementation

The service account for File service must have the `iam.serviceAccounts.signBlob` permission.

The predefined **Cloud Functions Service Agent**, **Cloud Run Service Agent**, and **Service Account
Token Creator** roles include the required permission.

For development purposes, it's recommended to create a separate service account.
It's enough to grant the **Service Account Token Creator** role to the development service account.

Obtaining user credentials for Application Default Credentials isn't suitable in this case because
signing a blob is only available with the service account credentials. Remember to set the
`GOOGLE_APPLICATION_CREDENTIALS` environment variable. Follow the [instructions on the Google
developer's portal][application-default-credentials].

### Persistence layer

The GCP implementation contains two mutually exclusive modules to work with the persistence layer.
Presently, OSDU R2 connects to legacy Cloud Datastore for compatibility with the current OpenDES
implementation. In future releases, Cloud Datastore will be replaced by the existing Cloud Firestore
implementation that's already available in the project.

* The Cloud Datastore implementation is located in the **provider/file-gcp-datastore** folder.
* The Cloud Firestore implementation is located in the **provider/file-gcp** folder.

To learn more about available collections, follow to the [Firestore collections](#collections)
section.

## Datastore

The service account for File service must have the `datastore.indexes.*` permissions.
The predefined **roles/datastore.indexAdmin** and **roles/datastore.owner** roles include the
required permission.

## Firestore

The GCP implementation of the File service uses Cloud Firestore with the following
[collections](#collections) and [indexes](#indexes).

### Collections

`file-locations`

| Field     | Type     | Description                                                               |
| --------- | -------- | ------------------------------------------------------------------------- |
| FileID    | `String` | Unique file ID used as a key to reference a file data object with Driver, Location, CreatedAt, and CreatedBy |
| Driver    | `String` | Description of the storage where files were loaded                        |
| Location  | `String` | Direct URI to the file in storage                                         |
| CreatedAt | `String` | Time when the record was created                                          |
| CreatedBy | `String` | ID of the user that requested file location                               |

> The `Location` value might be different from the signed URL returned to the user.
> The `CreatedBy` property isn't supported in the OSDU R2 Prototype.

### Indexes

#### Single Field

| Collection ID  | Field path | Collection scope | Collection group scope |
| -------------- | ---------- | ---------------- | ---------------------- |
| file-locations | FileID     | _no changes_     | _no changes_           |

#### Composite

| Collection ID  | Fields                             | Query scope |
| -------------- | ---------------------------------- | ----------- |
| file-locations | `CreatedBy: ASC`, `CreatedAt: ASC` | Collection  |

[application-default-credentials]: https://developers.google.com/identity/protocols/application-default-credentials#calling
