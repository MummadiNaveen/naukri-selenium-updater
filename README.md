# Naukri Resume Updater (Java + Maven)

This project logs into Naukri and uploads a resume file from `src/main/resources`.

## File

- `src/main/java/com/naveen/selenium/SimpleSeleniumApp.java`

## Prerequisites

1. Java 17+
2. Maven
3. Google Chrome

## Run

```powershell
 $env:NAUKRI_EMAIL="your_email"
 $env:NAUKRI_PASSWORD="your_password"
 $env:RESUME_FILE_NAME="Gowtham_sv_jr.pdf"
mvn -q exec:java
```

## Run in headless mode

```powershell
 $env:NAUKRI_EMAIL="your_email"
 $env:NAUKRI_PASSWORD="your_password"
 $env:RESUME_FILE_NAME="Gowtham_sv_jr.pdf"
mvn -q exec:java "-Dexec.args=--headless"
```

