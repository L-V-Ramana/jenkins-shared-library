def call(Map configMap){
        pipeline {
        agent { label 'agent-1' }
        options{
            ansiColor('xterm')
        }
        environment { 
            acc_id = '867920734831'
            region = 'us-east-1'
            project = configMap.get('project')
            component = configMap.get('component')
            appVersion = ''
        }
        parameters{
            booleanParam(name:'deployment', defaultValue: false, description: 'Toggle this value')
        }

        stages {    

            stage('Read package.json') {
                steps {
                    script {
                        def packageJSON = readJSON file: 'package.json'
                        appVersion = packageJSON.version
                        echo "App Version: ${appVersion}"
                    }
                }
            }

            stage('Install dependencies') {
                steps {
                    script{
                        sh """
                            npm install 
                        """ 
                    }
                    
                }
            }

            // stage('sonar scan'){

            //      environment {
            //                   scannerHome = tool 'sonar-7.2'
            //                 }
            //     steps{
                                        
            //        script{
            //             withSonarQubeEnv(installationName: 'sonar-7.2') {
            //             sh "${scannerHome}/bin/sonar-scanner"
            //             }
            //         }
            //     }
            // }   

            // stage('waiting-scanner-results'){
            //     steps{
            //         timeout(time: 15, unit: 'MINUTES') { // Set a reasonable timeout
            //         waitForQualityGate abortPipeline: false
            //     }
            //  }
            // }

            stage('Dependabot Scan') {
                steps {
                    withCredentials([string(credentialsId: 'github-token', variable: 'TOKEN')]) {

                        script {

                            def vulnCount = sh(
                                script: '''
                                curl -s \
                                -H "Authorization: Bearer $TOKEN" \
                                -H "Accept: application/vnd.github+json" \
                                "https://api.github.com/repos/L-V-Ramana/catalogue/dependabot/alerts?state=open&per_page=100" \
                                | jq '[.[] | select(.security_advisory.severity=="high" or .security_advisory.severity=="critical")] | length'
                                ''',
                                returnStdout: true
                            ).trim()

                            echo "High/Critical Dependabot alerts: ${vulnCount}"

                            if (vulnCount.toInteger() > 0) {
                                error "Pipeline failed: High/Critical Dependabot alerts present"
                            } else {
                                echo "No High/Critical alerts. Pipeline passed."
                            }
                        }
                    }
                }
            }
    


            stage('Docker Build & Push') {
                steps {
                    script {
                        withAWS(credentials: 'aws-auth', region: "${region}") {
                            sh """
                            aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 867920734831.dkr.ecr.us-east-1.amazonaws.com
                            docker build -t ${project}/${component}:${appVersion} .
                            docker tag ${project}/${component}:${appVersion} 867920734831.dkr.ecr.us-east-1.amazonaws.com/ ${project}/${component}:${appVersion}
                            docker push 867920734831.dkr.ecr.us-east-1.amazonaws.com/${project}/${component}:${appVersion}
                            """
                        }
                    }
                }
            }

            // stage('trigger cd'){
            //     when{
            //         expression { params.deployment}
            //     }
            //     steps{
            //             echo "${appVersion}"
            //             build job: 'catalogue-cd', 
            //             parameters: [
            //                 string(name: 'appVersion', value: "${appVersion}"),
            //                 string(name: 'deploy', value: 'dev')
                    
            //             ],
            //             propagate: false, // even catalogue cd failes will not show ci as failed
            //             wait: false // wont wait untill cd complete , if ci complete show as success
            //     }

            // }

        }
    }  

}

 

