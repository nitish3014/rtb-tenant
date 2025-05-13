pipeline {
    agent { label 'slave' }

    environment {
        DOCKER_CREDENTIALS_ID = 'Docker'
        SCANNER_HOME = tool 'sonar-scanner'
        AWS_REGION = 'us-east-1'
        ENV_NAME = "${env.BRANCH_NAME == 'main' ? 'prod' : 'dev'}"
        EKS_CLUSTER_NAME = "${env.BRANCH_NAME == 'main' ? 'MVPin90-prod' : 'MVPin90'}"
        K8S_NAMESPACE = "${env.BRANCH_NAME == 'main' ? 'mvp90-prod' : 'mvp90-dev'}"
        DOCKER_IMAGE_NAME = "${env.BRANCH_NAME == 'main' ? 'tenant-service-prod' : 'tenant-service-dev'}"
    }

    stages {
        stage('Checkout Code') {
            steps {
                cleanWs()
                script {
                    git credentialsId: 'Github', 
                        url: 'https://github.com/PixidAi/tenant-service.git',
                        branch: "${env.BRANCH_NAME}"
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    def commit = sh(returnStdout: true, script: "git log -1 --pretty=format:'%h'").trim()
                    def commitMessage = sh(returnStdout: true, script: "git log -1 --pretty=format:'%s'").trim()
                    def committerName = sh(returnStdout: true, script: "git log -1 --pretty=format:'%cn'").trim()
                    def message = """
🚀 Build Started:
*Build Name:* ${env.JOB_NAME} [${env.BUILD_NUMBER}]
*Build Status:* Started
*Trigger Commit:* ${commit}
*Commit Message:* ${commitMessage}
*Changes Made By:* ${committerName}
*Build URL:* ${env.BUILD_URL}
                    """
                    sendMessageToChatChannels(message)

                    if (isUnix()) {
                        sh '''
                        sudo chmod +x ./gradlew
                        ./gradlew clean build
                        '''
                    } else {
                        bat 'gradlew.bat build'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew test'
                    } else {
                        bat 'gradlew.bat test'
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'build/libs/**/*.jar', allowEmptyArchive: true
            }
        }

        stage("Sonarqube Analysis") {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh '''
                    $SCANNER_HOME/bin/sonar-scanner \
                    -Dsonar.projectName=${DOCKER_IMAGE_NAME} \
                    -Dsonar.projectKey=${DOCKER_IMAGE_NAME} \
                    -Dsonar.sources=. \
                    -Dsonar.java.binaries=build/classes/java/main \
                    -X
                    '''
                }
            }
        }

        stage('Build Docker') {
            steps {
                script {
                    sh """
                    sudo docker image prune -a -f
                    echo 'Building Docker Image...'
                    sudo docker build -t nitish0104/${DOCKER_IMAGE_NAME}:latest -t nitish0104/${DOCKER_IMAGE_NAME}:${BUILD_NUMBER} .
                    echo 'Docker Build Completed'
                    """
                }
            }
        }

        stage('Push Docker Image to DockerHub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh """
                        echo 'Logging into DockerHub...'
                        echo $DOCKER_PASSWORD | sudo docker login -u $DOCKER_USERNAME --password-stdin
                        echo 'Pushing Docker Image...'
                        sudo docker push nitish0104/${DOCKER_IMAGE_NAME}:latest
                        sudo docker push nitish0104/${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}
                        """
                    }
                }
            }
        }

        stage('Update Kubernetes Deployment') {
            steps {
                script {
                    sh """
                    echo 'Authenticating with EKS Cluster...'
                    aws eks --region $AWS_REGION update-kubeconfig --name $EKS_CLUSTER_NAME

                    echo 'Restarting Kubernetes Deployment...'
                    kubectl rollout restart deployment/deployment-tenant -n $K8S_NAMESPACE
                    kubectl rollout status deployment/deployment-tenant -n $K8S_NAMESPACE
                    """
                }
            }
        }
    }

    post {
        success {
            script {
                def commit = sh(returnStdout: true, script: "git log -1 --pretty=format:'%h'").trim()
                def commitMessage = sh(returnStdout: true, script: "git log -1 --pretty=format:'%s'").trim()
                def committerName = sh(returnStdout: true, script: "git log -1 --pretty=format:'%cn'").trim()
                def message = """
✅ Build Success:
*Build Name:* ${env.JOB_NAME} [${env.BUILD_NUMBER}]
*Build Status:* Success
*Trigger Commit:* ${commit}
*Commit Message:* ${commitMessage}
*Changes Made By:* ${committerName}
*Build URL:* ${env.BUILD_URL}
                """
                sendMessageToChatChannels(message)
            }
        }
        failure {
            script {
                def commit = sh(returnStdout: true, script: "git log -1 --pretty=format:'%h'").trim()
                def commitMessage = sh(returnStdout: true, script: "git log -1 --pretty=format:'%s'").trim()
                def committerName = sh(returnStdout: true, script: "git log -1 --pretty=format:'%cn'").trim()
                def message = """
❌ Build Failure:
*Build Name:* ${env.JOB_NAME} [${env.BUILD_NUMBER}]
*Build Status:* Failure
*Trigger Commit:* ${commit}
*Commit Message:* ${commitMessage}
*Changes Made By:* ${committerName}
*Build URL:* ${env.BUILD_URL}
                """
                sendMessageToChatChannels(message)
            }
        }
    }
}

def sendToGoogleChat(String message) {
    withCredentials([string(credentialsId: 'google-chat-webhook', variable: 'GOOGLE_CHAT_WEBHOOK_URL')]) {
        try {
            def response = httpRequest(
                httpMode: 'POST',
                acceptType: 'APPLICATION_JSON',
                contentType: 'APPLICATION_JSON',
                requestBody: groovy.json.JsonOutput.toJson(["text": message]),
                url: GOOGLE_CHAT_WEBHOOK_URL
            )
            echo "Google Chat notification sent: ${response.status}"
        } catch (Exception e) {
            echo "Failed to send notification to Google Chat: ${e.message}"
        }
    }
}

def sendToSlack(String message) {
    withCredentials([string(credentialsId: 'slack-webhook', variable: 'SLACK_WEBHOOK_URL')]) {
        try {
            def response = httpRequest(
                httpMode: 'POST',
                acceptType: 'APPLICATION_JSON',
                contentType: 'APPLICATION_JSON',
                requestBody: groovy.json.JsonOutput.toJson(["text": message]),
                url: SLACK_WEBHOOK_URL
            )
            echo "Slack notification sent: ${response.status}"
        } catch (Exception e) {
            echo "Failed to send notification to Slack: ${e.message}"
        }
    }
}

def sendMessageToChatChannels(String message) {
    sendToGoogleChat(message)
    sendToSlack(message)
}
