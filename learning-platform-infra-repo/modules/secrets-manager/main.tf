
# PostgreSQL credentials secret
resource "aws_secretsmanager_secret" "database" {
  name        = "${var.project_name}/${var.environment}/database"
  description = "PostgreSQL credentials for Learning Platform"

  recovery_window_in_days = var.recovery_window_in_days

  tags = {
    Name        = "${var.project_name}-${var.environment}-database-secret"
    Project     = var.project_name
    Environment = var.environment
  }
}

# JWT secret
resource "aws_secretsmanager_secret" "jwt" {
  name        = "${var.project_name}/${var.environment}/jwt"
  description = "JWT signing secret for Learning Platform"

  recovery_window_in_days = var.recovery_window_in_days

  tags = {
    Name        = "${var.project_name}-${var.environment}-jwt-secret"
    Project     = var.project_name
    Environment = var.environment
  }
}
