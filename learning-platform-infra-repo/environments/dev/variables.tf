variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "vpc_cidr" {
  type = string
}

variable "availability_zones" {
  type = list(string)
}

variable "public_subnet_cidrs" {
  type = list(string)
}

variable "private_subnet_cidrs" {
  type = list(string)
}

variable "applications" {
  description = "Application services and ports"

  type = map(object({
    port = number
  }))
}

variable "alb_applications" {
  description = "Applications exposed through the Application Load Balancer"

  type = map(object({
    port = number
  }))
}

variable "db_name" {
  description = "Initial RDS PostgreSQL database"
  type        = string
  default     = "learning_auth"
}

variable "db_username" {
  description = "RDS PostgreSQL master username"
  type        = string
  default     = "learningadmin"
}

/*variable "db_password" {
  description = "RDS PostgreSQL master password"
  type        = string
  sensitive   = true
}*/

variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "rds_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

variable "ecr_repositories" {
  description = "Application services that need ECR repositories"
  type        = set(string)

  default = [
    "discovery-server",
    "api-gateway",
    "auth-service",
    "lecture-service",
    "ui-service"
  ]
}

variable "database_names" {
  description = "Additional PostgreSQL databases"
  type        = set(string)

  default = [
    "learning_lecture"
  ]
}