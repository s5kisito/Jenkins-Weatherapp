
pipeline {
    agent any

    stages {
        stage('Login to Docker Hub') {
            steps {
                script {
                    // Define your Docker Hub credentials
                    def dockerHubUser = 'christ.nyc@gmail.com'
                    def dockerHubPassword = 'chriseven'

                    // Docker login command
                    sh "docker login -u ${dockerHubUser} -p ${dockerHubPassword}"
                }
            }
        }
    }
}

