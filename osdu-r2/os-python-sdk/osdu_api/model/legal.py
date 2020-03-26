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

from osdu_api.model.legal_compliance import LegalCompliance

'''
Legal model mirroring what's found in core common
'''
class Legal:
    def __init__(self, legaltags: list, other_relevant_data_countries: list, status: LegalCompliance):
        self.legaltags = legaltags
        self.other_relevant_data_countries = other_relevant_data_countries
        self.status = status

    def get_dict(self):
        legal_dict = {}
        legal_dict['legaltags'] = self.legaltags
        legal_dict['otherRelevantDataCountries'] = self.other_relevant_data_countries
        legal_dict['status'] = str(self.status)
        return legal_dict