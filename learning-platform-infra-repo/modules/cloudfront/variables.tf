
variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "alb_dns_name" {
  description = "Application Load Balancer DNS name"
  type        = string
}

variable "cache_policy_id" {
  description = "CloudFront managed CachingOptimized policy"
  type        = string
  default     = "658327ea-f89d-4fab-a63d-7e88639e58f6"
}

variable "api_cache_policy_id" {
  description = "CloudFront managed CachingDisabled policy"
  type        = string
  default     = "4135ea2d-6df8-44a3-9df3-4b5a84be39ad"
}

variable "api_origin_request_policy_id" {
  description = "CloudFront managed AllViewerExceptHostHeader policy"
  type        = string
  default     = "b689b0a8-53d0-40ab-baf2-68738e2966ac"
}
