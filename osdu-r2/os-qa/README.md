# OSDU Test automation framework

The OSDU R2 project has two 2 types of test suites:

* Suites targeted at Google Cloud Platform (GCP) implementation
* Suites targeted at OSDU GCP services

To run the tests, the following environment variables must be set:

* Variables for the service host: `INGEST`, `DELIVERY`, `WORKFLOW`, and `AIRFLOW`.
* A bearer token variable: `TOKEN`.
* GCP specified tests have additional deep checkouts and require authentication with Google services:
`GOOGLE_APPLICATION_CREDENTIALS`.

**Example**

```
TOKEN=temp
INGEST=https://amer-demo28-test.apigee.net
DELIVERY=https://amer-demo28-test.apigee.net
WORKFLOW=https://amer-demo28-test.apigee.net
AIRFLOW=https://temp
GOOGLE_APPLICATION_CREDENTIALS = temp
```

Maven commands for the GCP tests:

```sh
# OSDU R2 Delivery service (formerly File service)
mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/file_service/FileServiceGcp.xml

# OSDU R2 Ingest
mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/ingest/IngestGcp.xml

# OSDU R2 Workflow
mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/workflow/WorkflowAnyCloud.xml
```

Maven commands for any cloud tests:

```sh
# OSDU R2 Delivery service (formerly File service)
mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/file_service/FileServiceAnyCloud.xml

# OSDU R2 Ingest service
mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/ingest/IngestAnyCloud.xml

# OSDU R2 Workflow service
mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/workflow/WorkflowAnyCloud.xml
```

To get Allure Report, execute an additional Maven task **after** test suites have run using the following command:

```sh
mvn site
```

To view the test report, open the generated `index.html` file under the `os-qa\target\site\allure-maven-plugin` folder.
