def call(runTests = "false",buildCmd = "",pushBinary = false) {
       
    sh script: "npm install -g yarn && \
        yarn install"

    if(runTests){
        sh script: "yarn test"
        
    }
    
    sh script: "yarn build"
    sh script: "ls && tar -cf build.tar build"
    stash name: "build", includes: "build.tar"
    stash name: "Dockerfile", includes: "Dockerfile" 
}
