
output "bucket_name" {
  description = "S3 bucket name"
  value       = aws_s3_bucket.lecture_files.id
}

output "bucket_arn" {
  description = "S3 bucket ARN"
  value       = aws_s3_bucket.lecture_files.arn
}

output "bucket_regional_domain_name" {
  description = "S3 bucket regional domain name"
  value       = aws_s3_bucket.lecture_files.bucket_regional_domain_name
}
