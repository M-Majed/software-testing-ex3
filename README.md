# Software Testing Exercise 3

A Java-based software testing exercise focused on designing and validating service-layer components using automated tests.

## Overview

This project contains a small application structure with domain models, repositories, services, and unit tests. The implementation demonstrates software testing concepts including service validation, repository interaction, and test-driven verification.

## Technologies

- Java
- Maven
- JUnit testing framework
- Object-oriented programming principles

## Project Structure

```
.
├── pom.xml
├── src
│   ├── main
│   │   └── java
│   │       └── com/iut
│   │           ├── BankService.java
│   │           ├── Database.java
│   │           ├── Repository.java
│   │           ├── account
│   │           │   ├── model
│   │           │   ├── repo
│   │           │   └── service
│   │           └── user
│   │               ├── model
│   │               ├── repo
│   │               └── service
│   └── test
│       └── java
│           └── com/iut
│               └── AccountServiceTest.java
└── README.md
```

## Running Tests

Make sure Maven is installed, then run:

```bash
mvn test
```

Maven will compile the project and execute the available unit tests.

## Testing Scope

The project covers testing of application services and business logic, including:

- Account service behavior
- Repository operations
- Data validation
- Expected and invalid scenarios
- Service-level unit testing

## Future Improvements

- Increase test coverage
- Add integration tests
- Add mocking framework support (Mockito)
- Improve project documentation
- Add CI workflow for automated testing

## License

This project is intended for educational purposes.
