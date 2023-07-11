def call() {
  def workspaceDir = new File("${WORKSPACE}")
  def packageJsonFile = new File("${workspaceDir}/package.json")
  def version

  if (packageJsonFile.exists()) {
    def packageJson = new groovy.json.JsonSlurper().parseText(packageJsonFile.text)
    version = packageJson.version
  } else {
    throw new FileNotFoundException("No version file found in workspace")
  }

  println version
  return version
}
