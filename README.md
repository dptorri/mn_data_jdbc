# mn_data_jdbc

Working with micronaut data api


#### 1. Setup gradle and java 
```
mn create-app example --build=gradle --lang=java
```
#### 2. Data Source configuration 
```
//build.gradle

annotationProcessor("io.micronaut.data:micronaut-data-processor") 
implementation("io.micronaut.data:micronaut-data-jdbc") 
implementation("io.micronaut.sql:micronaut-jdbc-hikari") 
runtimeOnly("com.h2database:h2") 

//application.yml

datasources:
  default:
    url: ${JDBC_URL:`jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`}
    username: ${JDBC_USER:sa}
    password: ${JDBC_PASSWORD:""}
    driverClassName: ${JDBC_DRIVER:org.h2.Driver}
```
#### 3. DB Schema creates tables BOOK and GENRE 
```
--- build.gradle ---

implementation("io.micronaut.flyway:micronaut-flyway")

--- application.yml

flyway:
  datasources:
    default:
      enabled: true
      
--- V1__schema.sql ---

DROP TABLE IF EXISTS BOOK;

CREATE TABLE GENRE (
  id    BIGINT SERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(255)              NOT NULL UNIQUE
);
```
