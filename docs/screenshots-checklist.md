# Screenshots Checklist — AWS Deployment Submission
## Hotel Booking System | SWER313AB Spring 2026

For each screenshot: open the AWS Console in **us-east-1**, navigate to the service, and take a full-browser screenshot. Name each file as shown.

---

## 1. ECS Service Steady State
**File:** `screenshot-01-ecs-service-steady.png`

**Where:** AWS Console → ECS → Clusters → `hotel-booking-cluster` → Services → `hotel-booking-service`

**What to capture:**
- [ ] Service name: `hotel-booking-service`
- [ ] Status: **ACTIVE**
- [ ] Desired count: **1**
- [ ] Running count: **1**
- [ ] Pending count: **0**
- [ ] Deployment status: **PRIMARY** with rollout state **COMPLETED**
- [ ] Last deployment timestamp visible

**Also capture the Tasks tab:**
**File:** `screenshot-01b-ecs-task-running.png`
- [ ] 1 task in **RUNNING** state
- [ ] Task ID visible
- [ ] Last status: RUNNING
- [ ] Health status: HEALTHY

---

## 2. Target Group Healthy Status
**File:** `screenshot-02-target-group-healthy.png`

**Where:** AWS Console → EC2 → Load Balancers → Target Groups → `hotel-booking-tg` → Targets tab

**What to capture:**
- [ ] Target group name: `hotel-booking-tg`
- [ ] Protocol: HTTP, Port: 8080
- [ ] Health check path: `/actuator/health`
- [ ] At least 1 registered target showing:
  - IP address of the ECS task
  - Port: 8080
  - Health status: **healthy** (green)

---

## 3a. CloudWatch Logs — App Startup
**File:** `screenshot-03a-cloudwatch-startup.png`

**Where:** AWS Console → CloudWatch → Log groups → `/ecs/hotel-booking` → [latest log stream]

**What to capture:**
- [ ] Log group name: `/ecs/hotel-booking`
- [ ] Log stream name starting with `ecs/hotel-booking/...`
- [ ] Spring Boot startup banner visible:
  ```
  Started HotelProjectApplication in X.XXX seconds
  ```
- [ ] Timestamp of startup event

## 3b. CloudWatch Logs — Request Log
**File:** `screenshot-03b-cloudwatch-request.png`

**What to capture:**
- [ ] A log entry showing an HTTP request (e.g., health check or API call)
- [ ] Request timestamp visible

---

## 4. RDS Instance Running
**File:** `screenshot-04-rds-running.png`

**Where:** AWS Console → RDS → Databases → `hotel-booking-db`

**What to capture:**
- [ ] DB identifier: `hotel-booking-db`
- [ ] Status: **Available** (green)
- [ ] Engine: MySQL 8.0.x
- [ ] Class: db.t3.micro
- [ ] Multi-AZ: **No**
- [ ] Storage: 20 GiB gp2
- [ ] Endpoint: `hotel-booking-db.xxxxxxxxx.us-east-1.rds.amazonaws.com`

---

## 5. ECR Repository
**File:** `screenshot-05-ecr-repo.png`

**Where:** AWS Console → ECR → Repositories → `hotel-booking`

**What to capture:**
- [ ] Repository name: `hotel-booking`
- [ ] Image tag: `latest`
- [ ] Push date visible
- [ ] Image size (should be ~200–250 MB)
- [ ] Scan status (no critical vulnerabilities preferred)

---

## 6. Secrets Manager
**File:** `screenshot-06-secrets.png`

**Where:** AWS Console → Secrets Manager → Secrets

**What to capture:**
- [ ] `hotel-booking/db-credentials` — Created, in use
- [ ] `hotel-booking/jwt-secret` — Created, in use
- [ ] `hotel-booking/mail-credentials` — Created, in use

---

## 7. ALB Health Check — API Response
**File:** `screenshot-07-actuator-health.png`

**How to take:** Open a browser or use curl, navigate to:
```
http://<ALB-DNS-NAME>/actuator/health
```

**What to capture:**
- [ ] Browser address bar showing the ALB DNS URL
- [ ] Response body: `{"status":"UP"}`
- [ ] HTTP 200 status (check browser DevTools Network tab)

---

## 8. CRUD Test — Successful API Response
**File:** `screenshot-08-crud-test.png`

**Suggested test (using curl or Postman):**

```bash
ALB_DNS="<your-alb-dns>"

# List hotels (public endpoint)
curl -s "http://${ALB_DNS}/api/v1/hotels/" | python3 -m json.tool
```

Or register + login + create a hotel:
```bash
# Register admin (or use LoadDatabase seeded admin)
curl -s -X POST "http://${ALB_DNS}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hotel.com","password":"admin123"}'

# List hotels
curl -s "http://${ALB_DNS}/api/v1/hotels/"
```

**What to capture:**
- [ ] HTTP 200 response
- [ ] JSON body showing hotel(s) or empty list `{"content":[],"totalElements":0,...}`
- [ ] ALB DNS visible in the request URL

---

## Submission Checklist Summary

| # | Screenshot | Status |
|---|------------|--------|
| 01 | ECS Service — ACTIVE, desired=1, running=1 | ☐ |
| 01b | ECS Task — RUNNING, HEALTHY | ☐ |
| 02 | Target Group — target healthy (green) | ☐ |
| 03a | CloudWatch — Spring Boot startup log | ☐ |
| 03b | CloudWatch — HTTP request log | ☐ |
| 04 | RDS — Available, db.t3.micro, MySQL 8.0 | ☐ |
| 05 | ECR — image:latest pushed | ☐ |
| 06 | Secrets Manager — 3 secrets created | ☐ |
| 07 | Browser — /actuator/health → {"status":"UP"} | ☐ |
| 08 | API CRUD — 200 JSON response via ALB | ☐ |
