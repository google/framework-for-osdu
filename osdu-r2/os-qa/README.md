**Test automation framework** 

There are 2 types of the test suites:

*  	1 - for the specifically GCP;
*  	2 - for any other;

The first one has additional deep checkouts and requires google authentication which could be performed via GOOGLE_APPLICATION_CREDENTIALS variable.

Maven commands for the GCP tests:
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/file_service/FileServiceGcp.xml
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/ingest/IngestGcp.xml
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/workflow/WorkflowAnyCloud.xml

Maven commands for ANY cloud tests:
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/file_service/FileServiceAnyCloud.xml
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/ingest/IngestAnyCloud.xml
* 	mvn clean test -Dsurefire.suiteXmlFile=src/test/resources/suites/workflow/WorkflowAnyCloud.xml

And in order to get allure report it is also need to execute additional maven goal AFTER suites run with separate command:

* 	mvn site

Report can be found by the following path: ..\os-qa\target\site\allure-maven-plugin\index.html