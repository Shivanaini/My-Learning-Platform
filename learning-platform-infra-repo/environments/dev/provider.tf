provider "aws" {
  region = "ap-southeast-2"

  default_tags {
    tags = {
      Project     = "learning-platform"
      Environment = "dev"
      ManagedBy   = "Terraform"
    }
  }
}
