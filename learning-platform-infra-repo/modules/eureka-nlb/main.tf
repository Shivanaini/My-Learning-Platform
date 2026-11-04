
# Security group for Eureka internal NLB
resource "aws_security_group" "nlb" {
  name        = "${var.project_name}-${var.environment}-eureka-nlb-sg"
  description = "Security group for internal Eureka NLB"
  vpc_id      = var.vpc_id

  # Allow ECS services to connect to Eureka through the NLB
  ingress {
    description     = "Allow ECS tasks to access Eureka NLB"
    from_port       = 8761
    to_port         = 8761
    protocol        = "tcp"
    security_groups = [var.ecs_security_group_id]
  }

  # Allow NLB to forward traffic to Discovery Server ECS tasks
  egress {
    description     = "Allow traffic to Discovery Server ECS tasks"
    from_port       = 8761
    to_port         = 8761
    protocol        = "tcp"
    security_groups = [var.ecs_security_group_id]
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-eureka-nlb-sg"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Internal Network Load Balancer for Eureka
resource "aws_lb" "this" {
  name               = "${var.project_name}-${var.environment}-eureka-nlb"
  internal           = true
  load_balancer_type = "network"

  subnets         = var.private_subnet_ids
  security_groups = [aws_security_group.nlb.id]

  tags = {
    Name        = "${var.project_name}-${var.environment}-eureka-nlb"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Target group for Eureka ECS tasks
resource "aws_lb_target_group" "eureka" {
  name        = "${var.project_name}-${var.environment}-eureka-tg"
  port        = 8761
  protocol    = "TCP"
  target_type = "ip"
  vpc_id      = var.vpc_id

  health_check {
    protocol = "TCP"
    port     = "traffic-port"
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-eureka-tg"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Listener for Eureka traffic
resource "aws_lb_listener" "eureka" {
  load_balancer_arn = aws_lb.this.arn
  port              = 8761
  protocol          = "TCP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.eureka.arn
  }
}

# Allow NLB traffic to reach Discovery Server ECS tasks
resource "aws_security_group_rule" "allow_eureka_from_nlb" {
  type                     = "ingress"
  description              = "Allow Eureka NLB to reach Discovery Server"
  from_port                = 8761
  to_port                  = 8761
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.nlb.id
  security_group_id        = var.ecs_security_group_id
}
