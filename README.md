# URL Shortener Service

A production-ready URL shortening service built with Kotlin and Ktor framework, featuring environment-based
configuration, metrics monitoring, and Kubernetes deployment support.

## Features

- 🔗 URL shortening with Base62 encoding
- 🚀 Fast redirects (301 permanent redirects)
- 📊 Dropwizard metrics integration
- 🐳 Kubernetes-ready with ConfigMaps
- 🔄 Environment-based configuration (local, dev, prod)
- 💾 H2 database with HikariCP connection pooling
- ⏰ Automatic URL expiration (configurable, default 30 days)

## Tech Stack

- **Language**: Kotlin 2.0
- **Framework**: Ktor 3.0
- **Database**: H2 (Exposed ORM)
- **DI**: Koin
- **Metrics**: Dropwizard Metrics
- **Build**: Gradle with Kotlin DSL
- **Deployment**: Kubernetes

## API Endpoints

| Method   | Endpoint                 | Description              | Request Body example             | Success Response example                                                              | Error Response                                                                          |
|----------|--------------------------|--------------------------|----------------------------------|---------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| **POST** | `/api/v1/shortUrl`       | Create a shortened URL   | `{"url": "https://example.com"}` | `200 OK`<br>`{"success": true, "url": "http://localhost:8080/abc123", "error": null}` | `400 Bad Request`<br>`{"success": false, "url": null, "error": "Invalid URL format"}`   |
| **GET**  | `/api/v1/{shortUrlCode}` | Get original URL as JSON | -                                | `200 OK`<br>`{"success": true, "url": "https://example.com", "error": null}`          | `404 Not Found`<br>`{"success": false, "url": null, "error": "Original URL not found"}` |
| **GET**  | `/{shortUrlCode}`        | Redirect to original URL | -                                | `301 Moved Permanently`<br>Redirects to original URL                                  | `404 Not Found`<br>`{"success": false, "url": null, "error": "Original URL not found"}` |
| **GET**  | `/healthcheck`           | Health check endpoint    | -                                | `200 OK`<br>`OK`                                                                      | -                                                                                       |

## Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle 9.1.0 or higher

### Running Locally

1. **Clone the repository**
2. **Run with Gradle** : `./gradlew run -Penv=local` (change env for different environments)
3. **Test the service**:

   #### Create a short URL

   `curl -X POST http://localhost:8080/api/v1/shortUrl
   -H "Content-Type: application/json"
   -d '{"url": "https://www.google.com"}'`

   Make sure you removed all escape slashes from the link which could be added by copying.

   #### Retrieve the original URL
   `curl -L http://localhost:8080/api/v1/abc123`

   #### Redirect (follow redirect with -L)

   `curl -L http://localhost:8080/abc123`

   Or simply click the shortened URL to access the original destination.

### Running with Different Environments

Local environment (default)
`./gradlew run`

Dev environment
`./gradlew run -Penv=dev`

Prod environment
`./gradlew run -Penv=prod`

#### Key Configuration Options

| Config Key              | Env Variable                | Description                   | Default                 |
|-------------------------|-----------------------------|-------------------------------|-------------------------|
| `app.host`              | `APP_HOST`                  | Service base URL              | `http://localhost:8080` |
| `app.version`           | `APP_VERSION`               | Application version           | `1.0`                   |
| `app.skipMetrics`       | `SKIP_METRICS`              | Disable metrics               | `false`                 |
| `app.apiUrl`            | `API_URL`                   | API base path                 | `/api/v1`               |
| `db.url`                | `DB_URL`                    | Database JDBC URL             | H2 in-memory            |
| `db.driver`             | `DB_DRIVER`                 | Database driver class         | `org.h2.Driver`         |
| `db.user`               | `DB_USER`                   | Database username             | -                       |
| `db.password`           | `DB_PASSWORD`               | Database password             | -                       |
| `db.maximumPoolSize`    | `DB_MAXIMUM_POOL_SIZE`      | HikariCP max pool size        | `5`                     |
| `db.skipInitialisation` | `DB_SKIP_INITIALISATION`    | Skip DB init for tests        | `false`                 |
| `db.expirationTimeDays` | `DB_EXPIRATION_TIME_DAYS`   | Time for DB entries to expire | 30                      |
| `db.expirationTimeDays` | `DB_CLEANUP_INTERVAL_HOURS` | Time for cleanup job interval | 24                      |
| `env_marker`            | `ENV_MARKER`                | Environment identifier        | `local`                 |

## Building

Build JAR
`./gradlew build`

Build without tests
`./gradlew build -x test`

## Testing

Run all tests
`./gradlew test`

## Kubernetes Deployment for local environment

1. Build your docker image with `docker build -t urlshortener:local .`
2. Apply your ConfigMap YAML:
   `kubectl apply -f urlshortener-configmap-local.yaml`
4. Apply your Deployment YAML (make sure image is urlshortener:local):
   `kubectl apply -f urlshortener-deployment-local.yaml`
5. Check pods status:
   `kubectl get pods`
   Wait for them to be Running and Ready.
6. View pod logs for config verification:
   `kubectl logs -f <pod-name>`
7. Port-forward your deployment for local access:
   `kubectl port-forward deployment/urlshortener 8080:8080`
7. Test app athttp://localhost:8080
8. Destroy the pods after tests are finished:
   `kubectl delete deployment urlshortener`

## Project Structure Highlights

- **Framework**: Built with Ktor considering the size of the project
- **Dependency Injection**: Koin for clean DI
- **Exception Handling**: Global error handling with StatusPages
- **Environment Config**: Flexible multi-environment setup
- **Metrics**: Built-in performance monitoring
- **Database**: Exposed ORM with connection pooling

## Author

**Ivan Ponomarev**  
Email: vankap0n@gmail.com  
License: MIT