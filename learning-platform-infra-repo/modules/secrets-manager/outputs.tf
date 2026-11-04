
output "database_secret_arn" {
  description = "ARN of the PostgreSQL credentials secret"
  value       = aws_secretsmanager_secret.database.arn
}

output "database_secret_name" {
  description = "Name of the PostgreSQL credentials secret"
  value       = aws_secretsmanager_secret.database.name
}

output "jwt_secret_arn" {
  description = "ARN of the JWT secret"
  value       = aws_secretsmanager_secret.jwt.arn
}

output "jwt_secret_name" {
  description = "Name of the JWT secret"
  value       = aws_secretsmanager_secret.jwt.name
}
