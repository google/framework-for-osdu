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

from osdu_api.model.search.coordinate import Coordinate

class ByBoundingBox:
    def __init__(self, top_left: Coordinate, bottom_right: Coordinate):
        self.top_left = top_left
        self.bottom_right = bottom_right