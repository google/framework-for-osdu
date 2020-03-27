# OSDU Python SDK

OSDU DAGs are cloud platform-agnostic by design. However, there are specific implementation requirements by cloud
platforms, and the OSDU R2 Prototype provides a dedicated Python SDK to make sure that DAGs are independent from the
cloud platforms.

The Python SDK must be installed on the Airflow worker instance and be used by the DAGs. Each cloud provider needs to
modify and configure this SDK to run on their cloud platform.

In OSDU R2 Prototype, the SDK encapsulates calls to the ODES Storage and Search services. In the future releases, the
SDK might provide additional interfaces, in particular, for the Ingestion service.

To authenticate requests, the DAGs needs to add a bearer token to the payload, which is passed to the SDK's methods when
calling the OSDU services.

The Python SDK is a package to interface with OSDU microservices.

To install this package:

1. Install `setuptools` and `wheel`:

```sh
python3 -m pip install --user --upgrade setuptools wheel
```

2. Run the following command:

```sh
python setup.py sdist bdist_wheel
```

3. Uninstall `osdu-api`:

```sh
pip uninstall osdu-api
```

4. Run  make sure to substitute your machine's path in that command

```sh
python -m pip install <YOUR_PATH_TO_PYTHON_SDK>/dist/osdu_api-0.0.1-py3-none-any.whl
```

5. Import and use the SDK in your code:

```python
from osdu_api.storage.record_client import RecordClient
```
