terraform {
  backend "s3" {
    bucket       = "shakra-learning-platform-terraform-state"
    key          = "learning-platform/dev/terraform.tfstate"
    region       = "ap-south-1"
    encrypt      = true
    use_lockfile = true
  }
}