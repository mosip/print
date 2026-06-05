# AGENTS.md — MOSIP Print Service

This file provides guidance to AI agents when working with code in this repository.

---

## Project Overview

The **MOSIP Print Service** is a Spring Boot microservice that generates PDF identity credentials (eUIN cards, reprint cards, QR code cards) for residents enrolled in MOSIP — the Modular Open Source Identity Platform.

It sits at the tail end of the **ID Lifecycle Management** pipeline:
1. A resident registers or updates their ID at a registration centre.
2. Demographic and biometric data are captured and processed by Registration Processor.
3. Credentials are issued and an event is published to WebSub.
4. **This service** consumes the credential event, decrypts resident data, renders a template, generates a PDF, uploads it to DataShare, and publishes a completion event.

The service is a **reference implementation** for credential partner onboarding. Deployers are expected to customise templates and partner certificates.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.3 + Spring Cloud Config |
| Build | Maven 3.9.6 |
| PDF generation | iText 7 (html2pdf 2.0.0) + iText 5 (legacy) |
| Template engine | Apache Velocity 1.7 |
| QR codes | ZXing (Google) 3.4.1 |
| Cryptography | BouncyCastle 1.66, JWT 3.8.1 |
| Messaging | MOSIP WebSub (kernel-websub-client-api) |
| Biometrics | CBEFF format via kernel-cbeffutil |
| Tests | JUnit 5, Mockito 4.11.0, JaCoCo |
| Containerisation | Docker (Alpine-based), Helm for Kubernetes |

---

## Repository Layout

```
print/
├── src/main/java/io/mosip/print/
│   ├── PrintPDFApplication.java          # Spring Boot entry point
│   ├── controller/Print.java             # Single REST endpoint (WebSub callback)
│   ├── service/
│   │   ├── PrintService.java             # Interface
│   │   └── impl/
│   │       ├── PrintServiceImpl.java     # Core orchestration logic (~500 lines)
│   │       ├── PDFGeneratorImpl.java     # iText PDF generation
│   │       ├── QrcodeGeneratorImpl.java  # ZXing QR code generation
│   │       ├── TemplateManagerImpl.java  # Velocity template processing
│   │       ├── CbeffImpl.java            # Biometric (CBEFF) handling
│   │       └── UinCardGeneratorImpl.java
│   ├── util/                             # 50+ utility classes
│   │   ├── CryptoUtil.java               # Encryption / decryption
│   │   ├── DataShareUtil.java            # DataShare upload
│   │   ├── TemplateGenerator.java        # Template instantiation
│   │   ├── DigitalSignatureUtility.java  # PDF digital signing
│   │   ├── WebSubSubscriptionHelper.java # WebSub subscription management
│   │   └── RestApiClient.java            # HTTP client wrapper
│   ├── dto/                              # 65+ Data Transfer Objects
│   ├── constant/                         # 35+ constant classes
│   ├── exception/                        # 35+ custom exception classes
│   ├── model/                            # Domain models (Event, StatusEvent)
│   └── entity/                           # CBEFF entity models
├── src/main/resources/
│   ├── application-local1.properties     # Local dev config (port 8088)
│   ├── bootstrap.properties              # Spring Cloud Config bootstrap
│   └── partner.p12                       # Sample partner certificate
├── src/test/java/io/mosip/print/         # 45+ unit test files
├── docs/
│   ├── build-and-run.md
│   ├── configuration.md
│   └── print-service.png                 # Architecture diagram
├── deploy/                               # Shell scripts for bare-metal deploy
├── helm/                                 # Kubernetes Helm charts
├── Dockerfile
└── pom.xml
```

---

## Build and Run

### Build (skip tests)
```bash
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

### Build with tests
```bash
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

### Run tests only
```bash
mvn test
```

### Run the JAR
```bash
java -Dloader.path=<path-to-kernel-auth-adapter.jar> \
     -Dspring.profiles.active=local1 \
     -jar target/print-*.jar
```

### Docker
```bash
# Build image
docker build -t print-service:local .

# Run container
docker run -d -p 8099:8099 --name print-service print-service:local
```

Service listens on **port 8099** (default) or **port 8088** (local1 profile).

---

## Key Configuration Properties

All runtime configuration is sourced from a **Spring Cloud Config Server** (mosip-config repository). Local overrides live in `application-local1.properties`.

| Property | Purpose |
|---|---|
| `mosip.event.hubURL` | WebSub hub URL |
| `mosip.partner.id` | This service's partner ID (`mpartner-default-print`) |
| `mosip.event.topic` | WebSub topic to subscribe to |
| `mosip.event.callBackUrl` | Public URL of the `/print/callback/notifyPrint` endpoint |
| `mosip.event.secret` | WebSub HMAC secret |
| `mosip.datashare.partner.id` | DataShare partner (`mpartner-default-resident`) |
| `mosip.datashare.policy.id` | DataShare policy (`mpolicy-default-resident`) |
| `mosip.print.crypto.p12.filename` | Partner P12 certificate filename |
| `mosip.print.crypto.p12.password` | P12 password |
| `mosip.print.crypto.p12.alias` | P12 key alias |
| `mosip.template-language` | Language code for template lookup (e.g. `eng`) |
| `mosip.kernel.pdf_owner_password` | PDF encryption owner password |
| `mosip.supported-languages` | Comma-separated ISO language codes |
| `mosip.iam.adapter.clientid` | Keycloak client ID for this service |
| `mosip.iam.adapter.issuerURL` | Keycloak realm issuer URL |
| `TEMPLATES` | Masterdata templates endpoint |
| `CREATEDATASHARE` | DataShare create endpoint |
| `PDFSIGN` | Keymanager PDF sign endpoint |

---

## REST API

### `POST /v1/print/print/callback/notifyPrint`

The single public endpoint. Called by the WebSub hub when a credential event is published.

**Headers required:**
- `x-hub-signature` — HMAC-SHA256 signature from WebSub hub (validated by the service)

**Body:** WebSub notification JSON containing the credential event payload.

**Flow:**
1. Validates WebSub signature and intent.
2. Extracts and decrypts credential data using the partner P12 key.
3. Fetches the HTML template for the card type from Masterdata.
4. Renders the template with resident data using Velocity.
5. Converts HTML to PDF via iText html2pdf.
6. Optionally embeds a QR code and applies a digital signature.
7. Uploads the PDF to DataShare.
8. Publishes a `CREDENTIAL_STATUS_UPDATE` event back to WebSub.

---

## Data Flow and Integration Points

```
WebSub Hub
    │  POST /print/callback/notifyPrint
    ▼
Print.java (controller)
    │  generateCard(event)
    ▼
PrintServiceImpl.java
    ├── KeyManager / CryptoUtil        — decrypt credential data
    ├── Masterdata (TEMPLATES)         — fetch HTML template
    ├── TemplateManagerImpl            — merge template + data (Velocity)
    ├── PDFGeneratorImpl               — HTML → PDF (iText)
    ├── QrcodeGeneratorImpl            — embed QR code (ZXing)
    ├── DigitalSignatureUtility        — sign PDF (Keymanager PDFSIGN)
    ├── DataShareUtil                  — upload PDF (DataShare)
    └── WebSubSubscriptionHelper       — publish status event
```

External services consumed:
- **Keymanager** — decryption and PDF signing
- **Masterdata** — HTML templates
- **DataShare** — secure PDF upload
- **WebSub** — event subscription and publication
- **IAM (Keycloak)** — token-based authentication for all REST calls

---

## ID Lifecycle Context

MOSIP's ID Lifecycle Management covers the full journey of an identity:

- **Registration** — Resident attends a registration centre; demographic and biometric data are captured.
- **Processing** — Registration Processor de-duplicates and validates the data.
- **Activation** — A UIN (Unique Identification Number) is issued; the ID is active.
- **Credential Issuance** — Credentials (VCs, PDF cards) are generated for configured partners.
- **Updates** — Resident can update demographics or biometrics; a new credential is re-issued.
- **Deactivation / Reactivation** — IDs can be deactivated and reactivated via resident services.
- **Print / Reprint** — This service handles the print partner use case: it receives credential events and produces PDF identity cards.

This service handles the **Credential Issuance → Print** leg. The `UinCardType` constant class enumerates supported card types: `EUIN`, `REPRINT`, `QRCODE`.

---

## Working with This Codebase

### Before making changes
1. Read `PrintServiceImpl.java` — it is the central orchestrator; most changes flow through it.
2. Check `PrintRestClientServiceImpl.java` for how external REST calls are made before adding new ones.
3. Verify properties exist in `application-local1.properties` or `docs/configuration.md` before introducing new config keys.

### Adding a new card type
1. Add a constant to `UinCardType.java`.
2. Add a template to Masterdata and reference it in `PrintServiceImpl.generateCard()`.
3. Add a corresponding branch in `TemplateGenerator.java` if template rendering differs.

### Modifying template rendering
Templates are HTML files processed by Velocity. Variables are injected via `VelocityContext`. See `TemplateManagerImpl.java` and `TemplateGenerator.java`.

### Cryptography
Do not modify `CryptoUtil.java` without understanding the P12 key usage. The partner certificate must match the credential encryption key registered in Keymanager.

### Tests
- Unit tests are in `src/test/java/io/mosip/print/`.
- Tests use Mockito; external service calls are mocked.
- Run `mvn test` before committing any change.
- JaCoCo generates a coverage report under `target/site/jacoco/`.

### Docker image
The `Dockerfile` copies the built JAR and runs it. The image is published to Docker Hub as `mosipid/print-service`. The current version under development is `1.4.0-SNAPSHOT`.

---

## Conventions

- Package root: `io.mosip.print`
- All Spring beans follow constructor injection where possible.
- REST responses are wrapped in a standard `ResponseWrapper<T>` DTO.
- Exceptions extend `BaseCheckedException` or `BaseUncheckedException` from the MOSIP kernel.
- Logging uses `io.mosip.print.logger.PrintLogger` (SLF4J wrapper); do not use `System.out`.
- Constants live in dedicated files under the `constant/` package; do not hard-code strings inline.
- All external HTTP calls go through `PrintRestClientService` — do not use `RestTemplate` directly in service classes.

---

## Useful References

- MOSIP documentation: https://docs.mosip.io/1.2.0/
- ID Lifecycle Management: https://docs.mosip.io/1.2.0/id-lifecycle-management
- mosip-config repository: contains `print-default.properties` with all runtime properties
- MOSIP functional tests: https://github.com/mosip/mosip-functional-tests
- Docker Hub image: `mosipid/print-service`
- Build and run guide: `docs/build-and-run.md`
- Configuration guide: `docs/configuration.md`
