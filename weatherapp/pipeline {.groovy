
pipeline {
    agent any
  environment {
		DOCKERHUB_CREDENTIALS=credentials('dockerhub')
	}
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        disableConcurrentBuilds()
        timeout (time: 60, unit: 'MINUTES')
        timestamps()
      }
    stages {



 
        stage('Test auth') {
	     agent {
            docker {
              image 'golang:alpine'
              args '-u root:root'
            }
           }
            steps {
                sh '''
            id
            cd weatherapp/auth/src/main
            go build 
            cd -
            ls -la
                '''
            }
        }


        stage('Test UI') {
	     agent {
            docker {
              image 'node:17'
              args '-u root:root'
            }
           }
            steps {
                sh '''
            cd weatherapp/UI
            npm run
                '''
            }
        }

        stage('Test weather') {
	     agent {
            docker {
              image 'python:3.8-slim-buster'
              args '-u root:root'
            }
           }
            steps {
                sh '''
            cd weatherapp/weather
            pip3 install -r requirements.txt
                '''
            }
        }


         stage('SonarQube analysis') {
            agent {
                docker {
                  image 'sonarsource/sonar-scanner-cli:4.7.0'
                }
               }
               environment {
        CI = 'true'
        //  scannerHome = tool 'Sonar'
        scannerHome='/opt/sonar-scanner'
    }
            steps{
                withSonarQubeEnv('Sonar') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
            }
        }



        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                waitForQualityGate abortPipeline: true }
            }
        }

    stage('Login') {

			steps {
				sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
			}
		}

        stage('Build auth') {
            steps {
                sh '''
            cd $WORKSPACE/weatherapp/auth
            docker build -t devopseasylearning/weatherapp-auth:${BUILD_NUMBER} .
                '''
            }
        }


        stage('Build UI') {
            steps {
                sh '''
            cd $WORKSPACE/weatherapp/UI
            docker build -t devopseasylearning/weatherapp-ui:${BUILD_NUMBER} .
                '''
            }
        }

        stage('Build Weather') {
            steps {
                sh '''
            cd $WORKSPACE/weatherapp/weather
            docker build -t devopseasylearning/weatherapp-weather:${BUILD_NUMBER} .
                '''
            }
        }

        stage('Build Redis') {
            steps {
                sh '''
            cd $WORKSPACE/weatherapp/redis
            docker build -t devopseasylearning/weatherapp-redis:${BUILD_NUMBER} .
                '''
            }
        }


        stage('Build db') {
            steps {
                sh '''
            cd $WORKSPACE/weatherapp/db
            docker build -t devopseasylearning/weatherapp-db:${BUILD_NUMBER} .
                '''
            }
        }




    }


   post {
   
   success {
      slackSend (channel: '#development-alerts', color: 'good', message: "Application The_Weather_app SUCCESSFUL:  Branch name  <<${env.BRANCH_NAME}>>  Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
    }

 
    unstable {
      slackSend (channel: '#development-alerts', color: 'warning', message: "Application The_Weather_app UNSTABLE:  Branch name  <<${env.BRANCH_NAME}>>  Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
    }

    failure {
      slackSend (channel: '#development-alerts', color: '#FF0000', message: "Application The_Weather_app FAILURE:  Branch name  <<${env.BRANCH_NAME}>> Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
    }
   
    cleanup {
      deleteDir()
    }
}

}


post {
        success {
            slackSend(channel: 'session5-november-2022', color: 'good', message: "Application The_Weather_app SUCCESSFUL:  Branch name  <<${env.BRANCH_NAME}>>  Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }

        unstable {
            slackSend(channel: 'session5-november-2022', color: 'warning', message: "Application The_Weather_app UNSTABLE:  Branch name  <<${env.BRANCH_NAME}>>  Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }

        failure {
            slackSend(channel: 'session5-november-2022', color: '#FF0000', message: "Application The_Weather_app FAILURE:  Branch name  <<${env.BRANCH_NAME}>> Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }

        cleanup {  
          deleteDir()
        }
}