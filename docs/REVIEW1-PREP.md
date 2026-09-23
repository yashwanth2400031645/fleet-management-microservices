# Project Review 1 — Preparation Pack

Three members · 5 minutes each · assessed **individually**

---

## ⚠️ Before anything else — three gaps only you can close

| # | Gap | Who | Deadline |
|---|---|---|---|
| 1 | **Survey not conducted.** Empathy map, persona and journey map must come from real responses. | All three, tonight | Before the article |
| 2 | **MOOC progress** — 50% minimum in *each* course, checked at the review | Each member separately | Before your slot |
| 3 | **LinkedIn article** — team lead publishes, all members named | Team lead | Before your slot |

Do not invent survey numbers. A professor asking "how many people did you survey?" and
getting a hesitant answer costs more marks than a small honest sample.

### The 6-question survey — send tonight

Make a Google Form, send it to drivers / delivery staff / anyone who schedules vehicles
(auto drivers, college transport staff, a local logistics office, family contacts). **10–15
responses is enough.**

1. How do you currently find out which vehicles are free today?
2. How often does a vehicle get assigned when it should not have been? *(Never / Rarely / Monthly / Weekly)*
3. When a vehicle breaks down, who do you inform, and how long until it is recorded?
4. What is the single most frustrating part of your day with the current system?
5. Roughly how much of your day goes on phone calls just confirming status?
6. If one thing could become automatic tomorrow, what would you choose?

Then: **Empathy map** from all responses · **Persona** from *one* respondent ·
**Journey map** from the stages in `LINKEDIN-ARTICLE.md` section 3.3.

---

# The 30-second answer all three of you must know

> "We built a logistics fleet management system as **six independent microservices**. Each
> service does one job and owns its own database. They find each other through **Eureka**, all
> traffic enters through one **API Gateway** that validates a **JWT**, and services talk to each
> other by name using **OpenFeign**."

If you are asked *"what problem does it solve?"*:

> "Nobody trusts the vehicle status they are looking at. We made status change automatically as
> a side effect of real events, instead of relying on someone remembering to update a sheet."

---

# MEMBER 1 — Problem, DTI & Service Discovery
### Covers rubric: Problem analysis · DTI concepts · **Microservice Identification and Service Discovery (10)**

## Your 5 minutes

**0:00 – 1:00 — The problem**
> "Fleet operations run on spreadsheets and phone calls. We surveyed [N] people. The common issue
> was that nobody trusts the vehicle status they see — which causes double allocation, invisible
> downtime and no accountability."

**1:00 – 2:00 — DTI**
Show the empathy map, persona and journey map.
> "Our empathy map showed dispatchers *saying* 'just tell me what's free' while *thinking* 'this
> sheet is probably wrong'. The journey map showed the failure is always at the same three points —
> trip start, breakdown, repair done — the moments a human was expected to update a record."

**2:00 – 4:00 — Why these six services**
> "We split by **business capability**, not by technical layer. Identity is one concern, the fleet
> is another, journeys another, repairs another. Each one changes for a different reason, so each
> is its own service with its own database."

Name them: Eureka (registry), Gateway (entry), Auth, Vehicle, Trip, Maintenance.

**4:00 – 5:00 — Show Eureka**
Open **http://localhost:8761**.
> "All five services registered themselves at startup. We never typed an address anywhere in the
> code — services are referred to by name, like `lb://vehicle-service`, and Eureka resolves it."

## Questions your professor will ask

**Q: Why did you choose these particular microservices?**
> By business capability — each has its own reason to change. Auth changes when security policy
> changes, Vehicle when fleet rules change. If we split by layer instead — a "controller service",
> a "database service" — every feature would touch every service, which defeats the purpose.

**Q: Why does each service need its own database? Isn't one simpler?**
> Simpler to build, but then they are not independent. With a shared database, vehicle-service
> cannot change its schema without risking trip-service. Separate schemas mean each team can
> evolve its own data.

**Q: Then how does trip-service get vehicle data?**
> It calls vehicle-service over HTTP using OpenFeign. We gave up SQL joins to gain independence —
> that is the central trade-off of microservices.

**Q: What exactly is service discovery? Why not just use port numbers?**
> Hardcoded ports break the moment a service moves, restarts elsewhere, or is scaled to several
> copies. Services register with Eureka at startup and send a heartbeat every 30 seconds. Callers
> ask for a name and get a live address.

**Q: What happens if a service goes down?**
> It stops sending heartbeats and Eureka removes it. Callers stop being routed to it. Our
> trip-service also has a circuit breaker, so calls fail fast with a clear message instead of hanging.

**Q: What is `lb://` in your configuration?**
> Load balancer. If we ran three copies of vehicle-service, Spring Cloud LoadBalancer would pick
> one per request automatically.

**Q: Could this run on separate machines?**
> Yes — nothing in the code assumes localhost. Only the Eureka address is configured, through an
> environment variable.

---

# MEMBER 2 — Security & JWT Authentication
### Covers rubric: **JWT Authentication (10)**

## Your 5 minutes

**0:00 – 1:00 — The idea**
> "JWT is like a movie ticket. You show ID once at the counter — that's login — and get a signed
> ticket. Every door after that just checks the ticket. The server stores no session."

**1:00 – 2:30 — How we implemented it**
- Passwords stored as **BCrypt hashes**, never plain text
- Login returns a **signed HS256 token**, valid 1 hour
- Token carries **username, user id, role**
- **Stateless** — nothing stored server-side
- Three roles: **ADMIN, DISPATCHER, DRIVER**, enforced by `@PreAuthorize`
- The signing key lives in an **environment variable**, not in the code

**2:30 – 4:30 — Live demo** (Swagger, `localhost:8080/swagger-ui.html`)
1. Auth Service → `POST /api/auth/login` → Execute → show the token
2. Click **Authorize**, paste it, close
3. Vehicle Service → `GET /api/vehicles` → **200**
4. **The important one:** open `localhost:8080/api/vehicles` in a plain browser tab → **401**
> "Same request. The only difference is the token."

**4:30 – 5:00 — Roles**
> "A DRIVER token cannot create a vehicle — the service returns 403. Authentication is *who you
> are*; authorization is *what you may do*. We do both."

## Questions your professor will ask

**Q: What is inside a JWT?**
> Three parts separated by dots: header, payload, signature. The payload holds username, user id
> and role. The signature is generated with our secret key.

**Q: Is the payload encrypted?**
> No — it is Base64 encoded and anyone can read it. JWT provides **integrity, not secrecy**. That
> is why we never put a password in it. To keep it private on the wire you use HTTPS.

**Q: So a user could edit the token and make themselves ADMIN?**
> They can edit it, but the signature will no longer match, and the gateway rejects it with 401.
> We tested exactly this case with a forged token.

**Q: Why hash passwords? Why BCrypt specifically?**
> So that even with database access an attacker cannot read passwords. BCrypt is deliberately slow
> and salts each password, which makes brute forcing and rainbow tables impractical.

**Q: What if a token is stolen?**
> It expires in one hour, which limits the window. In production we would add HTTPS and refresh
> tokens with revocation — we know that is the next step.

**Q: Why not use sessions?**
> A session lives in one server's memory. With microservices any instance may receive the request,
> so we would need shared session storage. A self-contained signed token avoids that entirely.

**Q: Where is your secret key stored?**
> In an environment variable, `JWT_SECRET`. There is no fallback value in the configuration files,
> so the application refuses to start without it — and the key cannot leak through source control.

**Q: Do all four services validate the token?**
> No — only the gateway. It validates once and passes the verified identity inward as headers.
> That is exactly why the services must not be exposed directly.

---

# MEMBER 3 — API Gateway & Service Integration
### Covers rubric: **API Gateway Configuration (10)** · inter-service communication

## Your 5 minutes

**0:00 – 1:00 — Why a gateway**
> "The gateway is the security guard at the single entrance. Four services run inside, but only
> port 8080 is open. Without it, each of the four would need its own JWT checking, its own rate
> limiting, its own CORS — four copies of the same code."

**1:00 – 2:00 — What it does**
- **Routes by path** — `/api/vehicles/**` → vehicle-service, `/api/trips/**` → trip-service
- **Validates the JWT** once, then injects `X-User-Id` / `X-Username` / `X-User-Role`,
  **deleting anything the client sent** — so identity cannot be spoofed
- **Rate limiting** — 120 requests/minute, then `429` with a `Retry-After` header
- **CORS** — an explicit allow-list of origins, never a wildcard
- **Aggregated Swagger** — all four APIs behind one URL

**2:00 – 4:00 — The star demo** (Swagger)
1. Trip Service → `POST /api/trips` with a vehicle id → **201 SCHEDULED**
2. `PATCH /api/trips/{id}/start` → **IN_PROGRESS**
3. Vehicle Service → `GET /api/vehicles/{id}` → status is now **ON_TRIP**

> "Trip-service just changed a record that lives in a different service, with a different database.
> It found it by name through Eureka. Nobody typed a port number."

**4:00 – 5:00 — Resilience**
> "If vehicle-service goes down, the circuit breaker opens after repeated failures and calls fail
> fast into a fallback with a clear message. The fallback deliberately does not fake a successful
> write — pretending a vehicle is ON_TRIP when the update never landed would leave two databases
> permanently inconsistent."

## Questions your professor will ask

**Q: What does an API gateway actually do?**
> Single entry point. Routing, cross-cutting security, rate limiting, CORS, and documentation —
> in one place instead of repeated in every service.

**Q: How does the gateway know where vehicle-service is?**
> It asks Eureka. The route says `lb://vehicle-service` — a name, not an address.

**Q: If the gateway fails, everything fails. Isn't that a single point of failure?**
> Yes, and that is the accepted trade-off. In production you run several gateway instances behind
> a load balancer. The benefit — one place to enforce security — outweighs it.

**Q: Explain how OpenFeign works.**
> We declare a Java interface with the target service's name and endpoints. Feign generates the
> HTTP client at runtime, Eureka resolves the name, and LoadBalancer picks an instance. We write
> an interface, not HTTP code.

**Q: What is a circuit breaker, and why do you need one?**
> If a service keeps failing, every new call still waits for a timeout, and threads pile up until
> the caller dies too — a cascading failure. The breaker "opens" after a failure threshold and
> fails fast for a cooling period. Ours opens at 50% failures over 10 calls, for 10 seconds.

**Q: Why rate limiting?**
> To stop one client flooding the services, and to blunt brute-force attempts on the login
> endpoint. We allow 120 requests per minute and return 429 beyond that.

**Q: Why not allow all origins in CORS?**
> Because these endpoints carry bearer tokens. A wildcard would let any website a logged-in user
> visits call our API on their behalf.

**Q: How do the inner services know who the user is, if they never see the token?**
> From the `X-User-*` headers the gateway injects after validating. Each service turns those into
> a Spring Security principal and applies `@PreAuthorize`. It works *because* the gateway
> overwrites any client-supplied copies.

---

# Questions any of you could be asked

**Q: What would you improve with more time?**
> Distributed transactions. Right now a remote call and a local commit are not atomic — if the
> commit fails after the remote call succeeded, the two services disagree. The proper fix is the
> saga or outbox pattern. We would also add automated tests and containerise with Docker.

**Q: Why microservices for a project this size? Isn't a monolith simpler?**
> For this size, honestly yes — a monolith would be less work. We chose microservices because the
> requirement was independent failure and independent evolution, and because it is what the
> subject teaches. We can state the trade-off rather than pretend there is none.

**Q: How do you test it?**
> Manually through Swagger and curl, covering the happy paths and the failure paths — no token,
> forged token, spoofed headers, duplicate registration, illegal state transitions. Automated
> tests are the honest next gap.

**Q: What was the hardest part?**
> Keeping two databases consistent across a service boundary. That is where the state machines and
> the fallback design came from.

**Q: Who did what?**
> Answer honestly and specifically about your own contribution. Every student is graded
> individually — a confident, accurate answer about your section is worth more than a vague claim
> about all of it.

---

# Final checklist — tick before you walk in

**Technical**
- [ ] MySQL running
- [ ] All six services started in order: eureka → auth → vehicle → trip → maintenance → gateway
- [ ] `localhost:8761` shows **5** registered services
- [ ] `localhost:8080` landing page loads
- [ ] `localhost:8080/swagger-ui.html` loads, and you have logged in **once** to check
- [ ] A fresh token copied and ready in a text file (they expire in 1 hour)

**Documents**
- [ ] LinkedIn article published by the team lead, all members named — URL ready
- [ ] Empathy map, persona, journey map — from real survey data
- [ ] Survey responses available to show
- [ ] Architecture diagram exported as PNG
- [ ] MOOC progress ≥ 50% for **each** member

**Practice**
- [ ] Each member has run their own demo start to finish, once, on the actual screen
- [ ] Each member can answer their section's questions without reading notes

---

## If something breaks during the demo

**A service will not start** → check MySQL is running, and that you opened STS fresh
(environment variables are only read at startup).

**401 in Swagger** → your token expired. Log in again, click Authorize, paste the new one.
Say it out loud: *"the token expired — that is the one-hour expiry working."*

**A service disappeared from Eureka** → restart it; registration takes about 30 seconds.

**Anything else** → describe what should happen and why. Explaining a failure correctly shows
more understanding than a demo that happens to work.
