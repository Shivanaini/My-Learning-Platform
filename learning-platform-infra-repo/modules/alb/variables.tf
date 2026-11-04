variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "public_subnet_ids" {
  type = map(string)
}

variable "alb_security_group_id" {
  type = string
}

variable "alb_applications" {
  description = "Applications exposed through the ALB"

  type = map(object({
    port = number
  }))
}