A package to interface with OSDU microservices

To install locally:
- Make sure you have setuptools and wheel installed `python3 -m pip install --user --upgrade setuptools wheel`
- Run `python setup.py sdist bdist_wheel`
- Make sure osdu-api isn't already installed `pip uninstall osdu-api`
- Run `python -m pip install <YOUR PATH TO PACKAGE>/dist/osdu_api-0.0.1-py3-none-any.whl` make sure to substitute your machine's path in that command

Example import after installing:
`from osdu_api.storage.record_client import RecordClient`
