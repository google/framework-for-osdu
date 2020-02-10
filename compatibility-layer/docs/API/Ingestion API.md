# Ingestion API overview

The current Ingestion service implementation supports uploading of Work Product Components and Files to the DELFI Data
Ecosystem.

The service requires the manifest to be added to each ingestion request. The manifest is a JSON description of the Work 
Products, Work Product Components, or File to be ingested. The incoming manifest is compared to the 
`WorkProductLoadManifestStagedFiles` schema stored in the **service/ingest/src/main/resource/** directory.

## Ingestion API

### POST /submit

Submit OSDU Work Product Components or files for ingestion to the DELFI Data Ecosystem.

|                     | Description                                                                            |
| ------------------- | -------------------------------------------------------------------------------------- |
| Authorization       | Authorization token must be included in the header: `Authorization: "Bearer {token}"`. | 
| URL parameters      | None                                                                                   |
| Request body        | Must contain the manifest                                                              |
| Content Type        | `application/json`                                                                     |
| Return Content Type | `application/json`                                                                     |

#### Ingestion request body example

```sh
curl -X POST \
  https://{Apigee URI}/submit \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Authorization: Bearer <your token here>' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 4276' \
  -H 'Content-Type: application/json' \
  -H 'Host: {Apigee URI}' \
  -d '{
  "WorkProduct": {
    "ResourceTypeID": "srn:type:work-product/WellLog:",
    "ResourceSecurityClassification": "srn:reference-data/ResourceSecurityClassification:RESTRICTED:",
    "Data": {
      "GroupTypeProperties": {
        "Components": []
      },
      "IndividualTypeProperties": {
        "Name": "AKM-11 LOG",
        "Description": "Well Log"
      },
      "ExtensionProperties": {}
    },
    "ComponentsAssociativeIDs": [
      "wpc-1"
    ]
  },
  "WorkProductComponents": [
    {
      "ResourceTypeID": "srn:type:work-product-component/WellLog:",
      "ResourceSecurityClassification": "srn:reference-data/ResourceSecurityClassification:RESTRICTED:",
      "Data": {
        "GroupTypeProperties": {
          "Files": [],
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
    }
  ],
  "Files": [
    {
      "ResourceTypeID": "srn:type:file/las2:",
      "ResourceSecurityClassification": "srn:reference-data/ResourceSecurityClassification:RESTRICTED:",
      "Data": {
        "GroupTypeProperties": {
          "FileSource": "",
          "PreLoadFilePath": "{Path to File}"
        },
        "IndividualTypeProperties": {},
        "ExtensionProperties": {}
      },
      "AssociativeID": "f-1"
    }
  ]
}
'
```

> Note that the example request doesn't contain an actual authorization token, link to file, or Apigee URI.

#### Ingestion response body example

Returned is the ingestion job Universally Unique Identifier (UUID). The UUID can be used to get the current status of
the ingestion job.
 
```json
{
  "jobId":"g83d3182-961a-4250-a73b-51b400cc54e2"
}
```

### GET /{jobId}/status 

Get the status of the ingestion job by ID. Possible statuses: FAILED, RUNNING, or COMPLETE.

URI example: `/b90d7319-983q-4459-ao92-51b500cc54e2/status`

#### Ingestion status response example

Returned is the status of the requested ingestion job. The response body also contains the list of SRNs of Work Product 
Components and Files that are being ingested.

```json
{
    "id":"b90d7319-983q-4459-ao92-51b500cc54e2",
    "status":"COMPLETE",
    "srns": [
      "srn:type:work-product-component/WellLog0cd300a9a6ce483ea19e5f38ca97c199:",
      "srn:file/las2:ccc84e9603e7435392f690629acf5638:"
    ]
}
```
