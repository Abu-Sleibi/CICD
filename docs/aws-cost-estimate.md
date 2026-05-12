# AWS Cost Estimate — Hotel Booking System
## Region: us-east-1 | Monthly Estimate

**Assumptions:**
- 1 Fargate task running 24/7, 0.25 vCPU / 0.5 GB RAM
- 1 ALB running 24/7, ~1,000 requests/day (~30,000/month)
- RDS db.t3.micro, Single-AZ, 20 GB gp2, 7-day backup retention
- CloudWatch Logs: ~1 GB ingestion/month
- Secrets Manager: 1 secret (3 items stored as one JSON per secret)

---

## Cost Breakdown

### 1. AWS Fargate (ECS)

| Resource | Spec | Hours/month | Unit price | Cost |
|----------|------|-------------|------------|------|
| vCPU | 0.25 vCPU × 24h × 30d = 180 vCPU-hours | 180 | $0.04048/vCPU-hr | **$7.29** |
| Memory | 0.5 GB × 24h × 30d = 360 GB-hours | 360 | $0.004445/GB-hr | **$1.60** |
| **Subtotal** | | | | **$8.89/mo** |

> Note: The task definition uses 0.5 vCPU / 1 GB (per spec). At those sizes:
> 0.5 vCPU × 720 hr × $0.04048 = **$14.57** + 1 GB × 720 hr × $0.004445 = **$3.20** → **$17.77/mo**

### 2. Application Load Balancer (ALB)

| Resource | Spec | Rate | Cost |
|----------|------|------|------|
| ALB-hours | 720 hr/month | $0.008/hr | **$5.76** |
| LCU (Load Balancer Capacity Units) | ~1,000 req/day = negligible | ~$0.00 | **~$0.01** |
| **Subtotal** | | | **$5.77/mo** |

### 3. RDS MySQL (db.t3.micro, Single-AZ)

| Resource | Spec | Rate | Cost |
|----------|------|------|------|
| Instance hours | db.t3.micro × 720 hr | $0.017/hr | **$12.24** |
| Storage | 20 GB gp2 | $0.115/GB-mo | **$2.30** |
| Backup storage | 7-day retention, ~20 GB | First 100% free (≤ DB size) | **$0.00** |
| **Subtotal** | | | **$14.54/mo** |

### 4. Amazon ECR

| Resource | Spec | Rate | Cost |
|----------|------|------|------|
| Storage | ~200 MB image | $0.10/GB-mo | **$0.02** |
| Data transfer | Internal (ECS pull) | Free within region | **$0.00** |
| **Subtotal** | | | **$0.02/mo** |

### 5. CloudWatch Logs

| Resource | Spec | Rate | Cost |
|----------|------|------|------|
| Log ingestion | 1 GB/month | $0.50/GB | **$0.50** |
| Log storage | ~1 GB (30-day retention) | $0.03/GB-mo | **$0.03** |
| **Subtotal** | | | **$0.53/mo** |

### 6. AWS Secrets Manager

| Resource | Spec | Rate | Cost |
|----------|------|------|------|
| Secrets stored | 3 secrets | $0.40/secret/mo | **$1.20** |
| API calls | ~10,000/mo (task restarts) | $0.05/10K calls | **$0.05** |
| **Subtotal** | | | **$1.25/mo** |

### 7. Data Transfer

| Resource | Spec | Cost |
|----------|------|------|
| Outbound (responses) | ~1,000 req/day × avg 5 KB = 150 MB/mo | First 1 GB free → **$0.00** |

---

## Total Monthly Estimate

| Service | Monthly Cost |
|---------|-------------|
| ECS Fargate (0.5 vCPU / 1 GB) | $17.77 |
| Application Load Balancer | $5.77 |
| RDS MySQL db.t3.micro | $14.54 |
| Amazon ECR | $0.02 |
| CloudWatch Logs | $0.53 |
| Secrets Manager | $1.25 |
| Data Transfer | $0.00 |
| **TOTAL** | **~$39.88/mo** |

> **AWS Free Tier note:** If this is a new AWS account (within 12 months), RDS db.t3.micro (750 hrs/mo) and some EC2/ALB usage may be free. Estimated free-tier savings: ~$12–15/mo.

---

## Top 3 Cost Drivers

1. **ECS Fargate — $17.77/mo (44.6%)**: The compute layer is the largest expense. Fargate's per-second pricing is convenient but costlier than reserved EC2 for steady-state workloads.

2. **RDS MySQL — $14.54/mo (36.5%)**: The managed database (instance hours + gp2 storage) is the second-largest expense. Single-AZ is already the cheapest option.

3. **Application Load Balancer — $5.77/mo (14.5%)**: The hourly ALB charge applies even with zero traffic. At low request volumes the fixed hour charge dominates the LCU cost.

---

## 2 Cost Reduction Ideas

### 1. Switch Fargate to Reserved EC2 + ECS (or use Fargate Spot)

- **Fargate Spot** can reduce Fargate compute costs by **up to 70%** (~$12.44/mo savings) for interruption-tolerant workloads like this dev/staging environment.
  - Add `--capacity-provider-strategy capacityProvider=FARGATE_SPOT,weight=1` to the ECS service.

- Alternatively, a single `t3.micro` EC2 Reserved Instance (1-year, no upfront) for ECS is ~$7.60/mo — cheaper than Fargate for constant 24/7 loads.

### 2. Replace ALB with a scheduled on/off schedule or use RDS Serverless v2

- **For dev/university project**: Schedule the ECS service to scale to 0 tasks during off-hours (evenings/weekends) using EventBridge + Lambda. At 8h/day × 5d/wk utilization the ALB is still $5.76, but Fargate costs drop by ~70%.

- **RDS Aurora Serverless v2** starts at 0.5 ACU when idle ($0.12/ACU-hr) — for very low traffic this can be cheaper than a continuously running db.t3.micro, and scales up automatically if needed.

---

## Pricing References
- Fargate: https://aws.amazon.com/fargate/pricing/
- RDS: https://aws.amazon.com/rds/mysql/pricing/
- ALB: https://aws.amazon.com/elasticloadbalancing/pricing/
- CloudWatch: https://aws.amazon.com/cloudwatch/pricing/
- Secrets Manager: https://aws.amazon.com/secrets-manager/pricing/
