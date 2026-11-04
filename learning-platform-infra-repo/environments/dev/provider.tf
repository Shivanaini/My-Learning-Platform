provider "aws" {
  region = "ap-south-1"

  default_tags {
    tags = {
      Project     = "learning-platform"
      Environment = "dev"
      ManagedBy   = "Terraform"
    }
  }
}