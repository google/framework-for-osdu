import pytest
import mock
import json
import unittest
import types
from osdu_api.storage.record_client import RecordClient
from osdu_api.base_client import BaseClient
from osdu_api.model.http_method import HttpMethod
from osdu_api.model.record import Record
from osdu_api.model.legal_compliance import LegalCompliance
from osdu_api.model.acl import Acl
from osdu_api.model.legal import Legal
from osdu_api.model.record_ancestry import RecordAncestry


class TestRecordClient(unittest.TestCase):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs) 
        self.test_record_dict = {
                    'acl': {
                        'owners':[
                            'data.test1@opendes.testing.com'
                        ],
                        'viewers':[
                            'data.test1@opendes.testing.com'
                        ]
                    },
                    'ancestry':{
                        'parents':[]
                    },
                    'data':{'id':'test'},
                    'id':'opendes:welldb:123456',
                    'kind':'opendes:welldb:wellbore:1.0.0',
                    'legal':{
                        'legaltags':['opendes-storage-1579034803194'],
                        'otherRelevantDataCountries':['US'],
                        'status':'compliant'
                    },
                    'meta':[
                        {}
                    ],
                    'version':0
                }

        self.test_record_str = """{
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
                }"""

        acl = Acl(['data.test1@opendes.testing.com'], ['data.test1@opendes.testing.com'])
        legal = Legal(['opendes-storage-1579034803194'], ['US'], LegalCompliance.compliant)
        ancestry = RecordAncestry([])
        id = 'opendes:welldb:123456'
        kind = 'opendes:welldb:wellbore:1.0.0'
        meta = [{}]
        version = 0
        data = {'id': 'test'}
        self.test_record = Record(id, version, kind, acl, legal, data, ancestry, meta)

    @mock.patch.object(BaseClient, '_get_bearer_token', return_value="stubbed")
    @mock.patch.object(BaseClient, 'make_request', return_value="response")
    @mock.patch.object(BaseClient, '_read_variables', return_value="stubbed")
    def test_create_update_records(self, get_bearer_token_mock, make_request_mock, parse_config_mock):
        # Arrange
        record_client = RecordClient()
        record_client.storage_url = 'stubbed url'
        record_client.headers = {}
        records = [
            self.test_record_dict
        ]
        headers = {'test': 'test-value'}

        # Act
        response = record_client.create_update_records_from_dict(records, headers=headers)

        # Assert
        make_request_mock.assert_called_with(data=json.dumps(records), method=HttpMethod.PUT, url='stubbed url',
                                             add_headers=headers)

    @mock.patch.object(BaseClient, '_get_bearer_token', return_value="stubbed")
    @mock.patch.object(BaseClient, 'make_request', return_value="response")
    @mock.patch.object(BaseClient, '_read_variables', return_value="stubbed")
    def test_create_update_records_model_record(self, get_bearer_token_mock, make_request_mock, parse_config_mock):
        # Arrange
        record_client = RecordClient()
        record_client.storage_url = 'stubbed url'
        record_client.headers = {}

        make_request_mock.return_value = 'called'
        headers = {}

        # Act
        response = record_client.create_update_records([self.test_record], headers=headers)

        # Assert
        assert response == make_request_mock.return_value

    @mock.patch.object(BaseClient, '_get_bearer_token', return_value="stubbed")
    @mock.patch.object(BaseClient, 'make_request', return_value="response")
    @mock.patch.object(BaseClient, '_read_variables', return_value="stubbed")
    def test_get_latest_record_version(self, get_bearer_token_mock, make_request_mock, parse_config_mock):
        # Arrange
        record_client = RecordClient()
        record_client.storage_url = 'stubbed url'
        record_client.headers = {}
        record_id = 'test'
        make_request_mock.return_value = types.SimpleNamespace()
        make_request_mock.return_value.content = self.test_record_str
        request_params = {'attribute': []}
        headers = {'test': 'test-value'}

        # Act
        record = record_client.get_latest_record(record_id, headers=headers)

        # Assert
        make_request_mock.assert_called_with(url=record_client.storage_url + '/test', params=request_params,
                                             method=HttpMethod.GET, add_headers=headers)
        assert record.acl.owners == self.test_record.acl.owners
        assert record.acl.viewers == self.test_record.acl.viewers
        assert record.id == self.test_record.id
        assert record.kind == self.test_record.kind
        assert record.legal.status == self.test_record.legal.status
        assert record.legal.legaltags == self.test_record.legal.legaltags
        assert record.legal.other_relevant_data_countries == self.test_record.legal.other_relevant_data_countries
        assert record.meta == self.test_record.meta
        assert record.version == self.test_record.version
        assert record.ancestry.parents == self.test_record.ancestry.parents

    @mock.patch.object(BaseClient, '_get_bearer_token', return_value="stubbed")
    @mock.patch.object(BaseClient, 'make_request', return_value="response")
    @mock.patch.object(BaseClient, '_read_variables', return_value="stubbed")
    def test_get_specific_record_version(self, get_bearer_token_mock, make_request_mock, parse_config_mock):
        # Arrange
        record_client = RecordClient()
        record_client.storage_url = 'stubbed url'
        record_client.headers = {}
        record_id = 'test'
        make_request_mock.return_value = types.SimpleNamespace()
        make_request_mock.return_value.content = self.test_record_str
        request_params = {'attribute': []}
        version = 123
        headers = {'test': 'test-value'}

        # Act
        record = record_client.get_specific_record(record_id, version, headers)

        # Assert
        make_request_mock.assert_called_with(url=record_client.storage_url + '/test/123', params=request_params,
                                             method=HttpMethod.GET, add_headers=headers)
        assert record.acl.owners == self.test_record.acl.owners
        assert record.acl.viewers == self.test_record.acl.viewers
        assert record.id == self.test_record.id
        assert record.kind == self.test_record.kind
        assert record.legal.status == self.test_record.legal.status
        assert record.legal.legaltags == self.test_record.legal.legaltags
        assert record.legal.other_relevant_data_countries == self.test_record.legal.other_relevant_data_countries
        assert record.meta == self.test_record.meta
        assert record.version == self.test_record.version
        assert record.ancestry.parents == self.test_record.ancestry.parents

    @mock.patch.object(BaseClient, '_get_bearer_token', return_value="stubbed")
    @mock.patch.object(BaseClient, 'make_request', return_value="response")
    @mock.patch.object(BaseClient, '_read_variables', return_value="stubbed")
    def test_get_record_versions(self, get_bearer_token_mock, make_request_mock, parse_config_mock):
        # Arrange
        record_client = RecordClient()
        record_client.storage_url = 'stubbed url'
        record_client.headers = {}
        record_id = 'test'
        make_request_mock.return_value = types.SimpleNamespace()
        make_request_mock.return_value.content = b'{"versions": [123]}'
        request_params = {'attribute': []}
        headers = {'test': 'test-value'}

        # Act
        versions = record_client.get_record_versions(record_id, headers)

        # Assert
        make_request_mock.assert_called_with(url=record_client.storage_url + '/versions/test', method=HttpMethod.GET,
                                             add_headers=headers)
        assert versions == [123]
