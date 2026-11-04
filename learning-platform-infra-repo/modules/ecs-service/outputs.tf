
# ECS Service Names
output "ecs_service_names" {
  description = "Names of all ECS services"

  value = {
    for name, service in aws_ecs_service.this :
    name => service.name
  }
}

# ECS Service ARNs
output "ecs_service_arns" {
  description = "ARNs of all ECS services"

  value = {
    for name, service in aws_ecs_service.this :
    name => service.id
  }
}

# ECS Task Definition ARNs
output "task_definition_arns" {
  description = "ARNs of all ECS task definitions"

  value = {
    for name, task in aws_ecs_task_definition.this :
    name => task.arn
  }
}

# CloudWatch Log Group Names
output "cloudwatch_log_group_names" {
  description = "CloudWatch log group names for all services"

  value = {
    for name, log_group in aws_cloudwatch_log_group.this :
    name => log_group.name
  }
}
