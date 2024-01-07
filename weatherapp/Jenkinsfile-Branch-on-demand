    
pipeline {
    agent {
        label ("BRANCH_ON_DEMAND")
    }
	
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS') 
        timestamps()
      }
    stages {
        stage('Setup parameters') {
            steps {
                script {
                    properties([
                        parameters([

                             string(name: 'BRANCH_TO_DEPLOY_FROM',
                             defaultValue: '',
                            description: '''Enter the Branch name you would like to deploy'''),


                             string(name: 'DEPLOYER_NAME',
                             defaultValue: '',
                            description: '''Enter your name'''),


                        choice(
                            choices: ['300', '450', '600', '750', '900'], 
                            name: 'KEEP_ALIVE'
                                 
                                ),

                        ])
                    ])
                }
            }
        }

    stage('check entry') {

			steps {
				sh ''' 
                #!/bin/bash

                 # Check if BRANCH_TO_DEPLOY_FROM is not empty
                 if [ -z "$BRANCH_TO_DEPLOY_FROM" ]; then
                     echo "Error: BRANCH_TO_DEPLOY_FROM is empty"
                     exit 1
                 fi
                 
                 # Check if DEPLOYER_NAME is not empty
                 if [ -z "$DEPLOYER_NAME" ]; then
                     echo "Error: DEPLOYER_NAME is empty"
                     exit 1
                 fi
                 
                 # Your deployment script logic goes here
                 echo "Starting deployment from branch '$BRANCH_TO_DEPLOY_FROM' by '$DEPLOYER_NAME'"
                 
                 # Rest of the script...

                '''
			}
		}
        stage('Login to Docker Hub') {  
            steps {
                script {
                    // Define your Docker Hub username and access token
                    def dockerHubUser = 'kisito2'
                    def dockerHubAccessToken = 'dckr_pat_BsJ_HqhkoOw66SYpHsK7q5EcCBw'

                    // Docker login command with access token
                    sh "echo ${dockerHubAccessToken} | docker login -u ${dockerHubUser} --password-stdin"
                }
            }
        }
        stage('cloning') {
            steps {
                script {
                        // Clone the Git repository
                        sh '''
                            rm -rf * || true 
                            git clone -b $BRANCH_TO_DEPLOY_FROM git@github.com:s5kisito/Jenkins-Weatherapp.git 
                            cd $WORKSPACE/Jenkins-weatherapp
                        '''
                    }
                }
            }
        }

        stage('Build auth') {
            steps {
                script {
                    sh '''
                        cd $WORKSPACE/weatherapp/auth
                        docker build -t kisito2/weatherapp-auth:${BUILD_NUMBER} .
                    '''
                }
            }
        }

        stage('Build UI') {
            steps {
                script {
                    sh '''
                        cd $WORKSPACE/weatherapp/UI
                        docker build -t kisito2/weatherapp-ui:${BUILD_NUMBER} .
                    '''
                }
            }
        }

        stage('Build weather') {
            steps {
                script {
                    sh '''
                        cd $WORKSPACE/weatherapp/weather
                        docker build -t kisito2/weatherapp-weather:${BUILD_NUMBER} .
                    '''
                }
            }
        }

        stage('Build DB') {
            steps {
                script {
                    sh '''
                        cd $WORKSPACE/weatherapp/DB
                        docker build -t kisito2/weatherapp-db:${BUILD_NUMBER} .
                    '''
                }
            }
        }

        stage('Build REDIS') {
            steps {
                script {
                    sh '''
                        cd $WORKSPACE/weatherapp/REDIS
                        docker build -t kisito2/weatherapp-redis:${BUILD_NUMBER} .
                    '''
                }
            }
        }
        stage('Generate-compose') {
          agent {
             label "BRANCH_ON_DEMAND"
          }
        
    
    steps {
        script {
            withCredentials([
                string(credentialsId: 'WEATHERAPP_MYSQL_ROOT_PASSWORD', variable: 'WEATHERAPP_MYSQL_ROOT_PASSWORD'),
                string(credentialsId: 'WEATHERAPP_REDIS_PASSWORD', variable: 'WEATHERAPP_REDIS_PASSWORD'),
                string(credentialsId: 'WEATHERAPP_DB_PASSWORD', variable: 'WEATHERAPP_DB_PASSWORD'),
                string(credentialsId: 'WEATHERAPP_APIKEY', variable: 'WEATHERAPP_APIKEY')
            ]) {
                sh '''
cat <<EOF_YAML > docker-compose.yaml
version: '3.5'
services:
  db:
    container_name: weatherapp-db
    image: kisito2/weatherapp-db:${BUILD_NUMBER}
    
    environment:
      MYSQL_ROOT_PASSWORD: ${WEATHERAPP_MYSQL_ROOT_PASSWORD}
    volumes:
      - db-data:/var/lib/mysql
    networks:   
      - weatherapp
    restart: always

  redis:
    container_name: weatherapp-redis
    image: kisito2/weatherapp-redis:${BUILD_NUMBER}
    networks:
      - weatherapp
    environment:
      REDIS_USER: redis
      REDIS_PASSWORD: ${WEATHERAPP_REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    restart: always

  weather:
    container_name: weatherapp-weather
    image: kisito2/weatherapp-weather:${BUILD_NUMBER}
    expose:
      - 5001
    environment:
      APIKEY: ${WEATHERAPP_APIKEY}"
    networks:
      - weatherapp
    restart: always
    depends_on:
      - db
      - redis

  auth:
    container_name: weatherapp-auth
    image: kisito2/weatherapp-auth:${BUILD_NUMBER}
    environment:
      DB_HOST: db
      DB_PASSWORD: ${WEATHERAPP_DB_PASSWORD}
    expose:
      - 8080
    networks:
      - weatherapp
    restart: always
    depends_on:
      - weather

  ui:
    container_name: weatherapp-ui
    image: kisito2/weatherapp-ui:${BUILD_NUMBER}
    environment:
      AUTH_HOST: auth
      AUTH_PORT: 8080
      WEATHER_HOST: weather
      WEATHER_PORT: 5001
      REDIS_USER: redis
      REDIS_PASSWORD: ${WEATHERAPP_REDIS_PASSWORD}
    expose:
      - 3000
    ports:
      - 3000:3000
    networks:
      - weatherapp
    restart: always
    depends_on:
      - auth

networks:
  weatherapp:


volumes:
  db-data:
  redis-data:

EOF_YAML

cat docker-compose.yaml

'''
            }
        }
    }
}
    
        stage('Deploy') {
            agent { 
                label "BRANCH_ON_DEMAND"
            }
            
            
            steps {
                sh '''
                    docker-compose down --remove-orphans || true
                    docker-compose up -d 
                    docker-compose ps 
                '''
            }
        }
         
        stage('checking deployment') {
          steps {
                sh '''
            ls -l
            bash Jenkins-Weatherapp/weatherapp/check.sh
                '''
            }
        }




        stage('checking website') {
            steps {
                sh '''
                ls -l
            sleep 10
#!/bin/bash
ip_address=$(curl -s https://api.ipify.org ; echo)
ip_port=3000
curl http://$ip_address:$ip_port/login | grep 'login'

if 
  [[  $? -ne 0 ]] 
then 
echo "The website is not comming up, please check"
exit 1
fi
                '''
            }
        }
        stage('deployment available') {
            steps {
                sh '''
            sleep $KEEP_ALIVE
                '''
            }
        }


        stage('Access app ') {
            steps {
                sh '''
            echo "$DEPLOYER_NAME access the application at http://$ip_address:$ip_port/login"
                '''
            }
        }
    }      
    

  //  post {
   
  //  success {
  //     slackSend (channel: 'session5-november-2022', color: 'good', message: " $DEPLOYER_NAME, Application The_Weather_app SUCCESSFUL and will be available for $KEEP_ALIVE second :  Branch name  <<${BRANCH_TO_DEPLOY_FROM}>>  Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
  //   }

 
  //   unstable {
  //     slackSend (channel: 'session5-november-2022', color: 'warning', message: "$DEPLOYER_NAME, Application The_Weather_app UNSTABLE:  Branch name  <<${BRANCH_TO_DEPLOY_FROM}>>  Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
  //   }

  //   failure {
  //     slackSend (channel: 'session5-november-2022', color: '#FF0000', message: "$DEPLOYER_NAME, Application The_Weather_app FAILURE:  Branch name  <<${BRANCH_TO_DEPLOY_FROM}>> Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
  //   }
   
  //   cleanup {
  //     deleteDir()
  //   }

  //  }

   


    
    


