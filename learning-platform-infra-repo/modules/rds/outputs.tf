output "db_instance_id" {
  description = "RDS instance ID"
  value       = aws_db_instance.this.id
}

output "db_instance_arn" {
  description = "RDS instance ARN"
  value       = aws_db_instance.this.arn
}

output "db_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = aws_db_instance.this.address
}

output "db_port" {
  description = "RDS PostgreSQL port"
  value       = aws_db_instance.this.port
}

output "db_name" {
  description = "Initial PostgreSQL database name"
  value       = aws_db_instance.this.db_name
}

output "db_username" {
  description = "RDS master username"
  value       = aws_db_instance.this.username
}

output "rds_security_group_id" {
  description = "RDS security group ID"
  value       = aws_security_group.rds.id
}

output "db_master_user_secret_arn" {
  description = "ARN of the RDS-managed master credentials secret"
  value       = aws_db_instance.this.master_user_secret[0].secret_arn
}