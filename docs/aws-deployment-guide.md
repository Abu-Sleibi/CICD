# AWS Deployment Guide — Hotel Booking System
## SWER313AB — Service-Oriented Architecture (Spring 2026)

---

## Prerequisites

```bash
# Verify tools
aws --version          # aws-cli/2.x
docker --version       # Docker 24+
aws configure list     # confirm credentials + us-east-1 region
```

---

## Step 2a — ECR: Create Repository, Build & Push Image

```bash
# Variables (set once, used throughout)
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION="us-east-1"
APP_NAME="hotel-booking"
ECR_BASE="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
ECR_REPO_URI="${ECR_BASE}/${APP_NAME}"

# Create ECR repository
aws ecr create-repository \
  --repository-name "${APP_NAME}" \
  --image-scanning-configuration scanOnPush=true \
  --region "${AWS_REGION}"
# Output: {"repository": {"repositoryUri": "<acct>.dkr.ecr.us-east-1.amazonaws.com/hotel-booking", ...}}

# Authenticate Docker to ECR
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR_BASE}"
# Output: Login Succeeded

# Build image (from project root — multi-stage Dockerfile compiles JAR inside Docker)
cd /path/to/project-step-1-littlestjeff1gf
docker build -t "${APP_NAME}:latest" .

# Tag and push
docker tag "${APP_NAME}:latest" "${ECR_REPO_URI}:latest"
docker push "${ECR_REPO_URI}:latest"
# Output: latest: digest: sha256:... size: ...
```

---

## Step 2b — Networking: Default VPC + 3 Security Groups

```bash
# Get default VPC
VPC_ID=$(aws ec2 describe-vpcs \
  --filters Name=isDefault,Values=true \
  --query 'Vpcs[0].VpcId' --output text --region "${AWS_REGION}")
echo "VPC: ${VPC_ID}"

# Get all subnet IDs in that VPC
SUBNET_IDS=$(aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=${VPC_ID}" \
  --query 'Subnets[*].SubnetId' --output text --region "${AWS_REGION}")
echo "Subnets: ${SUBNET_IDS}"

# Create security groups
ALB_SG_ID=$(aws ec2 create-security-group \
  --group-name "${APP_NAME}-alb-sg" \
  --description "ALB SG — ${APP_NAME}" \
  --vpc-id "${VPC_ID}" \
  --query 'GroupId' --output text --region "${AWS_REGION}")

ECS_SG_ID=$(aws ec2 create-security-group \
  --group-name "${APP_NAME}-ecs-sg" \
  --description "ECS tasks SG — ${APP_NAME}" \
  --vpc-id "${VPC_ID}" \
  --query 'GroupId' --output text --region "${AWS_REGION}")

RDS_SG_ID=$(aws ec2 create-security-group \
  --group-name "${APP_NAME}-rds-sg" \
  --description "RDS SG — ${APP_NAME}" \
  --vpc-id "${VPC_ID}" \
  --query 'GroupId' --output text --region "${AWS_REGION}")

echo "ALB_SG=${ALB_SG_ID}  ECS_SG=${ECS_SG_ID}  RDS_SG=${RDS_SG_ID}"

# Ingress rules (least-privilege)
# ALB: public HTTP
aws ec2 authorize-security-group-ingress \
  --group-id "${ALB_SG_ID}" \
  --protocol tcp --port 80 --cidr 0.0.0.0/0 \
  --region "${AWS_REGION}"

# ECS: only from ALB
aws ec2 authorize-security-group-ingress \
  --group-id "${ECS_SG_ID}" \
  --protocol tcp --port 8080 \
  --source-group "${ALB_SG_ID}" \
  --region "${AWS_REGION}"

# RDS: only from ECS tasks
aws ec2 authorize-security-group-ingress \
  --group-id "${RDS_SG_ID}" \
  --protocol tcp --port 3306 \
  --source-group "${ECS_SG_ID}" \
  --region "${AWS_REGION}"
```

---

## Step 2c — RDS MySQL 8.0

```bash
DB_PASSWORD="<your-secure-password-min-8-chars>"

# Subnet group for RDS (uses all subnets from default VPC)
aws rds create-db-subnet-group \
  --db-subnet-group-name "${APP_NAME}-subnet-group" \
  --db-subnet-group-description "Subnet group for ${APP_NAME}" \
  --subnet-ids ${SUBNET_IDS} \
  --region "${AWS_REGION}"

# Create RDS instance (~5 minutes to become available)
aws rds create-db-instance \
  --db-instance-identifier "${APP_NAME}-db" \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version "8.0" \
  --master-username "admin" \
  --master-user-password "${DB_PASSWORD}" \
  --db-name "hotelbooking" \
  --allocated-storage 20 \
  --storage-type gp2 \
  --vpc-security-group-ids "${RDS_SG_ID}" \
  --db-subnet-group-name "${APP_NAME}-subnet-group" \
  --backup-retention-period 7 \
  --no-publicly-accessible \
  --no-multi-az \
  --region "${AWS_REGION}"
# Output: {"DBInstance": {"DBInstanceStatus": "creating", ...}}

# Wait for available (~5 min)
aws rds wait db-instance-available \
  --db-instance-identifier "${APP_NAME}-db" \
  --region "${AWS_REGION}"

# Get endpoint
RDS_ENDPOINT=$(aws rds describe-db-instances \
  --db-instance-identifier "${APP_NAME}-db" \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text --region "${AWS_REGION}")
echo "RDS Endpoint: ${RDS_ENDPOINT}"
```

---

## Step 2d — Secrets Manager

```bash
JWT_SECRET_VAL="<your-jwt-secret-min-32-chars>"
MAIL_PASSWORD="<your-gmail-app-password>"
MAIL_USERNAME="your-email@gmail.com"

# DB credentials secret (JSON format as per spec)
DB_SECRET_ARN=$(aws secretsmanager create-secret \
  --name "${APP_NAME}/db-credentials" \
  --description "RDS credentials for ${APP_NAME}" \
  --secret-string "{\"username\":\"admin\",\"password\":\"${DB_PASSWORD}\",\"host\":\"${RDS_ENDPOINT}\",\"dbname\":\"hotelbooking\"}" \
  --query 'ARN' --output text --region "${AWS_REGION}")
echo "DB Secret ARN: ${DB_SECRET_ARN}"

# JWT secret
JWT_SECRET_ARN=$(aws secretsmanager create-secret \
  --name "${APP_NAME}/jwt-secret" \
  --description "JWT signing secret for ${APP_NAME}" \
  --secret-string "${JWT_SECRET_VAL}" \
  --query 'ARN' --output text --region "${AWS_REGION}")

# Mail credentials
MAIL_SECRET_ARN=$(aws secretsmanager create-secret \
  --name "${APP_NAME}/mail-credentials" \
  --description "Gmail credentials for ${APP_NAME}" \
  --secret-string "{\"username\":\"${MAIL_USERNAME}\",\"password\":\"${MAIL_PASSWORD}\"}" \
  --query 'ARN' --output text --region "${AWS_REGION}")
```

---

## Step 2e — IAM Role for ECS Task Execution

```bash
# Trust policy allowing ECS tasks to assume this role
TRUST_POLICY='{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Service": "ecs-tasks.amazonaws.com" },
    "Action": "sts:AssumeRole"
  }]
}'

EXEC_ROLE_ARN=$(aws iam create-role \
  --role-name "${APP_NAME}-ecs-exec-role" \
  --assume-role-policy-document "${TRUST_POLICY}" \
  --query 'Role.Arn' --output text)

# Attach managed policy (ECR pull + CloudWatch Logs write)
aws iam attach-role-policy \
  --role-name "${APP_NAME}-ecs-exec-role" \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Inline policy scoped to our 3 secrets
aws iam put-role-policy \
  --role-name "${APP_NAME}-ecs-exec-role" \
  --policy-name "${APP_NAME}-secrets-read" \
  --policy-document "{
    \"Version\": \"2012-10-17\",
    \"Statement\": [{
      \"Effect\": \"Allow\",
      \"Action\": [\"secretsmanager:GetSecretValue\"],
      \"Resource\": [
        \"${DB_SECRET_ARN}\",
        \"${JWT_SECRET_ARN}\",
        \"${MAIL_SECRET_ARN}\"
      ]
    }]
  }"
echo "Exec Role ARN: ${EXEC_ROLE_ARN}"
```

---

## Step 2f — CloudWatch Log Group

```bash
aws logs create-log-group \
  --log-group-name "/ecs/${APP_NAME}" \
  --region "${AWS_REGION}"
# Output: (no output on success)
```

---

## Step 2g — ECS Cluster

```bash
aws ecs create-cluster \
  --cluster-name "${APP_NAME}-cluster" \
  --capacity-providers FARGATE \
  --region "${AWS_REGION}"
# Output: {"cluster": {"clusterName": "hotel-booking-cluster", "status": "ACTIVE", ...}}
```

---

## Step 2h — ECS Task Definition

```bash
# Full JDBC URL (plain env var, not a secret — only creds are secrets)
DB_URL_VALUE="jdbc:mysql://${RDS_ENDPOINT}:3306/hotelbooking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
IMAGE_URI="${ECR_REPO_URI}:latest"

TASK_DEF_JSON=$(cat <<EOF
{
  "family": "${APP_NAME}-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "${EXEC_ROLE_ARN}",
  "containerDefinitions": [
    {
      "name": "${APP_NAME}",
      "image": "${IMAGE_URI}",
      "essential": true,
      "portMappings": [{ "containerPort": 8080, "protocol": "tcp" }],
      "environment": [
        { "name": "SPRING_PROFILES_ACTIVE", "value": "prod" },
        { "name": "SERVER_PORT",            "value": "8080" },
        { "name": "DB_URL",                 "value": "${DB_URL_VALUE}" }
      ],
      "secrets": [
        { "name": "DB_USERNAME",          "valueFrom": "${DB_SECRET_ARN}:username::" },
        { "name": "DB_PASSWORD",          "valueFrom": "${DB_SECRET_ARN}:password::" },
        { "name": "JWT_SECRET",           "valueFrom": "${JWT_SECRET_ARN}" },
        { "name": "SPRING_MAIL_USERNAME", "valueFrom": "${MAIL_SECRET_ARN}:username::" },
        { "name": "SPRING_MAIL_PASSWORD", "valueFrom": "${MAIL_SECRET_ARN}:password::" }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group":         "/ecs/${APP_NAME}",
          "awslogs-region":        "${AWS_REGION}",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command":     ["CMD-SHELL", "curl -sf http://localhost:8080/actuator/health || exit 1"],
        "interval":    30,
        "timeout":     5,
        "retries":     3,
        "startPeriod": 60
      }
    }
  ]
}
EOF
)

TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json "${TASK_DEF_JSON}" \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text --region "${AWS_REGION}")
echo "Task Def ARN: ${TASK_DEF_ARN}"
```

---

## Step 2i — Application Load Balancer

```bash
# Convert space-separated subnet IDs to array for ALB
read -r -a SUBNET_ARRAY <<< "${SUBNET_IDS}"

# Create ALB
ALB_ARN=$(aws elbv2 create-load-balancer \
  --name "${APP_NAME}-alb" \
  --subnets "${SUBNET_ARRAY[@]}" \
  --security-groups "${ALB_SG_ID}" \
  --scheme internet-facing \
  --type application \
  --ip-address-type ipv4 \
  --query 'LoadBalancers[0].LoadBalancerArn' \
  --output text --region "${AWS_REGION}")

ALB_DNS=$(aws elbv2 describe-load-balancers \
  --load-balancer-arns "${ALB_ARN}" \
  --query 'LoadBalancers[0].DNSName' \
  --output text --region "${AWS_REGION}")
echo "ALB DNS: ${ALB_DNS}"

# Create Target Group
TG_ARN=$(aws elbv2 create-target-group \
  --name "${APP_NAME}-tg" \
  --protocol HTTP \
  --port 8080 \
  --vpc-id "${VPC_ID}" \
  --target-type ip \
  --health-check-path "/actuator/health" \
  --health-check-interval-seconds 30 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3 \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text --region "${AWS_REGION}")

# Add listener: port 80 → forward to target group
aws elbv2 create-listener \
  --load-balancer-arn "${ALB_ARN}" \
  --protocol HTTP --port 80 \
  --default-actions "Type=forward,TargetGroupArn=${TG_ARN}" \
  --region "${AWS_REGION}"
echo "Target Group ARN: ${TG_ARN}"
```

---

## Step 2j — ECS Service

```bash
SUBNET_CSV=$(IFS=,; echo "${SUBNET_ARRAY[*]}")

aws ecs create-service \
  --cluster "${APP_NAME}-cluster" \
  --service-name "${APP_NAME}-service" \
  --task-definition "${TASK_DEF_ARN}" \
  --launch-type FARGATE \
  --desired-count 1 \
  --network-configuration \
    "awsvpcConfiguration={subnets=[${SUBNET_CSV}],securityGroups=[${ECS_SG_ID}],assignPublicIp=ENABLED}" \
  --load-balancers \
    "targetGroupArn=${TG_ARN},containerName=${APP_NAME},containerPort=8080" \
  --health-check-grace-period-seconds 120 \
  --region "${AWS_REGION}"
# Output: {"service": {"serviceName": "hotel-booking-service", "status": "ACTIVE", ...}}

# Wait for steady state (~3–5 min)
aws ecs wait services-stable \
  --cluster "${APP_NAME}-cluster" \
  --services "${APP_NAME}-service" \
  --region "${AWS_REGION}"
echo "Service is stable. App URL: http://${ALB_DNS}"
```

---

## Step 3 — Verification Commands

```bash
# 1. Health check
curl -s "http://${ALB_DNS}/actuator/health"
# Expected: {"status":"UP"}

# 2. ECS service status
aws ecs describe-services \
  --cluster "${APP_NAME}-cluster" \
  --services "${APP_NAME}-service" \
  --query 'services[0].{desired:desiredCount,running:runningCount,status:status}' \
  --output table --region "${AWS_REGION}"
# Expected: desired=1, running=1, status=ACTIVE

# 3. Target group health
aws elbv2 describe-target-health \
  --target-group-arn "${TG_ARN}" \
  --query 'TargetHealthDescriptions[*].{id:Target.Id,port:Target.Port,health:TargetHealth.State}' \
  --output table --region "${AWS_REGION}"
# Expected: health=healthy

# 4. CloudWatch logs (last 20 lines)
LOG_STREAM=$(aws logs describe-log-streams \
  --log-group-name "/ecs/${APP_NAME}" \
  --order-by LastEventTime --descending \
  --query 'logStreams[0].logStreamName' --output text --region "${AWS_REGION}")

aws logs get-log-events \
  --log-group-name "/ecs/${APP_NAME}" \
  --log-stream-name "${LOG_STREAM}" \
  --limit 20 \
  --query 'events[*].message' --output text --region "${AWS_REGION}"

# 5. CRUD test
BASE="http://${ALB_DNS}"

# Register a user and login
TOKEN=$(curl -s -X POST "${BASE}/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Test1234!","firstName":"Test","lastName":"User"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Or login if already registered
TOKEN=$(curl -s -X POST "${BASE}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234!"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# List hotels (public endpoint — no auth needed)
curl -s "${BASE}/api/v1/hotels/" | head -200
```

---

## One-Command Automated Deploy

After setting your passwords as environment variables:

```bash
export DB_PASSWORD="<your-db-password>"
export JWT_SECRET_VAL="<your-jwt-secret-32-chars>"
export MAIL_PASSWORD="<your-gmail-app-password>"

cd aws && bash deploy.sh
```
