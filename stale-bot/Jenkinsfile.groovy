def functions

pipeline {

    agent { label "maintenance" }

    parameters {
        string(name: 'DAYS', defaultValue: '72', description: 'A branch is defined as stale if the last commit is older than DAYS.')
        booleanParam(name: 'UPDATE_REMOTE', defaultValue: false, description: 'Purges stale branches on remote otherwise only locally.')
    }

    stages {
        stage('Clean up the stale branches') {
            steps {
                script {
                    functions = load "stale-bot/PipelineFunctions.groovy"
                    functions.purgeBranchesOlderThan(params.DAYS.toInteger(), params.UPDATE_REMOTE.toBoolean())
                }
            }
        }
    }

    post {
        cleanup {
            cleanWs()
        }
    }
}
