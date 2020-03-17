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

