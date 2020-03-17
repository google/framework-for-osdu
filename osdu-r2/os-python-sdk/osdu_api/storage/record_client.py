import json
from typing import List
from osdu_api.base_client import BaseClient
from osdu_api.model.record import Record
from osdu_api.model.http_method import HttpMethod

'''
Holds the logic for interfacing with Storage's record api
'''
class RecordClient(BaseClient):

    '''
    Calls storage's api endpoint createOrUpdateRecords taking a list of record objects and constructing
    the body of the request
    Returns the response object for the call

    Example of code to new up a record:
    acl = Acl(['data.test1@opendes.testing.com'], ['data.test1@opendes.testing.com'])
    legal = Legal(['opendes-storage-1579034803194'], ['US'], LegalCompliance.compliant)
    ancestry = RecordAncestry([])
    id = 'opendes:welldb:123456'
    kind = 'opendes:welldb:wellbore:1.0.0'
    meta = [{}]
    version = 0
    data = {'id': 'test'}
    record = Record(id, version, kind, acl, legal, data, ancestry, meta)
    '''
    def create_update_records(self, records: List[Record], headers: dict):
        records_data = [record.convert_to_dict() for record in records]
        print(records_data)

        return self.create_update_records_from_dict(records_data, headers)

    '''
    Calls storage's api endpoint createOrUpdateRecords taking individual attributes and constructing
    the body of the request
    Returns the response object for the call

    Example of records_data:
        [
            {
                "acl": {
                    "owners":[
                        "data.test1@opendes.testing.com"
                    ],
                    "viewers":[
                        "data.test1@opendes.testing.com"
                    ]
                },
                "ancestry":{
                    "parents":[]
                },
                "data":{"id":"test"},
                "id":"opendes:welldb:123456",
                "kind":"opendes:welldb:wellbore:1.0.0",
                "legal":{
                    "legaltags":["opendes-storage-1579034803194"],
                    "otherRelevantDataCountries":["US"],
                    "status":"compliant"
                },
                "meta":[
                    {}
                ],
                "version":0
            }
        ]
    '''
    def create_update_records_from_dict(self, records: dict, headers: dict):
        records_data = json.dumps(records)

        response = self.make_request(method=HttpMethod.PUT, url=self.storage_url, data=records_data, add_headers=headers)

        return response

    '''
    Calls storage's api endpoint getLatestRecordVersion taking the required attributes
    Returns the content for the response object
    '''
    def get_latest_record(self, recordId: str, attributes: List[str] = [], headers = dict):
        request_params = {'attribute': attributes}
        response = self.make_request(method=HttpMethod.GET, params=request_params, url=f"{self.storage_url}/{recordId}", add_headers=headers)
        response_content = json.loads(response.content)
        return Record.from_dict(response_content)

    '''
    Calls storage's api endpoint getSpecificRecordVersion taking the required attributes
    Returns the content for the response object
    '''
    def get_specific_record(self, recordId: str, version: str, headers: dict, attributes: List[str] = []):
        request_params = {'attribute': attributes}
        response = self.make_request(method=HttpMethod.GET, params=request_params, url=f"{self.storage_url}/{recordId}/{version}", add_headers=headers)
        response_content = json.loads(response.content)
        return Record.from_dict(response_content)

    '''
    Calls storage's api endpoint getRecordVersions taking the one required parameter record id
    Returns the content for the response object for the call containing the list of versions. 
    Find the versions in the response.content attribute
    '''
    def get_record_versions(self, recordId: str, headers: dict):
        response = self.make_request(method=HttpMethod.GET, url=f"{self.storage_url}/versions/{recordId}", add_headers=headers)
        response_content = json.loads(response.content.decode("utf-8"))
        return response_content['versions']
        