from osdu_api.client.storage.record_client import RecordClient
from entitlements_client import get_bearer_token

def create_records(**kwargs):
    # the only way to pass in values through the experimental api is through the conf parameter
    records = kwargs['dag_run'].conf['records']
    token = get_bearer_token()
    record_client = RecordClient(token)
    return record_client.create_update_records_from_dict(records)