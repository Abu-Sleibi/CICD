#!/usr/bin/env bash
# teardown.sh — Delete all AWS resources created by deploy.sh
# Usage: cd aws && bash teardown.sh
set -euo pipefail

APP_NAME="${APP_NAME:-hotel-booking}"
AWS_REGION="${AWS_REGION:-us-east-1}"

log()  { printf '\n\033[1;33m[%s] %s\033[0m\n' "$(date '+%H:%M:%S')" "$*"; }
step() { printf '\033[0;33m  ▸ %s\033[0m\n' "$*"; }

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
log "Tearing down ${APP_NAME} in ${AWS_REGION} (account ${AWS_ACCOUNT_ID})…"
read -r -p "  Type 'yes' to confirm: " CONFIRM
[[ "$CONFIRM" == "yes" ]] || { echo "Aborted."; exit 0; }

# ECS service (scale to 0 first)
log "Removing ECS service…"
aws ecs update-service \
  --cluster "${APP_NAME}-cluster" --service "${APP_NAME}-service" \
  --desired-count 0 --region "$AWS_REGION" 2>/dev/null || true
aws ecs delete-service \
  --cluster "${APP_NAME}-cluster" --service "${APP_NAME}-service" \
  --force --region "$AWS_REGION" 2>/dev/null || true
step "ECS service deleted."

# ECS cluster
log "Deleting ECS cluster…"
aws ecs delete-cluster \
  --cluster "${APP_NAME}-cluster" \
  --region "$AWS_REGION" 2>/dev/null || true
step "ECS cluster deleted."

# ALB listener, target group, load balancer
log "Deleting ALB resources…"
ALB_ARN=$(aws elbv2 describe-load-balancers \
  --names "${APP_NAME}-alb" --query 'LoadBalancers[0].LoadBalancerArn' \
  --output text --region "$AWS_REGION" 2>/dev/null || true)
if [[ -n "$ALB_ARN" && "$ALB_ARN" != "None" ]]; then
  LISTENER_ARNS=$(aws elbv2 describe-listeners \
    --load-balancer-arn "$ALB_ARN" \
    --query 'Listeners[*].ListenerArn' --output text --region "$AWS_REGION" 2>/dev/null || true)
  for L in $LISTENER_ARNS; do
    aws elbv2 delete-listener --listener-arn "$L" --region "$AWS_REGION" 2>/dev/null || true
  done
  aws elbv2 delete-load-balancer --load-balancer-arn "$ALB_ARN" --region "$AWS_REGION" 2>/dev/null || true
  step "ALB deleted."
fi
TG_ARN=$(aws elbv2 describe-target-groups \
  --names "${APP_NAME}-tg" --query 'TargetGroups[0].TargetGroupArn' \
  --output text --region "$AWS_REGION" 2>/dev/null || true)
if [[ -n "$TG_ARN" && "$TG_ARN" != "None" ]]; then
  aws elbv2 delete-target-group --target-group-arn "$TG_ARN" --region "$AWS_REGION" 2>/dev/null || true
  step "Target group deleted."
fi

# RDS instance
log "Deleting RDS instance (skipping final snapshot)…"
aws rds delete-db-instance \
  --db-instance-identifier "${APP_NAME}-mysql" \
  --skip-final-snapshot \
  --region "$AWS_REGION" 2>/dev/null || true
step "Waiting for RDS deletion…"
aws rds wait db-instance-deleted \
  --db-instance-identifier "${APP_NAME}-mysql" \
  --region "$AWS_REGION" 2>/dev/null || true
aws rds delete-db-subnet-group \
  --db-subnet-group-name "${APP_NAME}-subnet-group" \
  --region "$AWS_REGION" 2>/dev/null || true
step "RDS instance deleted."

# Secrets Manager
log "Deleting Secrets Manager entries…"
for SECRET in "${APP_NAME}/db-credentials" "${APP_NAME}/jwt-secret" "${APP_NAME}/mail-credentials"; do
  aws secretsmanager delete-secret \
    --secret-id "$SECRET" \
    --force-delete-without-recovery \
    --region "$AWS_REGION" 2>/dev/null || true
done
step "Secrets deleted."

# IAM role
log "Deleting IAM role…"
aws iam delete-role-policy \
  --role-name "${APP_NAME}-ecs-exec-role" \
  --policy-name "${APP_NAME}-secrets-read" 2>/dev/null || true
aws iam detach-role-policy \
  --role-name "${APP_NAME}-ecs-exec-role" \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy 2>/dev/null || true
aws iam delete-role \
  --role-name "${APP_NAME}-ecs-exec-role" 2>/dev/null || true
step "IAM role deleted."

# Security groups
log "Deleting security groups…"
VPC_ID=$(aws ec2 describe-vpcs \
  --filters Name=isDefault,Values=true \
  --query 'Vpcs[0].VpcId' --output text --region "$AWS_REGION" 2>/dev/null || true)
for SG_NAME in "${APP_NAME}-rds-sg" "${APP_NAME}-ecs-sg" "${APP_NAME}-alb-sg"; do
  SG_ID=$(aws ec2 describe-security-groups \
    --filters "Name=group-name,Values=${SG_NAME}" "Name=vpc-id,Values=${VPC_ID}" \
    --query 'SecurityGroups[0].GroupId' --output text --region "$AWS_REGION" 2>/dev/null || true)
  if [[ -n "$SG_ID" && "$SG_ID" != "None" ]]; then
    aws ec2 delete-security-group --group-id "$SG_ID" --region "$AWS_REGION" 2>/dev/null || true
    step "Deleted SG $SG_NAME ($SG_ID)"
  fi
done

# ECR repository
log "Deleting ECR repository (all images)…"
aws ecr delete-repository \
  --repository-name "$APP_NAME" \
  --force \
  --region "$AWS_REGION" 2>/dev/null || true
step "ECR repository deleted."

# CloudWatch log group
log "Deleting CloudWatch log group…"
aws logs delete-log-group \
  --log-group-name "/ecs/${APP_NAME}" \
  --region "$AWS_REGION" 2>/dev/null || true
step "Log group deleted."

printf '\n\033[1;33m  Teardown complete. All %s resources removed.\033[0m\n\n' "$APP_NAME"
