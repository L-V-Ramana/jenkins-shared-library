def call(Map configMap){

        pipeline{
            agent{ label 'agent-1'}
            
            environment{
                greeting=configMap.get('greeting')
            }
        }

        stages{
            stage('printing-greeting'){
                steps{
                    echo "${greeting}"
                }
                
            }
        }
}

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