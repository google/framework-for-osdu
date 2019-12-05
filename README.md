# OSDU Compatibility Layer

The OSDU Compatibility Layer is a dedicated layer for the [Open Subsurface Data Universe] standard developed for the 
[DELFI data ecosystem] on top of [Google Cloud Platform]. 

The OSDU Compatibility Layer provides a subset of [OSDU Release 1] functionality sufficient for demo and testing 
purposes. 

> DELFI Data Ecosystem is being open-sourced as [OpenDES] and is an internal project of Schlumberger.

This is not an officially supported Google product.

## Getting Started

To start using the OSDU Compatibility Layer:

1. Contact the Schlumberger account team for access to the [DELFI Data Ecosystem] test environment. The Schlumberger 
team will provide you with the following details:
    * OpenID credentials
    * DELFI partition
2. Contact the Google account team and provide them with the DELFI partition you received from SLB.
3. Obtain the Apigee URI to which you can sent requests.

The Apigee URI looks similar to this: `https://<gcp-project-id>.apigee.net`.

Once you received this URI, you can start sending requests to the OSDU Compatibility Layer.

## Implementation

The current implementation of the OSDU Compatibility Layer includes the following services:

* Ingestion, receives ingestion requests and starts the ingestion process
* Search, receives search requests and returns the results found in the DELFI Data Ecosystem
* Delivery, receives delivery requests and returns work products, work product components, or files

The compatibility layer also includes two helper services:

* Delfi-client, authenticates incoming requests and queries the DELFI Data Ecosystem
* Srn-mapper, the service that communicates with Cloud Firestore to obtain DELFI record IDs or store new IDs by SRNs

## Technology Stack

The OSDU Compatibility Layer is built with [Java] and [Terraform].

The project also uses the following Java libraries:

* [Spring Cloud Greenwich Service Release 2]
* [Spring Framework 2.1.7]
* [Spring Security Test 4.0]
* [Spring Boot 2.1.6 ]
* [MapStruct]
* [Project Lombok 1.18.8]
* [Javax Inject 1]
* [Apache HttpClient 4.3.4]
* [Jackson Databind 2.10.0.pr1]

The project leverages the following Google Cloud Platform services:

* [Cloud Run]
* [Cloud Storage]
* [Cloud Firestore]
* [Cloud Pub/Sub]

## License

This project is licensed under the Apache License. Consult the [LICENSE](./LICENSE.md) file for details.

[Open Subsurface Data Universe]: https://www.opengroup.org/osdu/forum-homepage
[DELFI data ecosystem]: https://www.software.slb.com/delfi/openness/delfi-data-ecosystem
[OSDU Release 1]: https://www.opengroup.org/membership/forums/open-subsurface-data-universe/achievement-and-plans
[OpenDES]: https://www.slb.com/newsroom/press-release/2019/pr-2019-0822-osdu-data-ecosystem
[Java]: https://docs.oracle.com/javase/8/docs/technotes/guides/language/index.html
[Terraform]: https://www.terraform.io/
[Google Cloud Platform]: https://cloud.google.com
[Spring Cloud Greenwich Service Release 2]: https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies/Greenwich.SR2
[Spring Framework 2.1.7]: https://mvnrepository.com/artifact/org.springframework
[Spring Security Test 4.0.0]: https://mvnrepository.com/artifact/org.springframework.security/spring-security-test/4.0.0.RELEASE
[Spring Boot 2.1.6]: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot/2.1.6.RELEASE
[MapStruct]: https://github.com/mapstruct/mapstruct
[Project Lombok 1.18.8]: https://mvnrepository.com/artifact/org.projectlombok/lombok/1.18.8
[Javax Inject 1]: https://mvnrepository.com/artifact/javax.inject/javax.inject/1
[Apache HttpClient 4.3.4]: https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient/4.3.4
[Jackson Databind 2.10.0.pr1]: https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.10.0.pr1
[Cloud Run]: https://cloud.google.com/run/
[Cloud Storage]: https://cloud.google.com/storage/
[Cloud Firestore]: https://firebase.google.com/
[Cloud Pub/Sub]: https://cloud.google.com/pubsub
[Comparing OSDU and OpenDES Semantics]: https://gitlab.opengroup.org/osdu/opendes-contribution-wiki/wikis/OSDU-(C)/Design-and-Implementation/Entity-and-Schemas/Comparing-OSDU-&-OpenDES-Schema-Semantics