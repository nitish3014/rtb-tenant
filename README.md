## Tenant Service
## Section 1: Adding DB and packages

* To add DB, update the `application.yml` file.

```  yml
 spring: 
  datasource: 
   driverClassName: org.postgresql.Driver 
   url: jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME} 
   username: ${DATABASE_USERNAME} 
   password: ${DATABASE_PASSWORD}
```  

* New libraries can be added via `build.gradle` file.
```  java
ext {
   // Add a version variable
   springVersion = "2.4.0"
}  
  
dependencies {  
   // Add the library in correct format
   implementation "org.springframework.boot:spring-boot-starter-data-jpa:${springVersion}" 
}
 ```  

## Section 2: Logging
* For enabling logging just go with the instructions in logback.xml under src/main/resources.
* If not enabled you will only get ERROR level logs as your class inherits from root logger which is ancestor of all the loggers.
<https://www.baeldung.com/logback#:~:text=Logback%20Architecture&text=A%20logger%20is%20a%20context,have%20more%20than%20one%20Appender.>

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogExample {
    public void someMethod() {
        log.info("Doing stuff");
    }
}
```