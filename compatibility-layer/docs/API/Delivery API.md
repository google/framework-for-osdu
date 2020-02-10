# Delivery API overview

The Delivery service fetches records with well data or records with links to files from the DELFI Data Ecosystem.
The service can deliver OSDU Work Products, Work Product Components, and Files. In case with files, the service responds
with direct links to files and metadata of file records. 

For input, the Delivery service expects a list of Subsurface Data Universe Resource Numbers (SRNs). The service delivers 
Work Products, Work Product Components, and files. In case with files, the service responds with direct links &mdash; 
signed URLs &mdash; to files and file records metadata.

To obtain a signed URL, the service calls a dedicated DELFI endpoint with the authorization token, DELFI partition, and 
application key (the key of the client’s application registered in DELFI). Using this data, DELFI signs the URLs to 
files and returns them to the Delivery service.

## Delivery API

All the delivery endpoints are relative to the path **https://{project-id}.apigee.net/**. 

### POST /delivery

Fetch any records with well data or records with links to files from DELFI.

|                       | Description                                                           |
| --------------------- | --------------------------------------------------------------------- |
| Authorization         | Authorization token in the header: `Authorization: "Bearer {token}"`. | 
| URL parameters        | None                                                                  |
| Request Content Type  | `application/json`                                                    |
| Response Content Type | `application/json`                                                    |

### Delivery request example

The delivery request is a JSON document with a list of SRNs of the OSDU **Work Products**, **Work Product Components**, 
**Master**, and **Reference Data** to be delivered to the requester. Additionally, the request must contain the target 
region ID.

Generic delivery request:

```json
{
  "SRNS": ["srn:file/type..."],
  "TargetRegionID": ""
}
``` 

#### File delivery request example

```sh
curl -X POST \
  https://{Apigee URI}/delivery \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Authorization: Bearer <your token here>' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 127' \
  -H 'Content-Type: application/json' \
  -H 'Host: {Apigee URI}' \
  -d '{
  "SRNS":[
    "srn:file/las2:cca54c53bfee4ce4b985e3fd1678ad09:1"
  ],
  "TargetRegionID": 123
}'
```

> Note that the example request doesn't contain an actual Apigee URI or an authorization token.

#### Response to a file delivery request

The Delivery service responds with the list of results for the given list of SRNs. The list of results for a file 
request may contain links to download locations (such as Google Cloud Storage) if any of the given SRNs is associated 
with a file.

```json
{
  "UnprocessedSrns": [],
  "Result": [
    {
      "FileLocation": "{signed URL to download the file}",
      "Data": {
        "ResourceID": "srn:file/las2:cca54c53bfee4ce4b985e3fd1678ad09:1",
        "ResourceTypeID": "srn:type:file/las2:",
        "ResourceHostRegionIDs": [],
        "ResourceObjectCreationDatetime": "2019-11-14T16:37:11.273",
        "ResourceVersionCreationDatetime": "2019-11-14T16:37:11.273",
        "ResourceCurationStatus": "srn:reference-data/ResourceCurationStatus:CREATED:",
        "ResourceLifecycleStatus": "srn:reference-data/ResourceLifecycleStatus:RECEIVED:",
        "ResourceSecurityClassification": "srn:reference-data/ResourceSecurityClassification:RESTRICTED:",
        "Data": {
          "GroupTypeProperties": {
            "PreLoadFilePath": "{Link to File}",
            "FileSource": "",
            "FileSize": 0
          },
          "IndividualTypeProperties": {},
          "ExtensionProperties": {}
        }
      },
      "SRN": "srn:file/las2:cca54c53bfee4ce4b985e3fd1678ad09:1"
    }
  ]
}
```

> Note that the example response doesn't contain an actual link to the file.
> `Result[0].Data.Data.GroupTypeProperties.PreLoadFilePath`.

#### Delivery request example for Work Product Component

The following example demoes a request for a Work Product Component.

```sh
curl -X POST \
  https://{Apigee URI}/delivery \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Authorization: Bearer {token}' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 127' \
  -H 'Content-Type: application/json' \
  -H 'Host: {Apigee URI}' \
  -d '{
  "SRNS":[
    "srn:work-product-component/WellLog:0195eb311b1c422c8f78ffc93c63e4db:1"
  ],
  "TargetRegionID": "123"
}'
```

> Note that the example request doesn't contain an actual Apigee URI and an authorization token.

#### Delivery response example for Work Product Component

```json
{
  "UnprocessedSrns": [],
  "Result": [
    {
      "Data": {
        "ResourceID": "srn:work-product-component/WellLog:0195eb311b1c422c8f78ffc93c63e4db:1",
        "ResourceTypeID": "srn:type:work-product-component/WellLog:1",
        "ResourceHostRegionIDs": [],
        "ResourceObjectCreationDatetime": "2019-11-14T16:37:12.261",
        "ResourceVersionCreationDatetime": "2019-11-14T16:37:12.261",
        "ResourceCurationStatus": "srn:reference-data/ResourceCurationStatus:CREATED:",
        "ResourceLifecycleStatus": "srn:reference-data/ResourceLifecycleStatus:RECEIVED:",
        "ResourceSecurityClassification": "srn:reference-data/ResourceSecurityClassification:RESTRICTED:",
        "Data": {
          "GroupTypeProperties": {
            "Files": [
              "srn:file/las2:cca54c53bfee4ce4b985e3fd1678ad09:1"
            ],
            "Artefacts": []
          },
          "IndividualTypeProperties": {
            "Name": "AKM-11 LOG",
            "Description": "Well Log",
            "WellboreID": "srn:master-data/Wellbore:1013:",
            "TopMeasuredDepth": {
              "Depth": 2182.0004,
              "UnitOfMeasure": "srn:reference-data/UnitOfMeasure:M:"
            },
            "BottomMeasuredDepth": {
              "Depth": 2481.0,
              "UnitOfMeasure": "srn:reference-data/UnitOfMeasure:M:"
            },
            "Curves": [
              {
                "Mnemonic": "DEPT",
                "TopDepth": 2182.0,
                "BaseDepth": 2481.0,
                "DepthUnit": "srn:reference-data/UnitOfMeasure:M:",
                "CurveUnit": "srn:reference-data/UnitOfMeasure:M:"
              },
              {
                "Mnemonic": "GR",
                "TopDepth": 2182.0,
                "BaseDepth": 2481.0,
                "DepthUnit": "srn:reference-data/UnitOfMeasure:M:",
                "CurveUnit": "srn:reference-data/UnitOfMeasure:GAPI:"
              },
              {
                "Mnemonic": "DT",
                "TopDepth": 2182.0,
                "BaseDepth": 2481.0,
                "DepthUnit": "srn:reference-data/UnitOfMeasure:M:",
                "CurveUnit": "srn:reference-data/UnitOfMeasure:US/F:"
              },
              {
                "Mnemonic": "RHOB",
                "TopDepth": 2182.0,
                "BaseDepth": 2481.0,
                "DepthUnit": "srn:reference-data/UnitOfMeasure:M:",
                "CurveUnit": "srn:reference-data/UnitOfMeasure:G/C3:"
              },
              {
                "Mnemonic": "DRHO",
                "TopDepth": 2182.0,
                "BaseDepth": 2481.0,
                "DepthUnit": "srn:reference-data/UnitOfMeasure:M:",
                "CurveUnit": "srn:reference-data/UnitOfMeasure:G/C3:"
              },
              {
                "Mnemonic": "NPHI",
                "TopDepth": 2182.0,
                "BaseDepth": 2481.0,
                "DepthUnit": "srn:reference-data/UnitOfMeasure:M:",
                "CurveUnit": "srn:reference-data/UnitOfMeasure:V/V:"
              }
            ]
          },
          "ExtensionProperties": {}
        },
        "AssociativeID": "wpc-1",
        "FileAssociativeIDs": [
          "f-1"
        ]
      },
      "SRN": "srn:work-product-component/WellLog:0195eb311b1c422c8f78ffc93c63e4db:1"
    }
  ]
}
``` 

## Delivery statuses

The Delivery service returns the following statuses:

* **401 Unauthorized**. Returned to the client if the request doesn’t contain a valid authorization header.

Response example if authorization fails.

```json
{
  "timestamp": "2019-11-29T09:16:40.332+0000",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing authorization token",
  "path": "/"
}
```

* **200 Success**. The response contains the requested data, the list of files (if any), and the list of unprocessed
SRNs (if any).
