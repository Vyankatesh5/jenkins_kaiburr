def getAppName() {
    def scmUrl = scm.getUserRemoteConfigs()[0].getUrl()
    def appName = scmUrl.split("/")[-1].replaceAll(".git","")
    return appName
}

def getSCMURL() {
    def scmUrl = scm.getUserRemoteConfigs()[0].getUrl()
    return scmUrl
}

def validateCommitMsg() {
    sh "git log -1"
    def result = sh (script: "git log -1 | grep -i 'skip ci'", returnStatus: true)
    if(result == 0) {
        return true
    } else {
        return false
    }
}

def appendFile(String fileName, String line) {
    def current = ""
    if (fileExists(fileName)) {
        current = readFile fileName
    }
    writeFile file: fileName, text: current + "\n" + line
}

def getHelmChartPath() {
    def pipelineConfig = readJSON file: "${WORKSPACE}/pipeline-config.json"
    //def helmChartPath = pipelineConfig["helmChartPath"]
    def deployment = pipelineConfig["deployment"]
    def helmChartPath = deployment.helmChartPath
    println "Helm folder " + helmChartPath
    return helmChartPath
}

def loadPipelineProps(key) {
    def pipelineConfig = readJSON file: "${WORKSPACE}/pipeline-config.json"
    println pipelineConfig[key]
    return pipelineConfig[key]
}
