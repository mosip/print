# Print Service

[![Maven Package upon a push](https://github.com/mosip/print/actions/workflows/push-trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/print/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=release-1.3.x&project=mosip_print&id=mosip_print&metric=alert_status)](https://sonarcloud.io/dashboard?branch=release-1.3.x&id=mosip_print)

## Overview

The **Print Service** is a reference implementation in MOSIP that handles the printing of credentials such as `euin`, `reprint`, and `qrcode` in PDF format. This service is designed to be customized and utilized by card printing agencies onboarding as [Credential Partners](https://docs.mosip.io/1.2.0/partners#credential-partner-cp).

It operates in an event-driven flow:
1. **Receive Event**: Listens for print request events from WebSub.
2. **Fetch Template**: Retreives the appropriate template from Masterdata.
3. **Generate PDF**: Decrypts resident data and converts it into a PDF card using the template.
4. **Upload**: Uploads the generated PDF to [DataShare](https://docs.mosip.io/1.2.0/modules/data-share).
5. **Notify**: Publishes a status event with the DataShare link back to WebSub.

The flow is visualized below:

![](docs/print-service.png)

## Features

- **Template-Based Printing**: Utilizes Velocity and iText to generate PDFs based on customizable templates.
- **Secure Data Handling**: Decrypts sensitive resident data using partner private keys (`.p12`).
- **DataShare Integration**: Securely uploads generated credential documents.
- **Credential Support**: Native support for EUIN, Reprint, and QR Code credential types.

## Services

The Print project consists of the following service:

1. **[Print Service](.)** (`print`) - The core Spring Boot application responsible for processing print requests and generating the credential PDFs.

## Database
NA (The service relies on Object Store/DataShare and Masterdata; it does not maintain its own primary database).

## Local Setup

The project can be set up in two ways:

1. [Local Setup (for Development or Contribution)](#local-setup-for-development-or-contribution)
2. [Local Setup with Docker (Easy Setup for Demos)](#local-setup-with-docker-easy-setup-for-demos)

### Prerequisites

Before you begin, ensure you have the following installed:

- **JDK**: 21
- **Maven**: 3.9.6 (or compatible 3.x version)
- **Docker**: Latest stable version (optional for local run)

### Runtime Dependencies

Ensure the following artifacts are available in the classpath or loader path:

- `kernel-auth-adapter.jar` - For IAM authentication.

### Configuration

- Print Service uses configuration files from the **[mosip-config repository](https://github.com/mosip/mosip-config/tree/master)**.
- Refer to the tagged version corresponding to your release.
- **Partner Key**: For local development, place your partner `.p12` file in the `src/main/resources` folder to enable decryption.

## Installation

### Local Setup (for Development or Contribution)

1. Ensure the **Config Server** is running and accessible.To run config server [check here.](https://github.com/mosip/mosip-config/blob/master/README.md)

2. Clone the repository:

```text
git clone https://github.com/mosip/print.git
cd print
```

3. Build the project:

```text
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

4. Start the application:
    - Run via IDE or command line:
      ```text
      java -Dloader.path=<path-to-kernel-auth-adapter-jar> \
      -jar target/print-*.jar
      ```

### Local Setup with Docker (Easy Setup for Demos)

#### Option 1: Pull from Docker Hub

Recommended for quick demos and testing.

```text
docker pull mosipid/print-service:1.3.0
```

Run the service:

```text
docker run -d -p 8099:8099 --name print-service mosipid/print-service:1.3.0
```

#### Option 2: Build Docker Images Locally

Recommended for developers.

1. Build the project (as shown in Local Setup).

2. Build the Docker image:

```text
docker build -t print-service:local .
```

3. Run the service:

```text
docker run -d -p 8099:8099 --name print-service print-service:local
```

#### Verify Installation

Check that the container is running:

```text
docker ps
```

The service runs on port `8099` by default.

## Deployment

### Kubernetes

To deploy Print Service on a Kubernetes cluster, refer to the [Sandbox Deployment Guide](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

## Documentation

For additional details, refer to the documents listed below:

- **[Build and Run Guide](docs/build-and-run.md)**: Detailed instructions for building and running the service.
- **[Configuration Guide](docs/configuration.md)**: Details on configuration properties and template setup.

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/print/issues)

## License

This project is licensed under the [Mozilla Public License 2.0](LICENSE).
