output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = module.vpc.private_subnet_ids
}

output "alb_security_group_id" {
  description = "ALB security group ID"
  value       = module.security_groups.alb_security_group_id
}

output "ecs_security_group_id" {
  description = "ECS security group ID"
  value       = module.security_groups.ecs_security_group_id
}

output "ecs_execution_role_arn" {
  description = "ECS task execution role ARN"
  value       = module.iam.ecs_execution_role_arn
}

output "ecs_execution_role_name" {
  description = "ECS task execution role name"
  value       = module.iam.ecs_execution_role_name
}

output "ecs_task_role_arn" {
  description = "ECS task role ARN"
  value       = module.iam.ecs_task_role_arn
}

output "ecs_task_role_name" {
  description = "ECS task role name"
  value       = module.iam.ecs_task_role_name
}

output "alb_id" {
  description = "Application Load Balancer ID"
  value       = module.alb.alb_id
}

output "alb_arn" {
  description = "Application Load Balancer ARN"
  value       = module.alb.alb_arn
}

output "alb_dns_name" {
  description = "Application Load Balancer DNS name"
  value       = module.alb.alb_dns_name
}

output "alb_zone_id" {
  description = "Application Load Balancer hosted zone ID"
  value       = module.alb.alb_zone_id
}

output "http_listener_arn" {
  description = "ALB HTTP listener ARN"
  value       = module.alb.http_listener_arn
}

output "target_group_arns" {
  description = "Target group ARNs by application"
  value       = module.alb.target_group_arns
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = module.rds.db_endpoint
}

output "rds_port" {
  description = "RDS PostgreSQL port"
  value       = module.rds.db_port
}

output "rds_db_name" {
  description = "Initial PostgreSQL database name"
  value       = module.rds.db_name
}

output "rds_security_group_id" {
  description = "RDS security group ID"
  value       = module.rds.rds_security_group_id
}
output "ecr_repository_urls" {
  description = "ECR repository URLs by service"
  value       = module.ecr.repository_urls
}

output "ecr_repository_names" {
  description = "ECR repository names by service"
  value       = module.ecr.repository_names
}
output "ecs_cluster_id" {
  description = "ECS cluster ID"
  value       = module.ecs_cluster.cluster_id
}

output "ecs_cluster_arn" {
  description = "ECS cluster ARN"
  value       = module.ecs_cluster.cluster_arn
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.ecs_cluster.cluster_name
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID"
  value       = module.cloudfront.cloudfront_distribution_id
}

output "cloudfront_domain_name" {
  description = "CloudFront default domain name"
  value       = module.cloudfront.cloudfront_domain_name
}