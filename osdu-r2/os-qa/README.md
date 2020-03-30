**Test automation framework** 

There are 2 types of the test suites:

*  	1 - for the specifically GCP;
*  	2 - for any other;

First thing that need to be done is to set environment variables:
  
*      1. Variables for service host
        1.1. INGEST variable
        1.2. DELIVERY variable
        1.3. WORKFLOW variable
        1.4. AIRFLOW variable
 
*     2. Variable for the bearer token
        2.1. TOKEN variable
        2.2. PAGE_WITH_TOKEN variable (need to specify page in the variable, - way to get token from the resource using selenium)
 
*     3. GCP specified tests have additional deep checkouts and requires google authentication
        3.1. GOOGLE_APPLICATION_CREDENTIALS variable

* Examples:
*  TOKEN=temp
*  INGEST=https://amer-demo28-test.apigee.net
*  DELIVERY=https://amer-demo28-test.apigee.net
*  WORKFLOW=https://amer-demo28-test.apigee.net
*  AIRFLOW=https://temp
*  GOOGLE_APPLICATION_CREDENTIALS = temp
********

Maven commands for the GCP tests:
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/file_service/FileServiceGcp.xml
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/ingest/IngestGcp.xml
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/workflow/WorkflowAnyCloud.xml

Maven commands for ANY cloud tests:
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/file_service/FileServiceAnyCloud.xml
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/ingest/IngestAnyCloud.xml
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/workflow/WorkflowAnyCloud.xml

And in order to get allure report it is also need to execute additional maven goal **AFTER** suites run with separate command:

* 	mvn site

Report can be found by the following path: ..\os-qa\target\site\allure-maven-plugin\index.html