package com.sreasons.tools

class JavaTools implements Serializable{

  protected String environment
  protected String sourceProject
  protected String projectName
  protected List<String> commandsAfterBuild
  def script

  JavaTools(script, String sourceProject){

    this.script = script
    this.environment = this.script.env.ENV
    this.sourceProject = sourceProject
    this.commandsAfterBuild = commandsAfterBuild

    this.script.echo """
    **********************************************************************************************
    JavaTools
    environment : ${environment}        
    sourceProject: ${sourceProject}
    
    **********************************************************************************************
    """    
  }

  JavaTools(script, String sourceProject, String projectName, List<String> commandsAfterBuild =[]){

    this.script = script
    this.environment = this.script.env.ENV
    this.sourceProject = sourceProject
    this.projectName = projectName
    this.commandsAfterBuild = commandsAfterBuild

    this.script.echo """
    **********************************************************************************************
    JavaTools
    environment : ${environment}        
    sourceProject: ${sourceProject}
    
    **********************************************************************************************
    """    
  }

  def buildJavaApp(){
    this.script.dir(this.sourceProject){
      script.sh "mvn -s settings.xml clean package"      
    }
    script.executeCommands(commandsAfterBuild)
  }

}