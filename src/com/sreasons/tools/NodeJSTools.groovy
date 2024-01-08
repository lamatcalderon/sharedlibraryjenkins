package com.sreasons.tools

class NodeJSTools implements Serializable {

  protected String environment
  protected String sourceProject
  protected List<String> commandsAfterBuild
  def script

  NodeJSTools(script, String sourceProject, List<String> commandsAfterBuild = []){
    this.script = script
    this.environment = this.script.env.ENV
    this.sourceProject = sourceProject
    this.commandsAfterBuild = commandsAfterBuild

    this.script.echo """
    **********************************************************************************************
    NodeJSTools
    environment : ${environment}        
    sourceProject: ${sourceProject}
        
    **********************************************************************************************
    """
  }  

  def buildNodeJSApp(){    
    this.script.dir(this.sourceProject){
      if(script.isUnix()){//compilacion on linux
        script.sh "npm install"
        script.sh "npm run build"      
      } else {// compilacion on windows
        script.powershell "npm install"
        script.powershell "npm run build"
        script.executeCommands(commandsAfterBuild)      
        script.stash includes: '/dist/**/*', name: 'builtSources'
      }
    }
  }
}