terraform {
  backend "s3" {
    bucket       = "my-learning-platform-terraform-state-2026"
    key          = "learning-platform/dev/terraform.tfstate"
    region       = "ap-southeast-2"
    encrypt      = true
    use_lockfile = true
  }
}
