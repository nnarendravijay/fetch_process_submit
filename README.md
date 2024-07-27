# Fetch Process Submit

## Overview
This project demonstrates fetching data from an API, processing it, and submitting the processed data to another API. It includes comprehensive error handling, resiliency features, and follows best practices in software engineering.

## Prerequisites
- Java 17
- Gradle 8.0 or higher

## Setup
1. Clone the repository:
    ```sh
    git clone https://github.com/nnarendravijay/fetch_process_submit.git
    cd fetch_process_submit
    ```

2. Set the required environment variables:
    ```sh
    export API_URL="your_api_url"
    export API_KEY="your_api_key"
    ```

## Tasks

### Running Tests
To run the unit tests:
```sh
./gradlew test
```
Test reports can be found in `build/reports/tests/test/index.html`.

### Running Static Code Analysis
To run Checkstyle, PMD, and SpotBugs:
```sh
./gradlew check
```
Reports for each tool can be found in:
- Checkstyle: `build/reports/checkstyle/main.html`
- PMD: `build/reports/pmd/main.html`
- SpotBugs: `build/reports/spotbugs/main.html`

### Generating Code Coverage Report
To generate a code coverage report using Jacoco:
```sh
./gradlew jacocoTestReport
```
The code coverage report can be found in `build/reports/jacoco/test/html/index.html`.

### Building the Application
To build the application:
```sh
./gradlew build
```
The build artifacts (JAR file) can be found in `build/libs/`.

### Running the Application
To run the application:
```sh
./gradlew run
```

## Additional Information
- Ensure that all environment variables are set correctly before running the application.
- Modify `build.gradle` for any specific configurations or additional tasks as required.