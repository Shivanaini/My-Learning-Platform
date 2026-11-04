variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}


variable "database_secret_arn" {
  description = "ARN of the PostgreSQL credentials secret"
  type        = string
}

variable "jwt_secret_arn" {
  description = "ARN of the JWT secret"
  type        = string
}
