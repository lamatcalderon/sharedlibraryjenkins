package com.sreasons.tools
import com.sreasons.security.HashicorpVault;
import com.sreasons.ProjectConfiguration;

class GradleTools implements Serializable {

  protected String environment
  protected String sourceProject
  protected String projectName
  protected ProjectConfiguration projectConfig;
  protected List<String> commandsAfterBuild
  def script

  // fijando el registry de produccion para obtener la imagen de Android para compilacion de applicaciones Android
  String registryCredentials = "ecr:us-west-2:sr-registry-credential-prd" 
  String imageAndroidBuild31 = "568069219141.dkr.ecr.us-west-2.amazonaws.com/devops-mingc-android-build-box"
  String url = "https://568069219141.dkr.ecr.us-west-2.amazonaws.com/"

  GradleTools(script, String sourceProject, String projectName, ProjectConfiguration projectConfig){

    this.script = script
    this.environment = this.script.env.ENV
    this.sourceProject = sourceProject
    this.projectName = projectName
    this.projectConfig = projectConfig
    this.commandsAfterBuild = this.projectConfig.commandsAfterBuild
    
    this.script.echo """
    **********************************************************************************************
    GradleTools
    environment : ${environment}        
    sourceProject: ${sourceProject}
    
    **********************************************************************************************
    """
  }

  def buildGradleApp(){
    
    this.script.docker.withRegistry(this.url, "${registryCredentials}") {

      String dockerVolume = "-v ${script.env.WORKSPACE}:/project"
      String dockerCmd = "docker run --rm ${dockerVolume} ${imageAndroidBuild31}"

      String infoCurrentUser = this.script.steps.sh(
        script: "echo \$(id -u \${USER}):\$(id -g \${USER})",
        returnStdout: true
      ).trim()

      this.script.dir(this.sourceProject){
        script.sh "chmod +x+o gradlew" 
        script.sh "${dockerCmd} bash -c \"./gradlew ${projectConfig.buildConfiguration.gradleTask} ; chown -R ${infoCurrentUser} /project \""      
      }
      
      script.executeCommands(commandsAfterBuild)
    }

  }

  def uploadArtifactGradleApp(){

      withHVCredential([
          [
              vaultCredentialPath : "nexus-credentials",
              vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
              username            : "nexususername",
              password            : "nexuspassword"            
          ]
      ]){
          this.script.dir(this.projectConfig.artifactConfiguration.folderDist){
              this.script.sh "7z a -t7z dist.7z *"
              this.script.sh ("curl -X \"POST\" https://nexus.sreasons.com/service/rest/v1/repositories/raw/hosted   -H \"accept: application/json\"   -H \"Content-Type: application/json\"   -d \"{\\\"name\\\": \\\"${projectConfig.artifactConfiguration.repositoryName}\\\",\\\"online\\\": true,\\\"storage\\\": {\\\"blobStoreName\\\": \\\"default\\\",\\\"strictContentTypeValidation\\\": true,\\\"writePolicy\\\": \\\"ALLOW\\\"},\\\"cleanup\\\": {\\\"policyNames\\\": [\\\"string\\\"]},\\\"component\\\": {\\\"proprietaryComponents\\\": false},\\\"raw\\\": {\\\"contentDisposition\\\": \\\"ATTACHMENT\\\"}}\" --user ${script.env.nexususername}:${script.env.nexuspassword}")
              this.script.sh ("curl --user ${script.env.nexususername}:${script.env.nexuspassword} --upload-file dist.7z https://nexus.sreasons.com/repository/${projectConfig.artifactConfiguration.repositoryName}/${projectConfig.artifactConfiguration.repositoryName}-${getBuildIdAsString()}.7z")
          }
      }
  }
  
  private withHVCredential(ArrayList credentialList, Closure body){
      HashicorpVault vault = new HashicorpVault(this.script, "")
      vault.withHVCredential(credentialList, body)
  }

  private String getBuildIdAsString(){
        return script.currentBuild.id.padLeft(5, "0")
    }

}