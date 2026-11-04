resource "aws_lb" "this" {
  name               = "${var.project_name}-${var.environment}-alb"
  internal           = false
  load_balancer_type = "application"

  security_groups = [
    var.alb_security_group_id
  ]

  subnets = values(var.public_subnet_ids)

  tags = {
    Name        = "${var.project_name}-${var.environment}-alb"
    Environment = var.environment
    Project     = var.project_name
  }
}


resource "aws_lb_target_group" "app" {
  for_each = var.alb_applications

  name = substr(
    "${var.project_name}-${var.environment}-${each.key}",
    0,
    32
  )

  port        = each.value.port
  protocol    = "HTTP"
  target_type = "ip"

  vpc_id = var.vpc_id

  health_check {
    enabled             = true
    protocol            = "HTTP"
    path                = "/"
    port                = "traffic-port"
    healthy_threshold   = 3
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-${each.key}-tg"
    Environment = var.environment
    Project     = var.project_name
    Application = each.key
  }
}


resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.this.arn

  port     = 80
  protocol = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app["ui-service"].arn
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-http-listener"
    Environment = var.environment
    Project     = var.project_name
  }
}


resource "aws_lb_listener_rule" "api" {
  listener_arn = aws_lb_listener.http.arn

  priority = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app["api-gateway"].arn
  }

  condition {
    path_pattern {
      values = [
        "/api/*"
      ]
    }
  }
}