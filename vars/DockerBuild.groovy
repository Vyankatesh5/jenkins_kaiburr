def call(framework = "",version = "") {

    def appVersion= version
    println appVersion

    def skipCI = utils.validateCommitMsg()
    if(skipCI) {
        echo "*********************** Skip CI found in Commit msg *************************"
        return
    }
    def deployment = utils.loadPipelineProps("deployment")
    if(deployment.container != null && deployment.container != true) {
        echo "*********************** Containerization is disabled *************************"
        return
    }
    def dockerRegistry = deployment.containerRegistry
    def dockerRepo = deployment.containerRepo
    def deployEnv = deployment.environment
    def isCIDeploy = deployment.ciDeploy
    def isRelease = deployment.release
    def gitOpsHost = deployment.gitOpsHost
    def gitOpsAppName = deployment.gitOpsAppName

    if(isCIDeploy != null && isCIDeploy == true && isRelease != null && isRelease == true) {
        echo "*********************** CI Deploy && Release are enabled *************************"
        return        
    }
    if(isCIDeploy != null && isCIDeploy == true) {
        deployEnv = "dev"
    }
    if(deployment.gitOpsHost != null) {
        gitOpsHost = deployment.gitOpsHost
    }
    if(deployment.gitOpsAppName != null) {
        gitOpsAppName = deployment.gitOpsAppName
    }

    if(deployEnv == "dev" || deployEnv == "stage" || deployEnv == "prod") {
        utils.appendFile('workflow.properties', "environment=${deployEnv}")
        utils.appendFile('workflow.properties', "gitOpsHost=${gitOpsHost}")
        utils.appendFile('workflow.properties', "gitOpsAppName=${gitOpsAppName}")

        def appName = utils.loadPipelineProps("appName")

        def dockerImage = "${dockerRegistry}/${dockerRepo}/${appName}:${appVersion}"
        println "Docker image: " + dockerImage

        sh "docker build -t ${dockerImage} ."
        withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'username', passwordVariable: 'password')]) {
            sh "echo ${password} | docker login ${dockerRegistry} -u ${username} --password-stdin"
            sh "docker push ${dockerImage}"
        }
        dockerRepo = "${dockerRegistry}/${dockerRepo}/${appName}"
        
        sh 'wget https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O yq && chmod +x yq'

        def chartFolder = deployment.helmChartPath

        def helmdir = new File("${WORKSPACE}/${chartFolder}")

        if (helmdir.exists() && helmdir.isDirectory()) {
            sh "./yq '.image.repository=\"${dockerRepo}\"' -i ${WORKSPACE}/${chartFolder}/environments/dev-values.yaml"
            sh "./yq '.image.tag=\"${appVersion}\"' -i ${WORKSPACE}/${chartFolder}/environments/dev-values.yaml"
            sh "./yq '.appVersion=\"${appVersion}\"' -i ${WORKSPACE}/${chartFolder}/Chart.yaml"
            withCredentials([gitUsernamePassword(credentialsId: 'github')]) {
                def gStatus = sh(script: "git status --porcelain",returnStdout: true)
                if(gStatus != "") {
                    sh "git remote get-url origin"
                    sh "git commit -am 'Updated Helm Charts'"
                    sh "git push origin ${BRANCH}"
                }
            }
        } else {
            println "Directory does not exist: ${helmdir}"
        }
              
    } else {
        echo "*********************** Invalid Deployment Environment *************************"
        return           
    }
}
