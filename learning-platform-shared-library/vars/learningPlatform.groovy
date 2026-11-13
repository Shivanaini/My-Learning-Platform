
def call() {

    pipeline {

        agent any

        options {
            timestamps()
            disableConcurrentBuilds()
            timeout(time: 60, unit: 'MINUTES')
        }

        environment {
            AWS_REGION = 'ap-south-1'
            AWS_CREDENTIALS_ID = 'aws-credentials'
            SONARQUBE_SERVER = 'SonarQube'

            PROJECT_NAME = 'learning-platform'
            ENVIRONMENT = 'dev'
            ECS_CLUSTER = 'learning-platform-dev-cluster'
        }

        stages {

            stage('Checkout') {
                steps {
                    echo 'Checking out Learning Platform source code...'
                    checkout scm
                }
            }

            stage('Resolve Service Selection') {
                steps {
                    script {

                        def allServices = [
                            'discovery-server',
                            'api-gateway',
                            'auth-service',
                            'lecture-service',
                            'ui-service'
                        ]

                        def selectedService =
                            params.SERVICE_NAME?.toString()?.trim() ?: 'ALL'

                        if (selectedService == 'ALL') {
                            env.SELECTED_SERVICES = allServices.join(',')
                        } else if (allServices.contains(selectedService)) {
                            env.SELECTED_SERVICES = selectedService
                        } else {
                            error "Invalid service selection: ${selectedService}"
                        }

                        env.SERVICE_SELECTION = selectedService

                        env.RUN_TESTS =
                            (params.RUN_TESTS == null ||
                            params.RUN_TESTS.toString().toBoolean()).toString()

                        env.RUN_SONAR =
                            (params.RUN_SONAR == null ||
                            params.RUN_SONAR.toString().toBoolean()).toString()

                        env.DEPLOY_TO_ECS =
                            (params.DEPLOY_TO_ECS == null ||
                            params.DEPLOY_TO_ECS.toString().toBoolean()).toString()

                        echo "Selected service: ${selectedService}"
                        echo "Services to process: ${env.SELECTED_SERVICES}"
                        echo "Run tests: ${env.RUN_TESTS}"
                        echo "Run SonarQube: ${env.RUN_SONAR}"
                        echo "Deploy to ECS: ${env.DEPLOY_TO_ECS}"
                    }
                }
            }

            stage('Validate Tools and Configuration') {
                steps {
                    script {

                        def selectedServices =
                            env.SELECTED_SERVICES.split(',')

                        selectedServices.each { service ->

                            def repositoryVariable =
                                "ECR_${service.toUpperCase().replace('-', '_')}"

                            if (!env[repositoryVariable]?.trim()) {
                                error "Missing Jenkins environment variable: ${repositoryVariable}"
                            }
                        }
                    }

                    sh '''
                        set -eu

                        java -version
                        mvn -version
                        docker --version
                        aws --version
                    '''

                    script {
                        if (env.DEPLOY_TO_ECS == 'true') {
                            sh 'jq --version'
                        }
                    }

                    echo 'Configuration validation completed.'
                }
            }

            stage('Maven Build and Test') {
                steps {
                    script {

                        def command = ['mvn', 'clean', 'verify']

                        if (env.SERVICE_SELECTION != 'ALL') {
                            command.add('-pl')
                            command.add(env.SERVICE_SELECTION)
                            command.add('-am')
                        }

                        if (env.RUN_TESTS != 'true') {
                            command.add('-DskipTests')
                        }

                        echo "Executing Maven command: ${command.join(' ')}"

                        sh command.join(' ')
                    }
                }
            }

            stage('SonarQube Analysis') {

                when {
                    expression {
                        env.RUN_SONAR == 'true'
                    }
                }

                steps {
                    script {

                        def command = ['mvn']

                        if (env.SERVICE_SELECTION != 'ALL') {
                            command.add('-pl')
                            command.add(env.SERVICE_SELECTION)
                            command.add('-am')
                        }

                        command.add('sonar:sonar')

                        echo "Executing SonarQube analysis: ${command.join(' ')}"

                        withSonarQubeEnv("${env.SONARQUBE_SERVER}") {
                            sh command.join(' ')
                        }
                    }
                }
            }

            stage('Generate Image Tag') {
                steps {
                    script {

                        def commitId = sh(
                            script: 'git rev-parse --short HEAD',
                            returnStdout: true
                        ).trim()

                        env.IMAGE_TAG = "${env.BUILD_NUMBER}-${commitId}"

                        echo "Docker image tag: ${env.IMAGE_TAG}"
                    }
                }
            }

            stage('Build and Push Docker Images to ECR') {
                steps {
                    withCredentials([
                        usernamePassword(
                            credentialsId: "${env.AWS_CREDENTIALS_ID}",
                            usernameVariable: 'AWS_ACCESS_KEY_ID',
                            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                        )
                    ]) {

                        script {

                            def selectedServices =
                                env.SELECTED_SERVICES.split(',')

                            selectedServices.each { service ->

                                def repositoryVariable =
                                    "ECR_${service.toUpperCase().replace('-', '_')}"

                                def repository = env[repositoryVariable]

                                def imageUri =
                                    "${repository}:${env.IMAGE_TAG}"

                                withEnv([
                                    "SERVICE_NAME=${service}",
                                    "IMAGE_URI=${imageUri}",
                                    "REPOSITORY_URL=${repository}"
                                ]) {

                                    sh '''
                                        set -eu

                                        export AWS_DEFAULT_REGION="$AWS_REGION"

                                        REGISTRY="${REPOSITORY_URL%%/*}"

                                        echo "Logging in to ECR..."

                                        aws ecr get-login-password \
                                            --region "$AWS_REGION" |
                                            docker login \
                                                --username AWS \
                                                --password-stdin "$REGISTRY"

                                        echo "Building Docker image for $SERVICE_NAME..."

                                        docker build \
                                            -f "$SERVICE_NAME/Dockerfile" \
                                            -t "$IMAGE_URI" \
                                            "$SERVICE_NAME"

                                        echo "Pushing image to ECR..."

                                        docker push "$IMAGE_URI"

                                        echo "Successfully pushed $IMAGE_URI"
                                    '''
                                }
                            }
                        }
                    }
                }
            }

            stage('Deploy to ECS') {

                when {
                    expression {
                        env.DEPLOY_TO_ECS == 'true'
                    }
                }

                steps {
                    withCredentials([
                        usernamePassword(
                            credentialsId: "${env.AWS_CREDENTIALS_ID}",
                            usernameVariable: 'AWS_ACCESS_KEY_ID',
                            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                        )
                    ]) {

                        script {

                            def selectedServices =
                                env.SELECTED_SERVICES.split(',')

                            selectedServices.each { service ->

                                def repositoryVariable =
                                    "ECR_${service.toUpperCase().replace('-', '_')}"

                                def repository = env[repositoryVariable]

                                def serviceName =
                                    "${env.PROJECT_NAME}-${env.ENVIRONMENT}-${service}"

                                def taskFamily =
                                    "${env.PROJECT_NAME}-${env.ENVIRONMENT}-${service}"

                                def imageUri =
                                    "${repository}:${env.IMAGE_TAG}"

                                withEnv([
                                    "SERVICE_NAME=${serviceName}",
                                    "TASK_FAMILY=${taskFamily}",
                                    "CONTAINER_NAME=${service}",
                                    "IMAGE_URI=${imageUri}"
                                ]) {

                                    sh '''
                                        set -eu

                                        export AWS_DEFAULT_REGION="$AWS_REGION"

                                        echo "Retrieving current task definition..."

                                        aws ecs describe-task-definition \
                                            --task-definition "$TASK_FAMILY" \
                                            --include TAGS \
                                            --output json \
                                            > described-task-definition.json

                                        echo "Validating container name..."

                                        jq -e --arg name "$CONTAINER_NAME" \
                                            '.taskDefinition.containerDefinitions |
                                            any(.name == $name)' \
                                            described-task-definition.json \
                                            > /dev/null

                                        echo "Preparing updated task definition..."

                                        jq \
                                            --arg name "$CONTAINER_NAME" \
                                            --arg image "$IMAGE_URI" \
                                            '
                                            .taskDefinition
                                            | del(
                                                .taskDefinitionArn,
                                                .revision,
                                                .status,
                                                .requiresAttributes,
                                                .compatibilities,
                                                .registeredAt,
                                                .registeredBy,
                                                .deregisteredAt
                                            )
                                            | (
                                                .containerDefinitions[]
                                                | select(.name == $name)
                                                | .image
                                              ) = $image
                                            ' \
                                            described-task-definition.json \
                                            > task-definition.json

                                        jq '.tags // []' \
                                            described-task-definition.json \
                                            > task-definition-tags.json

                                        echo "Registering new task definition revision..."

                                        if [ "$(jq 'length' task-definition-tags.json)" -gt 0 ]; then

                                            NEW_TASK_DEFINITION_ARN=$(
                                                aws ecs register-task-definition \
                                                    --cli-input-json file://task-definition.json \
                                                    --tags file://task-definition-tags.json \
                                                    --query 'taskDefinition.taskDefinitionArn' \
                                                    --output text
                                            )

                                        else

                                            NEW_TASK_DEFINITION_ARN=$(
                                                aws ecs register-task-definition \
                                                    --cli-input-json file://task-definition.json \
                                                    --query 'taskDefinition.taskDefinitionArn' \
                                                    --output text
                                            )

                                        fi

                                        echo "Updating ECS service..."

                                        aws ecs update-service \
                                            --cluster "$ECS_CLUSTER" \
                                            --service "$SERVICE_NAME" \
                                            --task-definition "$NEW_TASK_DEFINITION_ARN" \
                                            --output json > /dev/null

                                        echo "Deployment submitted for $SERVICE_NAME"
                                    '''
                                }
                            }
                        }
                    }
                }
            }

            stage('Verify ECS Deployment') {

                when {
                    expression {
                        env.DEPLOY_TO_ECS == 'true'
                    }
                }

                steps {
                    withCredentials([
                        usernamePassword(
                            credentialsId: "${env.AWS_CREDENTIALS_ID}",
                            usernameVariable: 'AWS_ACCESS_KEY_ID',
                            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                        )
                    ]) {

                        script {

                            def selectedServices =
                                env.SELECTED_SERVICES.split(',')

                            selectedServices.each { service ->

                                def serviceName =
                                    "${env.PROJECT_NAME}-${env.ENVIRONMENT}-${service}"

                                withEnv([
                                    "SERVICE_NAME=${serviceName}"
                                ]) {

                                    sh '''
                                        set -eu

                                        export AWS_DEFAULT_REGION="$AWS_REGION"

                                        echo "Waiting for $SERVICE_NAME..."

                                        aws ecs wait services-stable \
                                            --cluster "$ECS_CLUSTER" \
                                            --services "$SERVICE_NAME"

                                        aws ecs describe-services \
                                            --cluster "$ECS_CLUSTER" \
                                            --services "$SERVICE_NAME" \
                                            --query 'services[0]' \
                                            --output json \
                                            > ecs-service-status.json

                                        jq -e '
                                            .desiredCount == .runningCount
                                            and .pendingCount == 0
                                        ' ecs-service-status.json > /dev/null

                                        echo "$SERVICE_NAME is stable."
                                    '''
                                }
                            }
                        }
                    }
                }
            }
        }

        post {

            success {
                echo 'Learning Platform pipeline completed successfully.'
            }

            failure {
                echo 'Learning Platform pipeline failed. Check the failed stage and Jenkins console output.'
            }

            always {
                echo 'Pipeline execution finished.'
            }
        }
    }
}
