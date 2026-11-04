resource "aws_ecr_repository" "this" {
  for_each = var.repositories

  name = "${var.project_name}-${var.environment}-${each.value}"

  image_scanning_configuration {
    scan_on_push = true
  }

  image_tag_mutability = "MUTABLE"

  force_delete = false

  tags = {
    Name        = "${var.project_name}-${var.environment}-${each.value}"
    Environment = var.environment
    Project     = var.project_name
    Service     = each.value
  }
}