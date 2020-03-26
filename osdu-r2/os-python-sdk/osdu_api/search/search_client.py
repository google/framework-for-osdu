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

import json
from typing import List
from osdu_api.base_client import BaseClient
from osdu_api.model.http_method import HttpMethod
from osdu_api.model.search.query_response import QueryResponse

'''
Holds the logic for interfacing with Search's query api
'''
class SearchClient(BaseClient):

    '''
    Used to hit search's api endpoint "queryRecords"
    '''
    def query_records_from_dict(self, query_request: dict):
        query_request_data = json.dumps(query_request)

        response = self.make_request(method=HttpMethod.POST, url=self.search_url, data=query_request_data)
        response_content = json.loads(response.content)
        query_response = QueryResponse(response_content['results'], response_content['aggregations'])

        return query_response

