output "alb_id" {
  description = "Application Load Balancer ID"
  value       = aws_lb.this.id
}

output "alb_arn" {
  description = "Application Load Balancer ARN"
  value       = aws_lb.this.arn
}

output "alb_dns_name" {
  description = "Application Load Balancer DNS name"
  value       = aws_lb.this.dns_name
}

output "alb_zone_id" {
  description = "Application Load Balancer hosted zone ID"
  value       = aws_lb.this.zone_id
}

output "http_listener_arn" {
  description = "ALB HTTP listener ARN"
  value       = aws_lb_listener.http.arn
}

output "target_group_arns" {
  description = "Target group ARNs by application"

  value = {
    for key, target_group in aws_lb_target_group.app :
    key => target_group.arn
  }
}

output "target_group_names" {
  description = "Target group names by application"

  value = {
    for key, target_group in aws_lb_target_group.app :
    key => target_group.name
  }
}