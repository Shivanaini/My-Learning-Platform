variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  description = "Private subnet IDs for the RDS subnet group"
  type        = map(string)
}

variable "ecs_security_group_id" {
  description = "ECS security group allowed to connect to PostgreSQL"
  type        = string
}

variable "db_name" {
  description = "Initial PostgreSQL database"
  type        = string
  default     = "learning_auth"
}

variable "db_username" {
  description = "RDS master username"
  type        = string
  default     = "learningadmin"
}

/*variable "db_password" {
  description = "RDS master password"
  type        = string
  sensitive   = true
}*/

variable "instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "allocated_storage" {
  description = "RDS storage in GB"
  type        = number
  default     = 20
}