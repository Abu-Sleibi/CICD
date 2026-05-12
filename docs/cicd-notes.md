# CI/CD Notes — Hotel Booking System

## Trigger Rule
Pipeline triggers on every push to the `main` branch.

## Image Versioning
Each image is tagged with the git commit SHA and `latest`.
Example: 430757098958.dkr.ecr.us-east-1.amazonaws.com/hotel-booking:<commit-sha>

## Rollback Steps
1. Find previous revision: aws ecs describe-services --cluster hotel-booking-cluster --services hotel-booking-service --query "services[0].taskDefinition"
2. Redeploy it: aws ecs update-service --cluster hotel-booking-cluster --service hotel-booking-service --task-definition hotel-booking-task:<previous-revision-number> --region us-east-1
3. Wait: aws ecs wait services-stable --cluster hotel-booking-cluster --services hotel-booking-service --region us-east-1

## IAM Permissions Required
ECR: GetAuthorizationToken, BatchCheckLayerAvailability, PutImage, InitiateLayerUpload, UploadLayerPart, CompleteLayerUpload
ECS: RegisterTaskDefinition, UpdateService, DescribeServices, DescribeTaskDefinition
IAM: PassRole
