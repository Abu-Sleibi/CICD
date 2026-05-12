#!/usr/bin/env bash
# deploy.sh — Deploy Hotel Booking System to AWS ECS Fargate
# Usage: cd aws && bash deploy.sh
# Prereqs: aws CLI configured, Docker running
set -euo pipefail

# ─────────────────────────────────────────────────────────────────────────────
# Configuration  (override any of these with env vars before running)
# ─────────────────────────────────────────────────────────────────────────────
APP_NAME="${APP_NAME:-hotel-booking}"
AWS_REGION="${AWS_REGION:-us-east-1}"
DB_NAME="hotelbooking"
DB_INSTANCE_CLASS="${DB_INSTANCE_CLASS:-db.t3.micro}"
CONTAINER_PORT=8080
TASK_CPU=512       # 0.5 vCPU
TASK_MEMORY=1024   # 1 GB
DESIRED_COUNT=1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# ─────────────────────────────────────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────────────────────────────────────
log()  { printf '\n\033[1;34m[%s] %s\033[0m\n' "$(date '+%H:%M:%S')" "$*"; }
step() { printf '\033[0;32m  ▸ %s\033[0m\n' "$*"; }
die()  { printf '\033[1;31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }

# ─────────────────────────────────────────────────────────────────────────────
# 0. Prerequisites
# ─────────────────────────────────────────────────────────────────────────────
log "Checking prerequisites…"
command -v aws    >/dev/null 2>&1 || die "AWS CLI not installed. See https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
command -v docker >/dev/null 2>&1 || die "Docker not installed. See https://docs.docker.com/get-docker/"
aws sts get-caller-identity >/dev/null 2>&1 || die "AWS credentials not configured. Run: aws configure"

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
step "AWS Account : $AWS_ACCOUNT_ID"
step "Region      : $AWS_REGION"
step "Project dir : $PROJECT_DIR"

# ─────────────────────────────────────────────────────────────────────────────
# 1. Collect secrets (env vars take priority, then interactive prompts)
# ─────────────────────────────────────────────────────────────────────────────
log "Collecting sensitive values (will be stored in AWS Secrets Manager)…"
if [[ -z "${DB_PASSWORD:-}" ]]; then
  read -r -s -p "  RDS master password (min 8 chars):  " DB_PASSWORD; echo
fi
[[ ${#DB_PASSWORD} -ge 8 ]]  || die "DB password must be at least 8 characters."

if [[ -z "${JWT_SECRET_VAL:-}" ]]; then
  read -r -s -p "  JWT secret (min 32 chars):          " JWT_SECRET_VAL; echo
fi
[[ ${#JWT_SECRET_VAL} -ge 32 ]] || die "JWT secret must be at least 32 characters."

if [[ -z "${MAIL_PASSWORD:-}" ]]; then
  read -r -s -p "  Gmail app-password:                 " MAIL_PASSWORD; echo
fi

# ─────────────────────────────────────────────────────────────────────────────
# 2. ECR — build and push Docker image
# ─────────────────────────────────────────────────────────────────────────────
log "Setting up ECR repository…"
ECR_BASE="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
ECR_REPO_URI="${ECR_BASE}/${APP_NAME}"

aws ecr describe-repositories \
  --repository-names "$APP_NAME" \
  --region "$AWS_REGION" >/dev/null 2>&1 \
|| aws ecr create-repository \
     --repository-name "$APP_NAME" \
     --image-scanning-configuration scanOnPush=true \
     --region "$AWS_REGION" >/dev/null
step "Repository : $ECR_REPO_URI"

step "Building Docker image (this may take a few minutes on first run)…"
docker build -t "${APP_NAME}:latest" "$PROJECT_DIR"

step "Authenticating Docker to ECR…"
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_BASE"

docker tag "${APP_NAME}:latest" "${ECR_REPO_URI}:latest"
docker push "${ECR_REPO_URI}:latest"
IMAGE_URI="${ECR_REPO_URI}:latest"
step "Image pushed : $IMAGE_URI"

# ─────────────────────────────────────────────────────────────────────────────
# 3. Networking — default VPC, subnets, security groups
# ─────────────────────────────────────────────────────────────────────────────
log "Configuring networking…"

VPC_ID=$(aws ec2 describe-vpcs \
  --filters Name=isDefault,Values=true \
  --query 'Vpcs[0].VpcId' --output text --region "$AWS_REGION")
[[ "$VPC_ID" != "None" && -n "$VPC_ID" ]] \
  || die "No default VPC found. Create one: aws ec2 create-default-vpc --region $AWS_REGION"
step "VPC : $VPC_ID"

# Collect subnet IDs into an array
SUBNET_IDS_RAW=$(aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=$VPC_ID" \
  --query 'Subnets[*].SubnetId' --output text --region "$AWS_REGION")
read -r -a SUBNET_ARRAY <<< "$SUBNET_IDS_RAW"
[[ ${#SUBNET_ARRAY[@]} -ge 2 ]] \
  || die "Need at least 2 subnets in the default VPC (found ${#SUBNET_ARRAY[@]})."
SUBNET_CSV=$(IFS=,; echo "${SUBNET_ARRAY[*]}")
step "Subnets : $SUBNET_CSV"

# Helper — get existing SG by name or create a new one
get_or_create_sg() {
  local name="$1" desc="$2"
  local id
  id=$(aws ec2 describe-security-groups \
    --filters "Name=group-name,Values=${name}" "Name=vpc-id,Values=${VPC_ID}" \
    --query 'SecurityGroups[0].GroupId' --output text --region "$AWS_REGION" 2>/dev/null || true)
  if [[ -z "$id" || "$id" == "None" ]]; then
    id=$(aws ec2 create-security-group \
      --group-name "$name" --description "$desc" --vpc-id "$VPC_ID" \
      --query 'GroupId' --output text --region "$AWS_REGION")
  fi
  echo "$id"
}

ALB_SG_ID=$(get_or_create_sg "${APP_NAME}-alb-sg" "ALB SG — ${APP_NAME}")
ECS_SG_ID=$(get_or_create_sg "${APP_NAME}-ecs-sg" "ECS tasks SG — ${APP_NAME}")
RDS_SG_ID=$(get_or_create_sg "${APP_NAME}-rds-sg" "RDS SG — ${APP_NAME}")
step "Security groups  ALB=$ALB_SG_ID  ECS=$ECS_SG_ID  RDS=$RDS_SG_ID"

# Ingress rules — failures mean the rule already exists, so suppress and continue
aws ec2 authorize-security-group-ingress --group-id "$ALB_SG_ID" \
  --protocol tcp --port 80 --cidr 0.0.0.0/0 --region "$AWS_REGION" 2>/dev/null || true
aws ec2 authorize-security-group-ingress --group-id "$ECS_SG_ID" \
  --protocol tcp --port "$CONTAINER_PORT" \
  --source-group "$ALB_SG_ID" --region "$AWS_REGION" 2>/dev/null || true
aws ec2 authorize-security-group-ingress --group-id "$RDS_SG_ID" \
  --protocol tcp --port 3306 \
  --source-group "$ECS_SG_ID" --region "$AWS_REGION" 2>/dev/null || true

# ─────────────────────────────────────────────────────────────────────────────
# 4. AWS Secrets Manager
# ─────────────────────────────────────────────────────────────────────────────
log "Storing secrets in AWS Secrets Manager…"

# Create or update a secret; returns its ARN
store_secret() {
  local name="$1" desc="$2" value="$3"
  local arn
  arn=$(aws secretsmanager describe-secret \
    --secret-id "$name" --query 'ARN' --output text \
    --region "$AWS_REGION" 2>/dev/null || true)
  if [[ -z "$arn" || "$arn" == "None" ]]; then
    arn=$(aws secretsmanager create-secret \
      --name "$name" --description "$desc" --secret-string "$value" \
      --query 'ARN' --output text --region "$AWS_REGION")
  else
    aws secretsmanager put-secret-value \
      --secret-id "$name" --secret-string "$value" \
      --region "$AWS_REGION" >/dev/null
  fi
  echo "$arn"
}

DB_URL_VALUE="jdbc:mysql://${RDS_ENDPOINT}:3306/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
DB_SECRET_ARN=$(store_secret \
  "${APP_NAME}/db-credentials" \
  "RDS credentials for ${APP_NAME}" \
  "{\"username\":\"admin\",\"password\":\"${DB_PASSWORD}\",\"host\":\"${RDS_ENDPOINT}\",\"dbname\":\"${DB_NAME}\"}")

JWT_SECRET_ARN=$(store_secret \
  "${APP_NAME}/jwt-secret" \
  "JWT signing secret for ${APP_NAME}" \
  "${JWT_SECRET_VAL}")

MAIL_SECRET_ARN=$(store_secret \
  "${APP_NAME}/mail-credentials" \
  "Gmail credentials for ${APP_NAME}" \
  "{\"username\":\"gsleibi2005@gmail.com\",\"password\":\"${MAIL_PASSWORD}\"}")

step "DB secret  : $DB_SECRET_ARN"
step "JWT secret : $JWT_SECRET_ARN"
step "Mail secret: $MAIL_SECRET_ARN"

# ─────────────────────────────────────────────────────────────────────────────
# 5. RDS MySQL 8.0
# ─────────────────────────────────────────────────────────────────────────────
log "Provisioning RDS MySQL (${DB_INSTANCE_CLASS})…"

aws rds create-db-subnet-group \
  --db-subnet-group-name "${APP_NAME}-subnet-group" \
  --db-subnet-group-description "Subnet group for ${APP_NAME}" \
  --subnet-ids "${SUBNET_ARRAY[@]}" \
  --region "$AWS_REGION" 2>/dev/null || true

DB_STATUS=$(aws rds describe-db-instances \
  --db-instance-identifier "${APP_NAME}-mysql" \
  --query 'DBInstances[0].DBInstanceStatus' \
  --output text --region "$AWS_REGION" 2>/dev/null || echo "notfound")

if [[ "$DB_STATUS" == "notfound" ]]; then
  aws rds create-db-instance \
    --db-instance-identifier "${APP_NAME}-mysql" \
    --db-instance-class "$DB_INSTANCE_CLASS" \
    --engine mysql \
    --engine-version "8.0" \
    --master-username "admin" \
    --master-user-password "$DB_PASSWORD" \
    --db-name "$DB_NAME" \
    --allocated-storage 20 \
    --storage-type gp2 \
    --vpc-security-group-ids "$RDS_SG_ID" \
    --db-subnet-group-name "${APP_NAME}-subnet-group" \
    --backup-retention-period 7 \
    --no-publicly-accessible \
    --region "$AWS_REGION" >/dev/null
  step "RDS instance created. Waiting for it to become available (~5 min)…"
else
  step "RDS instance already exists (status: ${DB_STATUS}). Waiting for available…"
fi

aws rds wait db-instance-available \
  --db-instance-identifier "${APP_NAME}-mysql" \
  --region "$AWS_REGION"

RDS_ENDPOINT=$(aws rds describe-db-instances \
  --db-instance-identifier "${APP_NAME}-mysql" \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text --region "$AWS_REGION")
step "RDS endpoint : $RDS_ENDPOINT"

# ─────────────────────────────────────────────────────────────────────────────
# 6. IAM — ECS task execution role
# ─────────────────────────────────────────────────────────────────────────────
log "Configuring IAM task execution role…"

TRUST_POLICY='{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Service": "ecs-tasks.amazonaws.com" },
    "Action": "sts:AssumeRole"
  }]
}'

EXEC_ROLE_ARN=$(aws iam get-role \
  --role-name "${APP_NAME}-ecs-exec-role" \
  --query 'Role.Arn' --output text 2>/dev/null \
|| aws iam create-role \
     --role-name "${APP_NAME}-ecs-exec-role" \
     --assume-role-policy-document "$TRUST_POLICY" \
     --query 'Role.Arn' --output text)

# Managed policy: pull ECR images + write CloudWatch Logs
aws iam attach-role-policy \
  --role-name "${APP_NAME}-ecs-exec-role" \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy \
  2>/dev/null || true

# Inline policy: read the three secrets we created
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
step "Execution role : $EXEC_ROLE_ARN"

# ─────────────────────────────────────────────────────────────────────────────
# 7. CloudWatch Log Group
# ─────────────────────────────────────────────────────────────────────────────
aws logs create-log-group \
  --log-group-name "/ecs/${APP_NAME}" \
  --region "$AWS_REGION" 2>/dev/null || true

# ─────────────────────────────────────────────────────────────────────────────
# 8. ECS Cluster
# ─────────────────────────────────────────────────────────────────────────────
log "Creating ECS cluster…"
aws ecs create-cluster \
  --cluster-name "${APP_NAME}-cluster" \
  --capacity-providers FARGATE \
  --region "$AWS_REGION" >/dev/null 2>&1 || true
step "Cluster : ${APP_NAME}-cluster"

# ─────────────────────────────────────────────────────────────────────────────
# 9. ECS Task Definition
# ─────────────────────────────────────────────────────────────────────────────
log "Registering ECS task definition…"

TASK_DEF_JSON=$(cat <<EOF
{
  "family": "${APP_NAME}",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "${TASK_CPU}",
  "memory": "${TASK_MEMORY}",
  "executionRoleArn": "${EXEC_ROLE_ARN}",
  "containerDefinitions": [
    {
      "name": "${APP_NAME}",
      "image": "${IMAGE_URI}",
      "essential": true,
      "portMappings": [
        { "containerPort": ${CONTAINER_PORT}, "protocol": "tcp" }
      ],
      "environment": [
        { "name": "SPRING_PROFILES_ACTIVE", "value": "prod" },
        { "name": "SERVER_PORT",            "value": "8080" },
        { "name": "DB_URL",                 "value": "${DB_URL_VALUE}" }
      ],
      "secrets": [
        { "name": "DB_USERNAME",         "valueFrom": "${DB_SECRET_ARN}:username::" },
        { "name": "DB_PASSWORD",         "valueFrom": "${DB_SECRET_ARN}:password::" },
        { "name": "JWT_SECRET",          "valueFrom": "${JWT_SECRET_ARN}" },
        { "name": "SPRING_MAIL_USERNAME","valueFrom": "${MAIL_SECRET_ARN}:username::" },
        { "name": "SPRING_MAIL_PASSWORD","valueFrom": "${MAIL_SECRET_ARN}:password::" }
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
  --cli-input-json "$TASK_DEF_JSON" \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text --region "$AWS_REGION")
step "Task definition : $TASK_DEF_ARN"

# ─────────────────────────────────────────────────────────────────────────────
# 10. Application Load Balancer
# ─────────────────────────────────────────────────────────────────────────────
log "Setting up Application Load Balancer…"

ALB_ARN=$(aws elbv2 describe-load-balancers \
  --names "${APP_NAME}-alb" \
  --query 'LoadBalancers[0].LoadBalancerArn' \
  --output text --region "$AWS_REGION" 2>/dev/null || true)

if [[ -z "$ALB_ARN" || "$ALB_ARN" == "None" ]]; then
  ALB_ARN=$(aws elbv2 create-load-balancer \
    --name "${APP_NAME}-alb" \
    --subnets "${SUBNET_ARRAY[@]}" \
    --security-groups "$ALB_SG_ID" \
    --scheme internet-facing \
    --type application \
    --ip-address-type ipv4 \
    --query 'LoadBalancers[0].LoadBalancerArn' \
    --output text --region "$AWS_REGION")
fi

ALB_DNS=$(aws elbv2 describe-load-balancers \
  --load-balancer-arns "$ALB_ARN" \
  --query 'LoadBalancers[0].DNSName' \
  --output text --region "$AWS_REGION")
step "ALB DNS : $ALB_DNS"

TG_ARN=$(aws elbv2 describe-target-groups \
  --names "${APP_NAME}-tg" \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text --region "$AWS_REGION" 2>/dev/null || true)

if [[ -z "$TG_ARN" || "$TG_ARN" == "None" ]]; then
  TG_ARN=$(aws elbv2 create-target-group \
    --name "${APP_NAME}-tg" \
    --protocol HTTP \
    --port "$CONTAINER_PORT" \
    --vpc-id "$VPC_ID" \
    --target-type ip \
    --health-check-path "/actuator/health" \
    --health-check-interval-seconds 30 \
    --healthy-threshold-count 2 \
    --unhealthy-threshold-count 3 \
    --query 'TargetGroups[0].TargetGroupArn' \
    --output text --region "$AWS_REGION")
fi

# Listener (idempotent — ignore error if already exists)
aws elbv2 create-listener \
  --load-balancer-arn "$ALB_ARN" \
  --protocol HTTP --port 80 \
  --default-actions "Type=forward,TargetGroupArn=${TG_ARN}" \
  --region "$AWS_REGION" >/dev/null 2>&1 || true
step "Target group : $TG_ARN"

# ─────────────────────────────────────────────────────────────────────────────
# 11. ECS Service
# ─────────────────────────────────────────────────────────────────────────────
log "Creating / updating ECS service…"

SERVICE_STATUS=$(aws ecs describe-services \
  --cluster "${APP_NAME}-cluster" \
  --services "${APP_NAME}-service" \
  --query 'services[0].status' \
  --output text --region "$AWS_REGION" 2>/dev/null || echo "notfound")

if [[ "$SERVICE_STATUS" == "ACTIVE" ]]; then
  aws ecs update-service \
    --cluster "${APP_NAME}-cluster" \
    --service "${APP_NAME}-service" \
    --task-definition "$TASK_DEF_ARN" \
    --desired-count "$DESIRED_COUNT" \
    --region "$AWS_REGION" >/dev/null
  step "Existing service updated with new task definition."
else
  aws ecs create-service \
    --cluster "${APP_NAME}-cluster" \
    --service-name "${APP_NAME}-service" \
    --task-definition "$TASK_DEF_ARN" \
    --launch-type FARGATE \
    --desired-count "$DESIRED_COUNT" \
    --network-configuration \
      "awsvpcConfiguration={subnets=[${SUBNET_CSV}],securityGroups=[${ECS_SG_ID}],assignPublicIp=ENABLED}" \
    --load-balancers \
      "targetGroupArn=${TG_ARN},containerName=${APP_NAME},containerPort=${CONTAINER_PORT}" \
    --health-check-grace-period-seconds 120 \
    --region "$AWS_REGION" >/dev/null
  step "ECS service created."
fi

log "Waiting for service to stabilize (3–5 min)…"
aws ecs wait services-stable \
  --cluster "${APP_NAME}-cluster" \
  --services "${APP_NAME}-service" \
  --region "$AWS_REGION"

# ─────────────────────────────────────────────────────────────────────────────
# Done
# ─────────────────────────────────────────────────────────────────────────────
printf '\n\033[1;32m'
printf '═══════════════════════════════════════════════════════════\n'
printf '  Deployment complete!\n\n'
printf '\033[0m'
printf '  App URL    : \033[1mhttp://%s\033[0m\n'              "$ALB_DNS"
printf '  Swagger UI : \033[1mhttp://%s/swagger-ui.html\033[0m\n' "$ALB_DNS"
printf '  API Docs   : \033[1mhttp://%s/api-docs\033[0m\n'    "$ALB_DNS"
printf '\n'
printf '  ECS Cluster   : %s-cluster\n'  "$APP_NAME"
printf '  RDS Endpoint  : %s\n'          "$RDS_ENDPOINT"
printf '  ECR Image     : %s\n'          "$IMAGE_URI"
printf '  CloudWatch    : /ecs/%s\n'     "$APP_NAME"
printf '\033[1;32m'
printf '═══════════════════════════════════════════════════════════\n'
printf '\033[0m\n'
