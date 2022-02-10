def functions

pipeline {

    agent { label "maintenance" }

    // add cron job :)
    // triggers {
        // “At 05:05.”
        // https://crontab.guru/
        // cron('5 5 * * *')
    // }

    parameters {
        string(name: 'DAYS', defaultValue: '35', description: 'A branch is defined as stale if the last commit is older than DAYS.')
        booleanParam(name: 'UPDATE_REMOTE', defaultValue: true, description: 'Purges stale branches on remote otherwise only locally.')
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    functions = load "stale-bot/PipelineFunctions.groovy"
                }
            }
        }
        stage('Clean up the stale branches') {
            when {
                expression { script { !functions.isPublicBranch() } }
            }
            steps {
                script {
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
