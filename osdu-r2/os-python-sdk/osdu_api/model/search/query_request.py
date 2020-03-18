#  Copyright 2020 Amazon
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

from osdu_api.model.search.sort_query import SortQuery
from osdu_api.model.search.spatial_filter import SpatialFilter

class QueryRequest:

    def __init__(self, kind: str, limit: int, query: str, return_highlighted_fields: bool, 
        returned_fields: list, sort: SortQuery, query_as_owner: bool, spatial_filter: SpatialFilter, 
        from: int, aggregate_by: str):
        self.kind = kind
        self.limit = limit
        self.query = query
        self.return_highlighted_fields = return_highlighted_fields
        self.returned_fields = returned_fields
        self.sort = sort
        self.query_as_owner = query_as_owner
        self.spatial_filter = spatial_filter
        self.from = from
        self.aggregate_by = aggregate_by