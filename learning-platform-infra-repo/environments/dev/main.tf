module "vpc" {
  source = "../../modules/vpc"

  project_name = var.project_name
  environment  = var.environment

  vpc_cidr = var.vpc_cidr

  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
}


module "security_groups" {
  source = "../../modules/security-groups"

  project_name = var.project_name
  environment  = var.environment

  vpc_id = module.vpc.vpc_id

  applications = var.applications
}


module "iam" {
  source = "../../modules/iam"

  project_name = var.project_name
  environment  = var.environment

  database_secret_arn = module.rds.db_master_user_secret_arn
  jwt_secret_arn      = module.secrets_manager.jwt_secret_arn
}


module "alb" {
  source = "../../modules/alb"

  project_name = var.project_name
  environment  = var.environment

  vpc_id = module.vpc.vpc_id

  public_subnet_ids = module.vpc.public_subnet_ids

  alb_security_group_id = module.security_groups.alb_security_group_id

  alb_applications = var.alb_applications
}

module "rds" {
  source = "../../modules/rds"

  project_name = var.project_name
  environment  = var.environment

  vpc_id = module.vpc.vpc_id

  private_subnet_ids = module.vpc.private_subnet_ids

  ecs_security_group_id = module.security_groups.ecs_security_group_id

  db_name     = var.db_name
  db_username = var.db_username


  instance_class    = var.rds_instance_class
  allocated_storage = var.rds_allocated_storage
}

module "ecr" {
  source = "../../modules/ecr"

  project_name = var.project_name
  environment  = var.environment

  repositories = var.ecr_repositories
}
module "ecs_cluster" {
  source = "../../modules/ecs-cluster"

  project_name = var.project_name
  environment  = var.environment
}


# ==================================================
# ECS SERVICES CONFIGURATION
# ==================================================

locals {
  # Common OTel settings for all five microservices
  common_otel_environment = {
    OTEL_RESOURCE_ATTRIBUTES = "service.namespace=learning-platform,deployment.environment.name=dev"

    OTEL_TRACES_EXPORTER  = "otlp"
    OTEL_METRICS_EXPORTER = "none"
    OTEL_LOGS_EXPORTER    = "none"

    OTEL_EXPORTER_OTLP_PROTOCOL = "grpc"

    OTEL_PROPAGATORS = "tracecontext,baggage"

    OTEL_TRACES_SAMPLER     = "parentbased_traceidratio"
    OTEL_TRACES_SAMPLER_ARG = "1.0"
  }

  # Five ECS microservices
  ecs_services = {
    discovery-server = {
      image          = "${module.ecr.repository_urls["discovery-server"]}:latest"
      container_port = 8761
      cpu            = 256
      memory         = 512
      desired_count  = 1

      environment = merge(local.common_otel_environment, {
        OTEL_SERVICE_NAME = "discovery-server"
      })

      secrets          = {}
      target_group_key = "eureka"
    }

    api-gateway = {
      image          = "${module.ecr.repository_urls["api-gateway"]}:latest"
      container_port = 8080
      cpu            = 256
      memory         = 512
      desired_count  = 1

      environment = merge(local.common_otel_environment, {
        OTEL_SERVICE_NAME = "api-gateway"
        EUREKA_URL        = "http://${module.eureka_nlb.nlb_dns_name}:8761/eureka"
      })

      secrets          = {}
      target_group_key = "api-gateway"
    }

    auth-service = {
      image          = "${module.ecr.repository_urls["auth-service"]}:latest"
      container_port = 8081
      cpu            = 256
      memory         = 512
      desired_count  = 1

      environment = merge(local.common_otel_environment, {
        OTEL_SERVICE_NAME      = "auth-service"
        EUREKA_URL             = "http://${module.eureka_nlb.nlb_dns_name}:8761/eureka"
        SPRING_PROFILES_ACTIVE = "postgres"
        DB_HOST                = module.rds.db_endpoint
        DB_PORT                = tostring(module.rds.db_port)
        DB_NAME                = "learning_auth"
      })

      secrets = {
        DB_USERNAME = "${module.rds.db_master_user_secret_arn}:username::"
        DB_PASSWORD = "${module.rds.db_master_user_secret_arn}:password::"
      }
      target_group_key = null
    }

    lecture-service = {
      image          = "${module.ecr.repository_urls["lecture-service"]}:latest"
      container_port = 8082
      cpu            = 256
      memory         = 512
      desired_count  = 1

      environment = merge(local.common_otel_environment, {
        OTEL_SERVICE_NAME      = "lecture-service"
        EUREKA_URL             = "http://${module.eureka_nlb.nlb_dns_name}:8761/eureka"
        SPRING_PROFILES_ACTIVE = "postgres"
        DB_HOST                = module.rds.db_endpoint
        DB_PORT                = tostring(module.rds.db_port)
        DB_NAME                = "learning_lecture"
      })

      secrets = {
        DB_USERNAME = "${module.rds.db_master_user_secret_arn}:username::"
        DB_PASSWORD = "${module.rds.db_master_user_secret_arn}:password::"
      }
      target_group_key = null
    }

    ui-service = {
      image          = "${module.ecr.repository_urls["ui-service"]}:latest"
      container_port = 8083
      cpu            = 256
      memory         = 512
      desired_count  = 1

      environment = merge(local.common_otel_environment, {
        OTEL_SERVICE_NAME = "ui-service"
        EUREKA_URL        = "http://${module.eureka_nlb.nlb_dns_name}:8761/eureka"
      })

      secrets          = {}
      target_group_key = "ui-service"
    }
  }
}

# ==================================================
# ECS SERVICES MODULE
# ==================================================


module "eureka_nlb" {
  source = "../../modules/eureka-nlb"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.vpc.vpc_id


  private_subnet_ids    = values(module.vpc.private_subnet_ids)
  ecs_security_group_id = module.security_groups.ecs_security_group_id
}
module "ecs_service" {
  source = "../../modules/ecs-service"

  project_name = var.project_name
  environment  = var.environment

  cluster_id = module.ecs_cluster.cluster_arn

  subnet_ids = values(module.vpc.private_subnet_ids)

  ecs_security_group_id = module.security_groups.ecs_security_group_id

  execution_role_arn = module.iam.ecs_execution_role_arn
  task_role_arn      = module.iam.ecs_task_role_arn

  target_group_arns = merge(
    module.alb.target_group_arns,
    {
      eureka = module.eureka_nlb.target_group_arn
    }
  )

  services = local.ecs_services

  log_retention_days = 7
  assign_public_ip   = false
}



module "secrets_manager" {
  source = "../../modules/secrets-manager"

  project_name = var.project_name
  environment  = var.environment

  recovery_window_in_days = 7
}

module "s3" {
  source = "../../modules/s3"

  project_name = var.project_name
  environment  = var.environment
}

module "cloudfront" {
  source = "../../modules/cloudfront"

  project_name = var.project_name
  environment  = var.environment

  alb_dns_name = module.alb.alb_dns_name
}