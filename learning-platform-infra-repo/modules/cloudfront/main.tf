
resource "aws_cloudfront_distribution" "this" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "${var.project_name}-${var.environment}-distribution"

  origin {
    domain_name = var.alb_dns_name
    origin_id   = "application-alb"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  # UI traffic
  default_cache_behavior {
    target_origin_id       = "application-alb"
    viewer_protocol_policy = "redirect-to-https"

    allowed_methods = ["GET", "HEAD", "OPTIONS"]
    cached_methods  = ["GET", "HEAD"]

    cache_policy_id = var.cache_policy_id
  }

  # API traffic
  ordered_cache_behavior {
    path_pattern           = "/api/*"
    target_origin_id       = "application-alb"
    viewer_protocol_policy = "redirect-to-https"

    allowed_methods = [
      "GET", "HEAD", "OPTIONS",
      "PUT", "POST", "PATCH", "DELETE"
    ]

    cached_methods = ["GET", "HEAD"]

    cache_policy_id          = var.api_cache_policy_id
    origin_request_policy_id = var.api_origin_request_policy_id
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-cloudfront"
    Project     = var.project_name
    Environment = var.environment
  }
}
