def call(Map config = [:]) {
    // Default values
    def image = config.get('image', 'my-default-image')
    def registry = config.get('registry', 'my-default-registry')
    def tag = config.get('tag', 'latest')
    def containerPort = config.get('containerPort', '8080')
    def hostPort = config.get('hostPort', '8080')

    pipeline {
        agent any

        stages {
            stage('Clone project'){
                steps{
                    git branch: 'staging', credentialsId: 'github-id', url: 'https://github.com/sunlyhuor/nextjdproject.git'
                }
            }
            stage('Docker hub login'){
                script{
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-id', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                        sh'docker login -u $USER -p $PASS'
                    }
                }
            }
            stage('Build Docker Image') {
                steps {
                    script {
                        echo "Building Docker image: ${registry}/${image}:${tag}"
                        sh """
                            docker build -t ${registry}/${image}:${tag}
                            docker push ${registry}/${image}:${tag}
                        """
                    }
                }
            }
            stage('delete container'){
                steps{
                    sh'''
                    docker rm -f test
                    '''
                }
            }
            stage('Deploy Docker Container') {
                steps {
                    script {
                        echo "Deploying Docker container: ${registry}/${image}:${tag}"
                        sh """
                            docker run -d --name test --restart=always -p ${hostPort}:${containerPort} ${registry}/${image}:${tag}
                        """
                    }
                }
            }
        }
    }
}