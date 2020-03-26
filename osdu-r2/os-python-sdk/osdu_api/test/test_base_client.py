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

import unittest
import mock
from osdu_api.base_client import BaseClient

class TestBaseClient(unittest.TestCase):

    @mock.patch.object(BaseClient, '_get_bearer_token', return_value="stubbed")
    @mock.patch.object(BaseClient, '_read_variables', return_value="stubbed")
    def test_init(self, mocked_token_method, mocked_config_method):
        # Arrange

        # Act
        client = BaseClient()

        # Assert
        mocked_token_method.assert_called()
        mocked_config_method.assert_called()

