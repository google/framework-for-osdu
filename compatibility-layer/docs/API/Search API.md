# Search API overview

The Search service finds subsurface records in the DELFI Data Ecosystem. The service accepts search terms such as 
`fulltext`, `geospatial`, `metadata`, and `lineage`, and returns detailed data about the found item.

The search request to the service must come in the OSDU format, which is then internally transformed into the search 
object that’s compatible with the DELFI Data Ecosystem. The returned object from DELFI is mapped to the search result in
the OSDU format and is then returned to the client.

## Mapping of OSDU and DELFI search terms

There's a divergence between the DELFI format for data and the OSDU standard in terms of how search queries are 
formatted, which is why certain OSDU search terms can't be fully mapped to a DELFI search query. The Search service 
ignores such terms.

See the next table for the supported OSDU search terms.

| OSDU search term | Description                                                                             | Supported by DELFI |
| ---------------- | --------------------------------------------------------------------------------------- | ------------------ |
| fulltext         | Single search expression                                                                | Yes                |
| geo_centroid     | A list of numbers                                                                       | Yes                |
| geo_location     | Object with distance, type, and coordinates properties                                  | Yes                | 
| metadata         | A list of string values                                                                 | Yes                | 
| facets           | An array of facet names                                                                 | Yes                |
| full_results     | A boolean value to defines if only indexed values should be returned. **Always `true`** | No                 |
| sort             | Object value to control sorting of search results                                       | Yes                | 
| start            | The index of the first search result to be returned                                     | Yes                |
| count            | The number of search results to return for the current request                          | Yes                | 
| map_aggregates   | Boolean value. **Ignored**                                                              | No                 |
| zoom_level       | Integer that represents the zoom level applied to geo queries. **Ignored**              | No                 |
| aggregates_count | Integer used for the size of facet queries. **Ignored**                                 | No                 |

## Search result

| Search result property | Description                                                                   | Type              |
| ---------------------- | ----------------------------------------------------------------------------- | ----------------- |
| results                | A list of search result objects containing found metadata                     | A list of objects |
| totalHits              | The total number of found documents in the DELFI Portal                       | Integer           |
| facets                 | Contains the facet values for the facet names specified in the search request | A list of strings |
| count                  | The total number of search results in the current response                    | Integer           |
| start                  | The index of the first search result in the current response                  | Integer           |

### POST /search

The request body must also contain at least one of the next properties &mdash; `fulltext`, `metadata`, `geo_location`, 
or `geo_centroid`. If these search request fields are missing, then an empty search result is returned.

| Characteristics     | Description                                                                                        |
| ------------------- | -------------------------------------------------------------------------------------------------- |
| Authorization       | The request must contain the authorization token in the header: `Authorization: "Bearer {token}"`. | 
| URL parameters      | None                                                                                               |
| Request body        | Must contain a list of SRNs: `{ "SRNS": ["srn:file/type..."] }`.                                   |
| Content Type        | `application/json`                                                                                 |
| Return Content Type | `application/json`                                                                                 |

## Search API

All the delivery endpoints are relative to the path **https://{project-id}.apigee.net/**.

### GET /search

Search for data in DELFI. 

#### Search request body example

```sh
curl -X POST \
  http://{Apigee URI}/search \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Authorization: Bearer <your token here>' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 63' \
  -H 'Content-Type: application/json' \
  -H 'Host: {Apigee URI}' \
  -d '{
  "fulltext" : "AKM-11 LOG",
  "start": 1,
  "count": 1
}'
```

#### Search response body example

```json
{
  "results": [
    {
    "data": {
      "IndividualTypeProperties.Description": "Well Log",
      "IndividualTypeProperties.Name": "AKM-11 LOG",
      "IndividualTypeProperties.WellboreID": "srn:master-data/Wellbore:1013:",
      "IndividualTypeProperties.TopMeasuredDepth.Depth": 2182.0004
    },
    "kind": "{partition}:ingestion-test:work-product-component:1.0.0",
    "namespace": "{partition}:ingestion-test",
    "legal": {
      "legaltags": [
        "{partition}-public-usa-dataset-1"
      ],
      "otherRelevantDataCountries": [
        "US"
      ],
      "status": "compliant"
    },
    "id": "{partition}:doc:b8c930a1b1cc4299b0ea93f81355aa1e",
    "acl": {
      "viewers": [
        "data.default.viewers@{partition}.p4d.cloud.slb-ds.com"
      ],
      "owners": [
        "data.default.owners@{partition}.p4d.cloud.slb-ds.com"
      ]
    },
    "type": "work-product-component",
    "version": 1573656047229419
    }
  ],
  "totalHits": 68,
  "count": 1,
  "start": 1
}
```

> Note that the example response doesn't contain an actual DELFI partition ID in `results[0].id`.

### Search request example without required fields

```sh
curl -X POST \
  http://{Apigee URI}/search \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIiLCJpYXQiOjE1NzU2NDExMzB9.RZkUSPCEReWbQTDkSVN5ztz6iWN7wji5TqF2XR7A4FQ' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 63' \
  -H 'Content-Type: application/json' \
  -H 'Host: {Apigee URI}' \
  -d '{
  "start": 1,
  "count": 1
}'
```

Response body

```json
{
  "status": 400,
  "error": "Bad Request"
}
```

## Search statuses

The Search service returns the following statuses:

* **401 Unauthorized**. The request did not contain a valid authorization token.

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

* **200 Success**. The response may or may not contain data for a given request.
