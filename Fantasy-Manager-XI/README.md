# Fantasy-Manager-XI

Fantasy-Manager-XI is a JavaFX desktop application for football club management. It lets a user log in as a club, inspect the squad, scout players, use a public transfer market, and manage private transfer offers through a socket-based backend server.

## Features

- JavaFX desktop client with multiple FXML-based screens
- Club login with preloaded credentials
- Squad view and player detail pages
- Public transfer market for listing and buying players
- Scouting screen for browsing players across clubs
- Private club-to-club transfer offers
- Shared in-memory server state for squads, budgets, listings, and offers

## Tech Stack

- Java 21
- JavaFX 21
- Gradle 8 via Gradle Wrapper
- ControlsFX and related JavaFX UI libraries
- Plain Java sockets for client-server communication

## Project Structure

```text
Fantasy-Manager-XI/
|-- src/main/java/org/buet/fantasymanagerxi
|   |-- Launcher.java
|   |-- controllers and JavaFX application classes
|   |-- model/
|   |-- server/
|   `-- util/
|-- src/main/resources/org/buet/fantasymanagerxi
|   |-- fxml/
|   |-- css/
|   |-- data/
|   `-- images/
|-- build.gradle.kts
|-- settings.gradle.kts
|-- gradlew
`-- gradlew.bat
```

## Prerequisites

Before installing or running the project, make sure you have:

- JDK 21 installed
- `JAVA_HOME` pointing to the JDK installation
- An internet connection the first time Gradle downloads dependencies

You do not need to install Gradle manually because the wrapper is included.

## Installation

1. Open a terminal in the project directory:

```powershell
cd Fantasy-Manager-XI
```

2. Build the project with the Gradle wrapper.

Windows:

```powershell
.\gradlew.bat clean build
```

macOS/Linux:

```bash
./gradlew clean build
```

If the build succeeds, Gradle will compile the Java sources and place artifacts under `build/`.

## Running the Application

The system has two runtime parts:

- a transfer market server
- a JavaFX desktop client

Start the server first, then launch the client in a separate terminal.

### 1. Start the Server

Windows:

```powershell
.\gradlew.bat runServer
```

macOS/Linux:

```bash
./gradlew runServer
```

The server listens on `localhost:5000`.

### 2. Start the Client

Open a second terminal in the same directory and run:

Windows:

```powershell
.\gradlew.bat run
```

macOS/Linux:

```bash
./gradlew run
```

The client launches the login screen and connects to the local server.

## Demo Login Credentials

Use one of the preloaded club accounts below:

| Club | Password |
| --- | --- |
| `CHELSEA` | `1234` |
| `LIVERPOOL` | `5678` |
| `ARSENAL` | `2323` |
| `MANUTD` | `6767` |
| `MANCITY` | `7878` |
| `SPURS` | `4353` |

These credentials come from `src/main/resources/org/buet/fantasymanagerxi/data/ValidLoginInfo.txt`.

## Running from an IDE

If you prefer IntelliJ IDEA or another Java IDE, use these main classes:

- Client: `org.buet.fantasymanagerxi.Launcher`
- Server: `org.buet.fantasymanagerxi.server.TransferMarketServer`

Run the server configuration first, then the client configuration.

## Build Output

Useful generated artifacts include:

- `build/libs/Fantasy-Manager-XI-1.0-SNAPSHOT.jar`
- `build/distributions/`
- `build/reports/`

## Notes and Current Limitations

- The client expects the server to be available on `localhost:5000`.
- The signup screen is not currently wired into the server-side login credential file used by the live login flow.
- This project does not currently include an automated test suite under `src/test`.

## Additional Documentation

For a deeper architectural breakdown of controllers, models, resources, and runtime flow, see `documentation.md`.
