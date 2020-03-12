import unittest
import mock
from osdu_api.base_client import BaseClient

class TestBaseClient(unittest.TestCase):

    @mock.patch.object(BaseClient, '_get_bearer_token', return_value="stubbed")
    @mock.patch.object(BaseClient, '_parse_config', return_value="stubbed")
    def test_init(self, mocked_token_method, mocked_config_method):
        # Arrange

        # Act
        client = BaseClient()

        # Assert
        mocked_token_method.assert_called()
        mocked_config_method.assert_called()

    @mock.patch.object(BaseClient, '_get_bearer_token', return_value="stubbed")
    def test_parse_config(self, mocked_token_method):
        # Arrange

        # Act
        client = BaseClient()

        # Assert
        assert client.data_partition_id == 'stubbed_partition'
        assert client.storage_url == 'http://localhost:8081/api/storage/v2/records'
        assert client.provider == 'stubbed_provider'
        assert client.entitlements_module_name == 'entitlements_client'
