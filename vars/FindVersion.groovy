def call(){

    sh "rm -f workflow.properties"
    def scmUrl = utils.getSCMURL()
    deleteDir()
    git url: "${scmUrl}", branch: "${BRANCH}", credentialsId: "github"
    
    def skipCI = utils.validateCommitMsg()
    if(skipCI) {
        echo "*********************** Skip CI found in Commit msg *************************"
        return
    }


    def versionMgmt = utils.loadPipelineProps("versionMgmt")
    def version

    if (versionMgmt.useCZ) {
        if (versionMgmt.bumpVersion) {
            version=BumpVersion()
        } else {
            println "Only usecz true,bumversion false"
        }
    } else if (versionMgmt.fromSource) {
        version=FromSource()
    }

    return version.trim()


}

