# FoodTec Food Item Verification System

This is a Dual-Service Food Item Verification System built for **FoodTec**, a restaurant software provider.

## Components

### 1. FoodTec Gateway Service (Service A)
- **Port:** 8080
- **Public Endpoint:** `GET /public/food-items/{id}`
- **Secure Endpoint:** `GET /secure/food-items/{id}` (Requires Basic Auth)
- **Features:**
    - ID validation (must be positive numeric).
    - Forwards requests to FoodTec Inventory Service using an internal API key.
    - Basic Authentication:
        - **Username:** `user`
        - **Password:** `password`

### 2. FoodTec Inventory Service (Service B)
- **Port:** 8081
- **Internal Endpoint:** `GET /internal/food-items/{id}`
- **Features:**
    - Custom Header Authentication (`X-Service-Key: internal-secret-key`).
    - ID validation.
    - In-memory food item data (Pizza, Subs, Drinks, etc.).
    - Simulated 10% Service Unavailable (503) failure rate.

## Resilience & Best Practices

- **Retry Mechanism:** The Gateway Service uses **Spring Retry** to handle transient `503 Service Unavailable` errors from the Inventory Service. It will automatically retry the request up to **3 times** with a short delay (100ms) before returning an error to the client.
- **AOP-based Implementation:** Uses `@EnableRetry` and `@Retryable` for a clean, declarative implementation.

## How to Run

1.  **Clone the repository.**
2.  **Open as a Gradle project** in IntelliJ IDEA or Eclipse.
3.  **Run Inventory Service:**
    - Navigate to `inventory-service` and run `com.product.verification.inventory.FoodTecInventoryApplication`.
4.  **Run Gateway Service:**
    - Navigate to `gateway-service` and run `com.product.verification.gateway.FoodTecGatewayApplication`.

## Testing the API

### Public Access
```bash
curl http://localhost:8080/public/food-items/1
```

### Secure Access (Authorized)
```bash
curl -u user:password http://localhost:8080/secure/food-items/2
```

### Secure Access (Unauthorized)
```bash
curl http://localhost:8080/secure/food-items/2
```

### Invalid ID
```bash
curl http://localhost:8080/public/food-items/abc
```

### Non-existent Food Item
```bash
curl http://localhost:8080/public/food-items/99
```

## Assumptions & Design Decisions
- **Java 17+** is used (utilizing `record` for `FoodItem`).
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

