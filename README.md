# Product Verification System

This is a Dual-Service Product Verification System built with Spring Boot and Gradle.

## Components

### 1. Gateway Service (Service A)
- **Port:** 8080
- **Public Endpoint:** `GET /public/products/{id}`
- **Secure Endpoint:** `GET /secure/products/{id}` (Requires Basic Auth)
- **Features:**
    - ID validation (must be positive numeric).
    - Forwards requests to Inventory Service using an internal API key.
    - Basic Authentication:
        - **Username:** `user`
        - **Password:** `password`

### 2. Inventory Service (Service B)
- **Port:** 8081
- **Internal Endpoint:** `GET /internal/check/{id}`
- **Features:**
    - Custom Header Authentication (`X-Service-Key: internal-secret-key`).
    - ID validation.
    - In-memory product data.
    - Simulated 10% Service Unavailable (503) failure rate.

## How to Run

1.  **Clone the repository.**
2.  **Open as a Gradle project** in IntelliJ IDEA or Eclipse.
3.  **Run Inventory Service:**
    - Navigate to `inventory-service` and run `com.product.verification.inventory.InventoryApplication`.
4.  **Run Gateway Service:**
    - Navigate to `gateway-service` and run `com.product.verification.gateway.GatewayApplication`.

## Testing the API

### Public Access
```bash
curl http://localhost:8080/public/products/1
```

### Secure Access (Authorized)
```bash
curl -u user:password http://localhost:8080/secure/products/2
```

### Secure Access (Unauthorized)
```bash
curl http://localhost:8080/secure/products/2
```

### Invalid ID
```bash
curl http://localhost:8080/public/products/abc
```

### Non-existent Product
```bash
curl http://localhost:8080/public/products/99
```

## Assumptions & Design Decisions
- **Java 17+** is used (utilizing `record` for `Product`).
- **Spring Security** is used for Basic Auth in Gateway Service.
- **RestTemplate** is used for service-to-service communication.
- **Logging** is implemented using SLF4J/Logback (default in Spring Boot).
- The 10% failure rate in Inventory Service is implemented using `java.util.Random`.
- Internal communication uses a hardcoded API key for simplicity, configurable via `application.properties`.


## Future Roadmap (TODO)

### 1. Containerization & Orchestration
- **Docker:** Create `Dockerfile` for both Gateway and Inventory services to ensure consistent environments across development and production.
- **Kubernetes:** Define K8s manifests (Deployments, Services, ConfigMaps) to manage scaling, health checks, and service discovery.

### 2. Intelligent Search Agent (AI Integration)
- **Semantic Search:** Implement Spring AI with a Vector Store (e.g., SimpleVectorStore) to allow natural language product queries 
- (e.g., "Find me something cheesy under $15") rather than just a numeric ID.
- **AI Recommendation Agent:** Add a fallback mechanism that suggests similar products using vector similarity scores when a specific ID lookup fails.
- **Embedding Models:** Integrate OpenAI or local Transformers models to generate embeddings for product names and descriptions.

