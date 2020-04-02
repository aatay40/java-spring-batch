# Trustonic Code Test

This code test is designed to allow you to demonstrate your development skills. Please can you carry out the following task and return the solution in the form of a single archive. The solution should include a readme file, source code, any additional support files as needed, and a short script to compile and run the application.

## The objective

To validate given devices detail via given certificates from remote url. For this task, springboot is used for scheduling. 
RestTemplate is used for remote certification reads. 
CVS file is used for input source (under resources). 
Final results are logged to console.  

## Dependencies

### Java

This application is built for an runs on JDK 1.8. If you wanted to leverage JDK 9, you will need to make changes to your JVM to add modules, such as JAXB, for dependencies to work correctly. 

### Apache Maven

The project makes use of Maven as the build tool. The Apache Maven wrapper is used to assure consistency of version. This application was built and runs with Apache Maven version 3.6.0.

### IDE

The project was built using IntelliJ IDEA.

### Spring Boot

The project leverages Spring Boot version 2.2.5.RELEASE. 

### Spring Batch

The project leverages Spring Batch version 4.0.0.RELEASE.

## Development

To start your application in the dev profile, simply Maven, SpringBoot tools can be used:

    - Maven Project > Lifecyle > Test
    - SpringBoot application > Run

## Testing

To launch your application's tests, run:

    java -jar {jar-path}/demo-0.0.1-SNAPSHOT.jar

