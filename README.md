# Framework for OSDU

The Framework for OSDU provides an [Open Subsurface Data Universe](https://www.opengroup.org/osdu/forum-homepage) standard compatibility layer for the [DELFI Data Ecosystem](https://www.software.slb.com/delfi/openness/delfi-data-ecosystem); it provides a subset of [OSDU Release 1](https://www.opengroup.org/membership/forums/open-subsurface-data-universe/achievement-and-plans) functionality sufficient for demo and testing purposes. 

The DELFI Data Ecosystem is being [open-sourced as OpenDES](https://www.slb.com/newsroom/press-release/2019/pr-2019-0822-osdu-data-ecosystem).


## Getting Started
Please reach out to your Schlumberger account team for access to a [DELFI Data Ecosystem](https://www.software.slb.com/delfi/openness/delfi-data-ecosystem) test environment, and to your Google account team for assistance with deploying the framework.

### Prerequisites

This framework is dependent on the [Schlumberger Data Ecosystem](https://www.software.slb.com/delfi/openness/delfi-data-ecosystem) environment.

The framework leverages [Google Cloud Platform](cloud.google.com) functionality, specifically:

* [KNative](https://cloud.google.com/knative/)
* [Firebase](https://firebase.google.com/)
* [Cloud Pub Sub](https://cloud.google.com/pubsub)


The framework is implemented in Java, and uses the following libraries:

* [Spring Cloud Greenwich.SR2](https://spring.io/blog/2019/06/27/spring-cloud-greenwich-sr2-is-available)
* [Mapstruct 1.3.0.Final](https://github.com/mapstruct/mapstruct)
* [Spring Framework 2.1.7.RELEASE](https://mvnrepository.com/artifact/org.springframework)
* [Spring Security Test 4.0.0.RELEASE](https://mvnrepository.com/artifact/org.springframework.security/spring-security-test/4.0.0.RELEASE)
* [Project Lombok 1.18.8](https://mvnrepository.com/artifact/org.projectlombok/lombok/1.18.8)
* [Javax Inject 1](https://mvnrepository.com/artifact/javax.inject/javax.inject/1)
* [Spring Boot 2.1.6.RELEASE](https://spring.io/blog/2019/06/19/spring-boot-2-1-6-released)
* [Apache HttpClient 4.3.4](https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient/4.3.4)
* [Jackson Databind 2.10.0.pr1](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.10.0.pr1)


## Built With

* [Google Cloud Platform](cloud.google.com)
* [Java](https://www.java.com/en/)


## License

This project is licensed under the Apache License - see the [LICENSE](LICENSE) file for details