# Facebook Login Selenium (Java + Maven)

This project contains one Selenium runner that opens the Facebook login page and prints the page title.

## File

- `src/main/java/com/naveen/selenium/SimpleSeleniumApp.java`

## Prerequisites

1. Java 17+
2. Maven
3. Google Chrome

## Run

```powershell
mvn -q exec:java
```

## Run in headless mode

```powershell
mvn -q exec:java "-Dexec.args=--headless"
```

