pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    environment {
        EC2_IP = 'your-ec2-ip'
        DEPLOY_PATH = '/home/ubuntu/booknest'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Pulling code from GitHub...'
                git branch: 'main',
                    url: 'https://github.com/yourusername/booknest-backend.git'
            }
        }

        stage('Build All Services') {
            steps {
                echo 'Building all microservices...'
                sh '''
                    cd discovery-server && mvn clean package -DskipTests
                    cd ../config-server && mvn clean package -DskipTests
                    cd ../auth-service && mvn clean package -DskipTests
                    cd ../book-service && mvn clean package -DskipTests
                    cd ../api-gateway && mvn clean package -DskipTests
                '''
            }
        }

        stage('Deploy to EC2') {
            steps {
                echo 'Deploying to EC2...'
                sh '''
                    # Create deploy folder
                    mkdir -p ${DEPLOY_PATH}

                    # Copy all JARs
                    cp discovery-server/target/*.jar ${DEPLOY_PATH}/
                    cp config-server/target/*.jar ${DEPLOY_PATH}/
                    cp auth-service/target/*.jar ${DEPLOY_PATH}/
                    cp book-service/target/*.jar ${DEPLOY_PATH}/
                    cp api-gateway/target/*.jar ${DEPLOY_PATH}/

                    # Copy start script
                    cp start-services.sh ${DEPLOY_PATH}/
                    chmod +x ${DEPLOY_PATH}/start-services.sh

                    # Restart all services
                    ${DEPLOY_PATH}/start-services.sh
                '''
            }
        }
    }

    post {
        success {
            echo '✅ Backend deployed successfully!'
        }
        failure {
            echo '❌ Deployment failed!'
        }
    }
}