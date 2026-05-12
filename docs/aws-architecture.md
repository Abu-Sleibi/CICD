# AWS Architecture — Hotel Booking System

## Mermaid Diagram

```mermaid
graph TB
    Internet(["🌐 Internet<br/>(Public)"])

    subgraph AWS_Region["AWS Region: us-east-1"]

        subgraph ALB_Layer["Public Subnets (alb-sg: TCP 80 → 0.0.0.0/0)"]
            ALB["Application Load Balancer<br/>hotel-booking-alb<br/>HTTP :80 → TG :8080<br/>Health: /actuator/health"]
        end

        subgraph ECS_Layer["Private Subnets (ecs-sg: TCP 8080 ← alb-sg only)"]
            ECS["ECS Fargate Task<br/>hotel-booking-task<br/>0.5 vCPU / 1 GB RAM<br/>Spring Boot :8080<br/>Profile: prod"]
        end

        subgraph RDS_Layer["Private Subnets (rds-sg: TCP 3306 ← ecs-sg only)"]
            RDS[("RDS MySQL 8.0<br/>hotel-booking-db<br/>db.t3.micro / 20 GB gp2<br/>DB: hotelbooking")]
        end

        ECR["🐳 Amazon ECR<br/>hotel-booking:latest<br/>(Multi-stage Docker image)"]
        SM["🔑 Secrets Manager<br/>hotel-booking/db-credentials<br/>hotel-booking/jwt-secret<br/>hotel-booking/mail-credentials"]
        CW["📊 CloudWatch Logs<br/>/ecs/hotel-booking<br/>awslogs driver → ecs/*"]
        IAM["🔐 IAM Role<br/>hotel-booking-ecs-exec-role<br/>AmazonECSTaskExecutionRolePolicy<br/>+ SecretsManagerReadWrite (scoped)"]

    end

    Internet -->|"HTTP :80"| ALB
    ALB -->|"HTTP :8080 (forwarded)"| ECS
    ECS -->|"MySQL :3306"| RDS

    ECR -.->|"pull image at task start"| ECS
    SM -.->|"DB_URL / DB_USERNAME<br/>DB_PASSWORD / JWT_SECRET<br/>MAIL creds (injected)"| ECS
    ECS -.->|"stdout → awslogs"| CW
    IAM -.->|"grants ECR pull + logs write<br/>+ Secrets read"| ECS
```

## Text-Based Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         AWS us-east-1                                   │
│                                                                         │
│  🌐 Internet                                                            │
│       │  HTTP :80                                                       │
│       ▼                                                                 │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  ALB  hotel-booking-alb  [alb-sg: 0.0.0.0/0 → :80]            │   │
│  │       Listener :80 → Target Group hotel-booking-tg :8080        │   │
│  │       Health check: GET /actuator/health                         │   │
│  └───────────────────────────┬─────────────────────────────────────┘   │
│                              │  HTTP :8080                              │
│                              ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  ECS Fargate  hotel-booking-service  [ecs-sg: alb-sg → :8080]  │   │
│  │  Task: hotel-booking-task (0.5 vCPU / 1 GB)                     │   │
│  │  Image: <acct>.dkr.ecr.us-east-1.amazonaws.com/hotel-booking    │   │
│  │  SPRING_PROFILES_ACTIVE=prod                                     │   │
│  │  DB_URL / DB_USERNAME / DB_PASSWORD / JWT_SECRET  ◄──┐          │   │
│  └───────────────────────────┬────────────────────────── │──────────┘   │
│                              │  MySQL :3306              │              │
│                              ▼                           │              │
│  ┌──────────────────────────────────────────┐           │              │
│  │  RDS MySQL 8.0  hotel-booking-db         │           │              │
│  │  [rds-sg: ecs-sg → :3306]                │   ┌───────┴──────────┐  │
│  │  db.t3.micro / 20 GB gp2 / Single-AZ     │   │  Secrets Manager │  │
│  │  DB: hotelbooking  user: admin            │   │  db-credentials  │  │
│  └──────────────────────────────────────────┘   │  jwt-secret      │  │
│                                                  │  mail-creds      │  │
│  ┌──────────────────────┐                        └──────────────────┘  │
│  │  ECR  hotel-booking  │   ◄─ docker push (CI/CD or manual)          │
│  │  image:latest        │   ──► pulled at ECS task launch              │
│  └──────────────────────┘                                              │
│                                                                         │
│  ┌──────────────────────────────────────────┐                          │
│  │  CloudWatch Logs  /ecs/hotel-booking     │                          │
│  │  stream prefix: ecs/                     │ ◄── awslogs driver       │
│  └──────────────────────────────────────────┘                          │
│                                                                         │
│  IAM: hotel-booking-ecs-exec-role                                       │
│       AmazonECSTaskExecutionRolePolicy + scoped SecretsManager read     │
└─────────────────────────────────────────────────────────────────────────┘
```

## Component Summary

| Component | AWS Service | Details |
|-----------|-------------|---------|
| Container registry | Amazon ECR | `hotel-booking` repo, scan-on-push |
| Compute | ECS Fargate | 0.5 vCPU, 1 GB RAM, 1 task |
| Load balancer | ALB | Internet-facing, HTTP :80 → :8080 |
| Database | RDS MySQL 8.0 | db.t3.micro, 20 GB gp2, Single-AZ |
| Secrets | Secrets Manager | DB creds, JWT secret, mail creds |
| Logs | CloudWatch Logs | `/ecs/hotel-booking` |
| Permissions | IAM Role | ECS task execution + scoped Secrets read |
| Network | VPC + Security Groups | 3 SGs with least-privilege ingress |
