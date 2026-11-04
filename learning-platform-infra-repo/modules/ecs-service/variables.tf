
variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "cluster_id" {
  description = "ECS cluster ID"
  type        = string
}

variable "subnet_ids" {
  description = "Private subnet IDs for ECS tasks"
  type        = list(string)
}

variable "ecs_security_group_id" {
  description = "Security group ID for ECS services"
  type        = string
}

variable "execution_role_arn" {
  description = "ECS task execution role ARN"
  type        = string
}

variable "task_role_arn" {
  description = "ECS task role ARN"
  type        = string
}

variable "target_group_arns" {
  description = "ALB target group ARNs for externally routed services"
  type        = map(string)
  default     = {}
}

variable "services" {
  description = "Configuration for the five ECS microservices"

  type = map(object({
    image            = string
    container_port   = number
    cpu              = number
    memory           = number
    desired_count    = number
    environment      = optional(map(string), {})
    secrets          = optional(map(string), {})
    target_group_key = optional(string)
  }))
}

variable "log_retention_days" {
  description = "CloudWatch log retention period"
  type        = number
  default     = 7
}

variable "assign_public_ip" {
  description = "Whether ECS tasks should receive public IP addresses"
  type        = bool
  default     = false
}
