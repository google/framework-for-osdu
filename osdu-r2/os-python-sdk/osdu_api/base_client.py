#  Copyright 2020 Amazon
#  Copyright 2020 Google LLC
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import sys, os
import importlib
import yaml # MIT license
import requests
from airflow.models import Variable
from osdu_api.model.http_method import HttpMethod

'''
Base client that is meant to be extended by service specific clients
'''
class BaseClient:

    '''
    Base client gets initialized with configuration values and a bearer token
    based on provider-specific logic
    '''
    def __init__(self):
        self._read_variables()
        self.bearer_token = self._get_bearer_token()
    
    '''
    The path to the logic to get a valid bearer token is dynamically injected based on
    what provider and entitlements module name is provided in the configuration yaml
    '''
    def _get_bearer_token(self):
        entitlements_client = importlib.import_module(f"osdu_api.provider.{self.provider}.{self.entitlements_module_name}")
        return entitlements_client.get_bearer_token()

    '''
    Parses a yaml filed named osdu_api.yaml. All config values listed below are meant to 
    be required except URLs to specific services which may or may not be used depending
    on the specific script
    '''
    def _parse_config(self):
        config_file_location = os.path.join(sys.path[0], 'osdu_api.yaml')
        with open(config_file_location, 'r') as config_file:
            config = yaml.load(config_file)
            self.data_partition_id = self._parse_config_value(config, 'data_partition_id', True)
            self.storage_url = self._parse_config_value(config, 'storage_url', False)
            self.search_url = self._parse_config_value(config, 'search_url', False)
            self.provider = self._parse_config_value(config, 'provider', True)
            self.entitlements_module_name = self._parse_config_value(config, 'entitlements_module_name', True)

    '''
    Read Airflow variables 
    '''
    def _read_variables(self):
        self.storage_url = Variable.get('storage_url')
        self.search_url = Variable.get('search_url')
        self.provider = Variable.get('provider')
        self.entitlements_module_name = Variable.get('entitlements_module_name')
    
    '''
    Used during parsing of the yaml config file. Will raise an exception if a required config
    value is missing
    '''
    def _parse_config_value(self, config, config_name, is_required):
        config_value = ''
        try:
            config_value = config[config_name]
        except TypeError:
            if(is_required):
                raise Exception('Config value %s missing and is required' % config_name)
            else:
                print('Config value %s missing' % config_name)
        return config_value

    '''
    Makes a request using python's built in requests library. Takes additional headers if
    necessary
    '''
    def make_request(self, method: HttpMethod, url: str, data = '', add_headers = {}, params = {}):
        headers = {
            'content-type': 'application/json',
            'data-partition-id': self.data_partition_id,
            'Authorization': self.bearer_token
        }

        if (len(add_headers) > 0):
            for key, value in add_headers:
                headers[key] = value

        response = object

        if (method == HttpMethod.GET):
            response = requests.get(url=url, params=params, headers=headers)
        elif (method == HttpMethod.POST):
            response = requests.post(url=url, params=params, data=data, headers=headers)
        elif (method == HttpMethod.PUT):
            response = requests.put(url=url, params=params, data=data, headers=headers)
        
        return response
