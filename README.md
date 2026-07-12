# CrisisGrid — Event-Driven Emergency Response Platform

A production-style microservices platform for coordinating emergency response resources during disaster events. Built with Java Spring Boot, Apache Kafka, Redis, PostgreSQL, and an AI-powered triage pipeline using Groq (LLaMA 3.3 70B).

---

## Architecture Overview

```
React Frontend (crisisgrid-sentinel)
         │
         ▼
   [API Gateway]
         │
   ┌─────┴──────────────────────┐
   │                            │
crisis-service            resource-service
(port 8081)               (port 8082)
   │    │                      │    │
   │  Kafka ──────────────────►│  Kafka
   │    │                      │
   │    └──────► ai-service ───┘
   │              (port 8083)
   │                  │
   └──────────► notification-service
```

**4 Spring Boot microservices** communicate asynchronously via Apache Kafka. The AI service uses OpenFeign to call back into crisis-service and resource-service for data enrichment and automated allocation.

---

## Services

### 1. Crisis Service (`port 8081`)
Core service responsible for crisis lifecycle management.
- REST APIs for creating, updating, and querying crisis events
- JWT-based authentication with Spring Security
- Redis caching (TTL: 15 min) for frequently accessed crisis state
- Publishes `crisis.created` and `crisis.updated` Kafka events
- Geo-radius queries for locating nearby resources (default 50km, max 200km)
- Prometheus metrics endpoint for observability

### 2. Resource Service (`port 8082`)
Manages emergency resource inventory and deployment.
- Tracks resources: AMBULANCE, FIRE_TRUCK, RESCUE_TEAM, POLICE_UNIT, MEDICAL_TEAM, HELICOPTER, BOAT, SHELTER
- REST APIs for resource registration, status updates, and allocation
- Publishes `resource.allocated` and `resource.deployed` Kafka events
- Consumes crisis events to trigger automatic resource availability checks
- Custom exceptions: `ResourceNotFoundException`, `ResourceAlreadyDeployedException`

### 3. AI Service (`port 8083`)
AI-powered crisis triage and resource recommendation engine.
- Consumes `crisis.created` Kafka events and triggers 6-step analysis pipeline:
  1. **CrisisClassifierAgent** — classifies crisis type (FIRE, FLOOD, MEDICAL, ACCIDENT, etc.)
  2. **SeverityScorerAgent** — scores severity 1–10 with natural language explanation
  3. **Fetch nearby resources** — calls resource-service via OpenFeign
  4. **ResourceAllocatorAgent** — recommends optimal resource allocation
  5. **Update crisis** — patches crisis-service with AI analysis results
  6. **Auto-allocate** — automatically deploys top 3 resources if severity ≥ 7
- Powered by **Groq API** (LLaMA 3.3 70B Versatile) via Spring AI
- Uses OkHttp Feign client to support PATCH method calls

### 4. Notification Service
Multi-channel notification delivery for crisis and resource events.
- Consumes both `crisis.*` and `resource.*` Kafka topics
- Supports Email, SMS, and Push notification channels
- `NotificationLog` entity for delivery tracking and audit trail
- Bulk notification support via `BulkNotificationRequest`
- Priority-based routing (CRITICAL, HIGH, MEDIUM, LOW)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Messaging | Apache Kafka |
| Caching | Redis (Lettuce client) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT |
| Inter-service | OpenFeign (OkHttp) |
| AI / LLM | Groq API — LLaMA 3.3 70B |
| Containerization | Docker |
| Build | Maven |
| Frontend | React + TypeScript |

---

## Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose
- PostgreSQL (port 5432)
- Redis (port 6379)
- Kafka (port 9092)

### Environment Variables

```bash
# crisis-service
JWT_SECRET=your-base64-encoded-secret
REDIS_PASSWORD=your-redis-password

# ai-service
GROQ_API_KEY=your-groq-api-key
```

### Run with Docker

```bash
# Start infrastructure
docker-compose up -d

# Build all services
mvn clean install -DskipTests

# Start services
cd crisis-service && mvn spring-boot:run
cd resource-service && mvn spring-boot:run
cd ai-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

---

## API Endpoints

### Crisis Service
```
POST   /api/crises                    - Report a new crisis event
GET    /api/crises/{id}               - Get crisis details (Redis cached)
PATCH  /api/crises/{id}               - Update crisis status/AI analysis
GET    /api/crises?status=ACTIVE      - List active crises
```

### Resource Service
```
POST   /api/resources                 - Register a new resource
GET    /api/resources/nearby          - Find resources within radius
POST   /api/resources/allocate        - Allocate resources to a crisis
PATCH  /api/resources/{id}/status     - Update resource deployment status
```

### AI Service
```
POST   /api/ai/analyze                - Trigger manual AI analysis for a crisis
GET    /api/ai/analysis/{crisisId}    - Get AI analysis result
```

### Notification Service
```
POST   /api/notifications             - Send a notification
POST   /api/notifications/bulk        - Send bulk notifications
GET    /api/notifications/{id}        - Get notification delivery status
```

---

## Event Flow

```
1. User reports crisis → POST /api/crises
2. crisis-service saves to PostgreSQL, publishes → [crisis.created]
3. ai-service consumes [crisis.created]
   → Classifies type, scores severity (Groq/LLaMA)
   → Fetches nearby resources from resource-service (Feign)
   → If severity ≥ 7: auto-allocates top 3 resources
   → PATCHes crisis-service with AI analysis
4. resource-service publishes → [resource.allocated]
5. notification-service consumes both topics
   → Sends Email/SMS/Push to responders
```

---

## Project Structure

```
CrisisGrid/
├── crisis-service/         # Core crisis management + JWT auth
├── resource-service/       # Resource inventory and allocation
├── ai-service/             # Groq AI triage pipeline
├── notification-service/   # Multi-channel notification delivery
└── crisisgrid-sentinel/    # React + TypeScript frontend
```

---

## Key Design Decisions

- **Event-driven over synchronous REST** between services to ensure resilience — a downstream service failure doesn't block crisis creation
- **Redis caching** on crisis state to reduce DB load on frequently polled endpoints
- **AI auto-allocation threshold at severity ≥ 7** to balance automation with human oversight on lower-severity events
- **OkHttp for Feign** because Java's default `HttpURLConnection` does not support the PATCH method natively
- **AI service has no database** — stateless by design, relies on Feign calls for data

---

## Author

**Surya Amujuri**  
Java Backend Developer | Spring Boot | Microservices | Kafka  
[LinkedIn](https://www.linkedin.com/in/suryaamujuri-82a15a206) | suryaamujuri2002@gmail.com
