
variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "recovery_window_in_days" {
  description = "Number of days before a deleted secret is permanently removed"
  type        = number
  default     = 7
}
