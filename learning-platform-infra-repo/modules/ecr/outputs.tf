output "repository_urls" {
  description = "ECR repository URLs by service"

  value = {
    for service, repository in aws_ecr_repository.this :
    service => repository.repository_url
  }
}

output "repository_arns" {
  description = "ECR repository ARNs by service"

  value = {
    for service, repository in aws_ecr_repository.this :
    service => repository.arn
  }
}

output "repository_names" {
  description = "ECR repository names by service"

  value = {
    for service, repository in aws_ecr_repository.this :
    service => repository.name
  }
}