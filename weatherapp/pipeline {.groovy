pipeline {
    agent {
        docker {
            image 'golang:alpine'
        }
    }

    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '5', numToKeepStr: '5')
    }

    stages {
        stage('Build') {
            agent {
                docker {
                    image 'golang:alpine'
                }
            }
            steps {
                script {
                    catchError {
                        sh 'go build -o /go/bin/app ./weatherapp/auth/src/main'
                    }
                }
            }
        }

        stage('Print Working Directory') {
            agent any
            steps {
                sh 'pwd'
            }
        }

        stage('List Directory Contents') {
            agent any
            steps {
                sh 'ls -al'
            }
        }
    }

    post {
        always {
            // Archive or publish build artifacts
            archiveArtifacts(artifacts: '/go/bin/app')
        }
    }
}
