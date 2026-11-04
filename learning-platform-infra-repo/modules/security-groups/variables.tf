variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "applications" {
  type = map(object({
    port = number
  }))
}