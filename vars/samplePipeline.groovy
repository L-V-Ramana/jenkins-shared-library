def call(Map condfigMap){

    pipeline {
        agent { label 'agent-1' }

        environment {
            greeting = condfigMap.get('greeting')
        }

        stages {
            stage('printing-greeting') {
                steps {
                    echo "${greeting}"
                }
            }
        }
    }

}