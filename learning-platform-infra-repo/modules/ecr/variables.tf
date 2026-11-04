variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "repositories" {
  description = "ECR repositories for the application services"
  type        = set(string)
}