# LinkedIn Article — ready to publish

> **Fill every `[ ]` before posting.** Anything in square brackets is a placeholder only you can complete.
> **Only the team lead publishes**, and the article must name all three members.

---

## How to publish it (5 minutes)

1. Open LinkedIn on a **laptop** (the article editor is not on mobile)
2. On the **home feed**, click **"Write article"** — it sits just under the post box, next to *Start a post*
3. LinkedIn opens a full editor:
   - **Title** → paste the title below
   - **Cover image** → upload `architecture-diagram.png` (see the note at the end on converting the SVG)
   - **Body** → paste the article, section by section
4. Add the images where marked: architecture diagram, the Eureka dashboard screenshot, the Swagger screenshot, and your DTI artifacts (empathy map, persona, journey map)
5. Click **Publish** (top-right), then add the hashtags in the share box
6. **Copy the published URL** — you will be asked for it in the review
7. Tag your teammates in the share text so it appears on their profiles too

---

# TITLE

**Keeping Trucks Moving: A Microservices Platform for Real-Time Logistics Fleet Management**

*(Alternative titles, pick one:)*
- *From Spreadsheets to Services: Rebuilding Fleet Operations with Spring Boot Microservices*
- *Why One Broken Screen Should Not Stop an Entire Fleet: A Microservices Approach*

---

## Team Details

**Team [ number / name ]** — [ Department ], KL University

| Role | Name | ID |
|---|---|---|
| Team Lead | [ Name ] | [ ID ] |
| Member | [ Name ] | [ ID ] |
| Member | [ Name ] | [ ID ] |

**Guide / Faculty:** [ Name ]
**Course:** Service Oriented Architecture — Skill Development Project, Review 1

---

## 1. Problem Analysis & Requirement Specification

Logistics companies run fleets of trucks, vans and trailers across long distances. Three
groups depend on the same information at the same time, and each needs something different:

- **Dispatchers** need to know which vehicles are free *right now* before they can assign a trip
- **Drivers** need their assigned trip, and a way to report a breakdown from the road
- **Workshop staff** need the queue of faults, and the authority to pull a vehicle out of service

In most small and mid-size fleets this coordination still happens through phone calls,
WhatsApp groups and a shared spreadsheet. We identified four concrete failures in that approach:

1. **Double allocation** — the same vehicle gets assigned to two trips because two dispatchers
   read the sheet seconds apart
2. **Invisible downtime** — a vehicle goes into the workshop and nobody updates the sheet,
   so it keeps getting scheduled
3. **No accountability** — there is no record of who changed what, or when
4. **All-or-nothing failure** — when the one system that holds everything goes down, every
   part of the operation stops together

### Functional requirements we specified

| # | Requirement |
|---|---|
| FR1 | Register and authenticate users with distinct roles (admin, dispatcher, driver) |
| FR2 | Maintain a vehicle inventory with live status and last known location |
| FR3 | Schedule a trip only against a vehicle that is genuinely available |
| FR4 | Move a vehicle to *on trip* automatically when its trip starts, and release it on completion |
| FR5 | Allow any user to report a fault; allow staff to run the repair workflow |
| FR6 | Pull a vehicle out of service while it is being repaired, and return it afterwards |
| FR7 | Prevent invalid state changes (for example, dispatching a vehicle that is in the workshop) |

### Non-functional requirements

| # | Requirement | How we met it |
|---|---|---|
| NFR1 | One part failing must not stop the rest | Independent services, circuit breaker with fallback |
| NFR2 | Security must be enforced in one place | JWT validated at the API gateway only |
| NFR3 | Services must not know each other's addresses | Netflix Eureka service discovery |
| NFR4 | The system must survive a service moving or restarting | Registration and heartbeats, load-balanced by name |
| NFR5 | The API must be explorable without reading code | Aggregated OpenAPI / Swagger UI |
| NFR6 | No credentials in source control | All secrets read from environment variables |

---

## 2. Survey Conducted & Common Issue Addressed

> **[ FILL THIS IN — this section must be real. Replace every number below with your
> actual survey results. Do not publish invented figures. ]**

We surveyed **[ N ]** respondents — **[ N ]** drivers, **[ N ]** dispatchers/fleet supervisors
and **[ N ]** workshop staff — using a short questionnaire circulated over
**[ Google Forms / in person at ... ]** between **[ date ]** and **[ date ]**.

### What we asked

1. How do you currently find out which vehicles are free today?
2. Roughly how often does a vehicle get assigned when it should not have been?
3. When a vehicle breaks down, who do you inform, and how long until it is recorded?
4. What is the single most frustrating part of your day with the current system?
5. How much of your day goes on phone calls just to confirm status?
6. If one thing could be automatic tomorrow, what would you pick?

### What came back

| Finding | Respondents |
|---|---|
| Rely on a phone call or a WhatsApp message to confirm vehicle availability | [ n ] of [ N ] |
| Have seen the same vehicle assigned twice | [ n ] of [ N ] |
| Breakdowns are recorded hours after they happen, not immediately | [ n ] of [ N ] |
| Say status information is the biggest daily frustration | [ n ] of [ N ] |

### The common issue we addressed

> **Nobody trusts the vehicle status they are looking at.**

Every other complaint traces back to this one. Dispatchers phone drivers because the sheet
might be stale. Workshop delays go unrecorded because updating the sheet is somebody else's
job. Double allocation happens because two people read the same stale number.

So we made **vehicle status the single source of truth**, owned by exactly one service, and
changed **only** as a side effect of a real event — a trip starting, a repair beginning. No
human is asked to remember to update anything. That decision shaped the whole architecture.

---

## 3. Design Thinking & Innovation (DTI) Concepts

> **[ Attach your three DTI images here. Templates and instructions are in
> `REVIEW1-PREP.md` — build them from your real survey responses. ]**

### 3.1 Empathy Map — the dispatcher

Built from the survey responses of our dispatcher group.

| Quadrant | What we heard |
|---|---|
| **SAYS** | "Just tell me which trucks are free." · "I'll call the driver and check." |
| **THINKS** | "This sheet is probably out of date again." · "If I get this wrong, it's my name on it." |
| **DOES** | Phones drivers to confirm · Keeps a private notebook beside the shared sheet |
| **FEELS** | Anxious about double-booking · Frustrated at repeating the same calls daily |

**Pain:** no trustworthy live view of the fleet.
**Gain:** one screen that is always correct, without asking anyone.

### 3.2 Persona

> Built from **one** real survey respondent, as required.

**[ Name ], [ age ] — Dispatcher, [ company/location ]**

- **Background:** [ n ] years scheduling a fleet of about [ n ] vehicles
- **Goal:** assign every trip in the morning without a single vehicle conflict
- **Frustration:** *"I spend the first hour of every day on the phone finding out what I should already know."*
- **Tech comfort:** confident with a browser and a spreadsheet; not a technical user
- **Success looks like:** opening one page and trusting what it says

### 3.3 Customer Journey Map — dispatching one trip

| Stage | What they do now | Feeling | Our system |
|---|---|---|---|
| 1. Check availability | Open the sheet, then phone two drivers to be sure | 😟 Doubtful | `GET /api/vehicles?status=AVAILABLE` — live, no calls |
| 2. Assign the trip | Write it into the sheet, tell the driver | 😐 Uncertain | `POST /api/trips` — rejected if the vehicle is not free |
| 3. Trip begins | Hope somebody updates the sheet | 😣 Powerless | Vehicle flips to `ON_TRIP` automatically |
| 4. Breakdown reported | A phone call, remembered later | 😠 Stressed | Driver reports it; the vehicle leaves the pool at once |
| 5. Repair completed | Somebody eventually says so | 😕 Blind | Closing the ticket returns the vehicle to `AVAILABLE` |
| 6. Trip completed | Manually cross it off | 🙂 Relieved | Vehicle released automatically; trip is timestamped |

**Where we intervened:** stages 3, 4 and 5 — the three points where a human was expected to
remember to update a record, and regularly did not.

---

## 4. Architecture Diagram & Modules

> **[ Insert `architecture-diagram.png` here ]**

The system is built as **six independently deployable Spring Boot applications**.

| Module | Port | Responsibility |
|---|---|---|
| **Eureka Server** | 8761 | Service registry — every service registers and is discovered here |
| **API Gateway** | 8080 | The only public entry point: routing, JWT validation, rate limiting, CORS |
| **Auth Service** | 8081 | Registration, login, BCrypt hashing, JWT issuing |
| **Vehicle Service** | 8082 | Vehicle inventory, status lifecycle, location |
| **Trip Service** | 8083 | Trip scheduling and lifecycle |
| **Maintenance Service** | 8084 | Fault reporting and the repair workflow |

### How a request travels

1. The client sends a request to the gateway with a bearer token — nothing else is exposed
2. The gateway verifies the token signature; an invalid or missing token is rejected with `401`
3. The gateway attaches the verified identity as `X-User-Id`, `X-Username`, `X-User-Role`,
   **overwriting anything the client sent** — so identity cannot be forged
4. The gateway asks Eureka where the target service is and forwards the request
5. The service trusts those headers and applies role rules with `@PreAuthorize`

Every service follows the same internal layering:
**Controller → Service → Repository → Database**, with DTOs at the boundary.
JPA entities are never returned from a controller.

---

## 5. Microservices Listing, Data Handling & Service Integration

### Database per service

Each service owns a **separate MySQL schema**. No service reads another's tables.

| Service | Schema | Core table | Key columns |
|---|---|---|---|
| Auth | `fleet_auth` | `users` | user_id, username, password (BCrypt), role |
| Vehicle | `fleet_vehicle` | `vehicles` | vehicle_id, registration_number, type, status, location |
| Trip | `fleet_trip` | `trips` | trip_id, vehicle_id, driver_id, trip_status, origin, destination |
| Maintenance | `fleet_maintenance` | `maintenance_records` | maintenance_id, vehicle_id, issue, status |

A trip stores `vehicle_id` as a **plain column, not a foreign key** — the vehicle row lives in
a different database entirely. To read vehicle data, trip-service *calls* vehicle-service.
This is the deliberate trade-off of microservices: we give up joins to gain independence.

### Service integration with OpenFeign

```java
@FeignClient(name = "vehicle-service", fallbackFactory = VehicleClientFallbackFactory.class)
public interface VehicleClient {
    @GetMapping("/api/vehicles/{id}")
    VehicleResponse getVehicle(@PathVariable("id") Long id);

    @PatchMapping("/api/vehicles/{id}/status")
    VehicleResponse updateStatus(@PathVariable("id") Long id, @RequestBody UpdateVehicleStatusRequest req);
}
```

There is **no host name and no port** — only the service name, resolved through Eureka and
Spring Cloud LoadBalancer at call time.

### State machines keep the data consistent

Status is never set blindly; each transition is validated, and an illegal one returns `409 Conflict`.

- **Vehicle:** `AVAILABLE ⇄ ON_TRIP`, `AVAILABLE → UNDER_MAINTENANCE → AVAILABLE`, `OUT_OF_SERVICE`
- **Trip:** `SCHEDULED → IN_PROGRESS → COMPLETED`, cancellable from either active state
- **Maintenance:** `REPORTED → IN_REPAIR → RESOLVED`, cancellable from either open state

Because vehicle-service is the only owner of vehicle status, a trip cannot start on a vehicle
that the workshop has already pulled — the transition is simply refused.

---

## 6. Innovative Ideas

**1. Status changes as side effects, never as chores.**
No screen asks a human to "mark this vehicle as on trip". Starting a trip *is* what moves the
vehicle. This directly answers the core survey finding — stale data caused by forgotten updates.

**2. Identity is established once, at the edge.**
The gateway validates the JWT and injects verified identity headers, overwriting any the caller
supplied. Four services get role-based security without four copies of the security code — and
we tested that forged `X-User-*` headers are rejected with `401`.

**3. A fallback that refuses to lie.**
When vehicle-service is unreachable, the circuit breaker opens and our fallback returns a clear
error. It deliberately does **not** fake a successful write: pretending a vehicle is `ON_TRIP`
when the update never landed would leave two databases permanently disagreeing. Availability is
never bought with silent corruption.

**4. Documentation that cannot drift.**
Swagger UI is aggregated at the gateway, and each service's spec declares the gateway as its
server — so "Try it out" exercises the real security path, not a bypass. The docs are generated
from the running code, so they cannot go stale.

**5. Self-describing operations.**
The landing page polls each service's health through the gateway and shows live status, so the
health of the whole system is one glance rather than six terminal windows.

---

## What we learned

Splitting a system into services is the easy part. The real work is deciding **who owns which
data**, and accepting that a call across a service boundary can fail — which is why the circuit
breaker, the state machines and the error contract matter more than the split itself.

---

## Hashtags

```
#KLUniversity #KLEF #Y24SOA #SDPReview1 #Microservices #SpringBoot #SpringCloud
#JWT #APIGateway #Eureka #OpenFeign #MySQL #SOA #SoftwareArchitecture #DesignThinking
```

---

## Converting the diagram to an image

LinkedIn will not accept an `.svg`. To get a `.png`:

1. Open `docs/architecture-diagram.svg` in Chrome (drag the file into a tab)
2. Press `Win + Shift + S`, drag a box around the diagram, and it copies to the clipboard
3. Paste into Paint, then **File → Save as → PNG**

Or upload the SVG to any free "SVG to PNG" converter.

**Also screenshot for the article:**
- The Eureka dashboard at `localhost:8761` showing all five services registered
- The Swagger UI at `localhost:8080/swagger-ui.html`
- Your project landing page at `localhost:8080`
