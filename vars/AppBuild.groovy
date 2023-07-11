def call(mode = "") {

    version = FindVersion()

    def skipCI = utils.validateCommitMsg()
    if(skipCI) {
        echo "*********************** Skip CI found in Commit msg *************************"
        return
    } 
    def framework = ""
    def runTests = false
    def buildCmd = ""
    def pushBinary = false
    sh "git checkout ${BRANCH}"
    if(!fileExists("pipeline-config.json")) {
        error "Pipeline config file is missing"
    }
    def appBuild = utils.loadPipelineProps("appBuild")
    if(appBuild && appBuild != "") {
        if(appBuild.framework && appBuild.framework != "") {
            framework = appBuild.framework
        }
        if(appBuild.buildCmd && appBuild.buildCmd != "") {
            buildCmd = appBuild.buildCmd
        }        
        if(appBuild.runTests != null) {
            runTests = appBuild.runTests
        }
        if(appBuild.pushBinary != null) {
            pushBinary = appBuild.pushBinary
        }        
    }
    if(framework == "") {
        if(fileExists("package.json")) {
            framework = "npm"
        } 
    }
    if(framework == "") {
        error("Framework is not defined in pipeline config")
    }
    if(framework == "npm") {
        NPMBuild(runTests,buildCmd,pushBinary)
    }

    DockerBuild(framework,version)
}
