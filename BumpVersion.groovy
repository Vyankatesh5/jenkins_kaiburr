def call() {
    
    def CZ = "/usr/local/bin/cz"
    def version = sh script: "${CZ} bump --dry-run --yes | awk 'FNR==2' | cut -f2 -d: | tr -d ' '", returnStdout: true
    println "Version -- " + version
    sh "${CZ} bump --yes"
    sh "git branch"

    //utils.appendFile('workflow.properties', "version=" + version)

    withCredentials([gitUsernamePassword(credentialsId: 'github')]) {
        sh "git remote get-url origin"
        sh "git push origin ${params.BRANCH}"
    }

    return version

}
