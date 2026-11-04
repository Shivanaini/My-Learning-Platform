
data "aws_caller_identity" "current" {}

resource "aws_s3_bucket" "lecture_files" {
  bucket = "${var.project_name}-${var.environment}-lecture-files-${data.aws_caller_identity.current.account_id}"

  tags = {
    Name        = "${var.project_name}-${var.environment}-lecture-files"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Enable bucket versioning
resource "aws_s3_bucket_versioning" "lecture_files" {
  bucket = aws_s3_bucket.lecture_files.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Enable server-side encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "lecture_files" {
  bucket = aws_s3_bucket.lecture_files.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Block all public access
resource "aws_s3_bucket_public_access_block" "lecture_files" {
  bucket = aws_s3_bucket.lecture_files.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Enforce bucket ownership
resource "aws_s3_bucket_ownership_controls" "lecture_files" {
  bucket = aws_s3_bucket.lecture_files.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}
