# Fleet Management Microservices

Logistics fleet management platform built with Spring Boot 4.1.1 and Spring Cloud 2025.1.3.

## Modules

| Module | Port | Database | Purpose |
|---|---|---|---|
| `eureka-server` | 8761 | – | Service registry |
| `api-gateway` | 8080 | – | Single entry point, JWT enforcement, routing |
| `auth-service` | 8081 | `fleet_auth` | Registration, login, JWT issuance |
| `vehicle-service` | 8082 | `fleet_vehicle` | Vehicle inventory, status, location |
| `trip-service` | 8083 | `fleet_trip` | Trip scheduling and lifecycle |
| `maintenance-service` | 8084 | `fleet_maintenance` | Maintenance tickets |

Databases are created automatically (`createDatabaseIfNotExist=true`); only a running
MySQL server is required.

## Open these URLs

Start the services (see **Run** below), then open:

| Page | URL |
|---|---|
| **Project homepage** | <http://localhost:8080> |
| **Swagger UI** (all four APIs, testable) | <http://localhost:8080/swagger-ui.html> |
| **Eureka dashboard** (service registry) | <http://localhost:8761> |

These addresses never change - they are fixed to the service ports.

### Demo order for a walkthrough

1. **Homepage** - the architecture, and all six services showing live status
2. **Eureka** - proof the services really discover each other
3. **Swagger** - log in, click **Authorize**, paste the token, call the APIs


## Prerequisites

- JDK 17 or newer
- MySQL 8 running on `localhost:3306`
- Maven (or use the bundled `./mvnw`)

### Required environment variables

No credentials are stored in this repository. Each service reads them from the
environment and refuses to start if they are missing.

| Variable | Purpose |
|---|---|
| `DB_PASSWORD` | MySQL password (**required**) |
| `JWT_SECRET` | Base64-encoded 256-bit HMAC key, shared by auth-service and the gateway (**required**) |
| `DB_USERNAME` | MySQL user (defaults to `root`) |
| `DB_HOST`, `DB_PORT` | MySQL location (default `localhost:3306`) |

Set them once on Windows:

```powershell
[Environment]::SetEnvironmentVariable("DB_PASSWORD","<your-password>","User")
[Environment]::SetEnvironmentVariable("JWT_SECRET","<base64-key>","User")
```

Restart the IDE afterwards - it only picks up environment variables at startup.

## Build

```bash
./mvnw clean package -DskipTests
```

## Run

Start in this order — each service registers with Eureka on boot, and the
gateway needs the registry to resolve `lb://` route targets.

```bash
./mvnw -pl eureka-server spring-boot:run
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl auth-service spring-boot:run
./mvnw -pl vehicle-service spring-boot:run
./mvnw -pl trip-service spring-boot:run
./mvnw -pl maintenance-service spring-boot:run
```

Each command runs in the foreground, so use a separate terminal per service.
Check the registry at <http://localhost:8761> — all five clients should appear.

## Security model

1. `auth-service` issues an HS256 JWT (`sub`=username, `uid`=user id, `role`=role).
2. `api-gateway` validates the token on every route except `/api/auth/register`,
   `/api/auth/login` and `/actuator/**`, then forwards the verified identity as
   `X-User-Id` / `X-Username` / `X-User-Role`, overwriting any client-supplied copies.
3. Downstream services trust those headers (`GatewayIdentityFilter`) and enforce
   roles with `@PreAuthorize`.

**The downstream services must not be exposed directly** — only the gateway
should be reachable from outside, otherwise the identity headers can be forged.

The signing key is shared between `auth-service` and `api-gateway` via the
`JWT_SECRET` environment variable. It has no fallback value in the configuration,
so neither service can start with a missing or accidentally committed key.

### Edge hardening

The gateway also applies, ahead of routing:

- **Rate limiting** - `gateway.rate-limit.requests-per-minute` (default 120), keyed by
  verified username, or by IP for anonymous traffic. Over budget returns `429` with
  `Retry-After`, and every response carries `X-RateLimit-Remaining`.
- **CORS** - an explicit origin allow-list (`gateway.cors.allowed-origins`), never a
  wildcard, because these endpoints carry bearer tokens.
- **Circuit breaker** - `trip-service` and `maintenance-service` wrap their Feign calls
  in resilience4j. After 50% of 10 calls fail the circuit opens for 10 seconds and
  calls fail fast into `VehicleClientFallback` instead of queueing against a dead
  service. The fallback never fakes a write, so the two databases cannot silently
  diverge.

## Roles

| Role | Capabilities |
|---|---|
| `ADMIN` | Everything, including vehicle deletion |
| `DISPATCHER` | Manage vehicles, schedule/cancel trips, run maintenance workflow |
| `DRIVER` | Read, start/complete own trips, report faults |

## API walkthrough

All calls go through the gateway on port 8080.

```bash
# 1. Register an admin (public)
curl -X POST localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123","role":"ADMIN"}'

# 2. Log in and keep the accessToken
curl -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# 3. Add a vehicle
curl -X POST localhost:8080/api/vehicles \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"registrationNumber":"KA-01-AB-1234","type":"TRUCK","location":"Bengaluru depot"}'

# 4. Schedule a trip (vehicle must be AVAILABLE)
curl -X POST localhost:8080/api/trips \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"vehicleId":1,"driverId":2,"origin":"Bengaluru","destination":"Chennai"}'

# 5. Start it - trip-service calls vehicle-service over Feign, vehicle becomes ON_TRIP
curl -X PATCH localhost:8080/api/trips/1/start -H "Authorization: Bearer $TOKEN"

# 6. Complete it - vehicle returns to AVAILABLE
curl -X PATCH localhost:8080/api/trips/1/complete -H "Authorization: Bearer $TOKEN"

# 7. Report a fault, then run the workshop flow
curl -X POST localhost:8080/api/maintenance \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"vehicleId":1,"issue":"Brake pads worn"}'
curl -X PATCH localhost:8080/api/maintenance/1/start -H "Authorization: Bearer $TOKEN"
curl -X PATCH localhost:8080/api/maintenance/1/resolve \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"resolutionNotes":"Pads replaced"}'
```

## State machines

**Vehicle**

```
AVAILABLE <-> ON_TRIP
AVAILABLE  -> UNDER_MAINTENANCE -> AVAILABLE
AVAILABLE  -> OUT_OF_SERVICE    -> AVAILABLE | UNDER_MAINTENANCE
ON_TRIP    -> UNDER_MAINTENANCE
```

**Trip**: `SCHEDULED -> IN_PROGRESS -> COMPLETED`, with `CANCELLED` reachable from
either active state.

**Maintenance**: `REPORTED -> IN_REPAIR -> RESOLVED`, with `CANCELLED` reachable
from either open state.

Illegal transitions return `409 Conflict` listing what was allowed.

## Inter-service communication

`trip-service` and `maintenance-service` call `vehicle-service` through OpenFeign
(`@FeignClient(name = "vehicle-service")`), resolved via Eureka and Spring Cloud
LoadBalancer — no hostnames or ports anywhere. A `RequestInterceptor` propagates
the caller's identity headers; an `ErrorDecoder` maps downstream `404`/`409` onto
local exception types.

**Known limitation:** remote status changes and local commits are not atomic. The
remote call is made before the local commit, so a downstream rejection rolls the
local transaction back — but if the commit fails *after* a successful remote call,
the vehicle is left in the new state. Closing that gap needs an outbox or saga.

## Error format

Every service returns the same shape:

```json
{
  "timestamp": "2026-09-22T10:15:30Z",
  "status": 409,
  "error": "Conflict",
  "message": "Vehicle 1 is ON_TRIP, not AVAILABLE",
  "path": "/api/trips",
  "fieldErrors": null
}
```

`fieldErrors` is populated only for validation failures (`400`).
