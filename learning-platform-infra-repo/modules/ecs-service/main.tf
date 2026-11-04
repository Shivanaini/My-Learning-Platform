
# Get the current AWS region
data "aws_region" "current" {}

# --------------------------------------------------
# 1. CloudWatch Log Groups
# --------------------------------------------------

resource "aws_cloudwatch_log_group" "this" {
  for_each = var.services

  name              = "/ecs/${var.project_name}/${var.environment}/${each.key}"
  retention_in_days = var.log_retention_days

  tags = {
    Name        = "${var.project_name}-${var.environment}-${each.key}-logs"
    Environment = var.environment
    Project     = var.project_name
  }
}

# --------------------------------------------------
# 2. ECS Task Definitions
# --------------------------------------------------

resource "aws_ecs_task_definition" "this" {
  for_each = var.services

  family                   = "${var.project_name}-${var.environment}-${each.key}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]

  cpu    = tostring(each.value.cpu)
  memory = tostring(each.value.memory)

  execution_role_arn = var.execution_role_arn
  task_role_arn      = var.task_role_arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = each.value.image
      essential = true

      portMappings = [
        {
          containerPort = each.value.container_port
          hostPort      = each.value.container_port
          protocol      = "tcp"
        }
      ]

      environment = [
        for name, value in each.value.environment : {
          name  = name
          value = value
        }
      ]

      secrets = [
        for name, value in each.value.secrets : {
          name      = name
          valueFrom = value
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"

        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.this[each.key].name
          "awslogs-region"        = data.aws_region.current.region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = {
    Name        = "${var.project_name}-${var.environment}-${each.key}-task"
    Environment = var.environment
    Project     = var.project_name
  }
}

# --------------------------------------------------
# 3. ECS Fargate Services
# --------------------------------------------------

resource "aws_ecs_service" "this" {
  for_each = var.services

  name            = "${var.project_name}-${var.environment}-${each.key}"
  cluster         = var.cluster_id
  task_definition = aws_ecs_task_definition.this[each.key].arn

  launch_type      = "FARGATE"
  desired_count    = each.value.desired_count
  platform_version = "LATEST"
  health_check_grace_period_seconds = 120
 
  deployment_circuit_breaker {
    enable   = true
    rollback = true
}

  network_configuration {
    subnets          = var.subnet_ids
    security_groups  = [var.ecs_security_group_id]
    assign_public_ip = var.assign_public_ip
  }

  # Attach only services that have an ALB target group.
  dynamic "load_balancer" {
    for_each = each.value.target_group_key == null ? [] : [each.value.target_group_key]

    content {
      target_group_arn = var.target_group_arns[load_balancer.value]
      container_name   = each.key
      container_port   = each.value.container_port
    }
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-${each.key}-service"
    Environment = var.environment
    Project     = var.project_name
  }
}
