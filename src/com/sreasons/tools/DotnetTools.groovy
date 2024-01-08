package com.sreasons.tools
import com.sreasons.security.HashicorpVault;

class DotnetTools implements Serializable {

    protected String environment
    protected String sourceProject
    protected String projectName
    protected List<String> commandsAfterBuild
    def script

    //String msbuildExec = "c:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\MSBuild\\15.0\\Bin\\MSBuild.exe"
    String msbuildExec = "C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\BuildTools\\MSBuild\\15.0\\Bin\\MSBuild.exe"
    DotnetTools(script, String sourceProject){
        this.script = script
        this.environment = this.script.env.ENV
        this.sourceProject = sourceProject
        this.commandsAfterBuild = commandsAfterBuild

        this.script.echo """
        **********************************************************************************************
        DotnetTools
        environment : ${environment}        
        sourceProject: ${sourceProject}
        
        **********************************************************************************************
        """
    }
    
    DotnetTools(script, String sourceProject, String projectName, List<String> commandsAfterBuild = []){
        this.script = script
        this.environment = this.script.env.ENV
        this.sourceProject = sourceProject
        this.projectName = projectName
        this.commandsAfterBuild = commandsAfterBuild

        this.script.echo """
        **********************************************************************************************
        DotnetTools
        environment : ${environment}        
        sourceProject: ${sourceProject}
        
        **********************************************************************************************
        """
    }

    def buildNetCoreApp(){
        this.script.dir(this.sourceProject){
            script.sh 'rm appsettings.json'
            script.sh "mv appsettings.${environment}.json appsettings.json"
            if(this.environment == 'crt' || this.environment == 'prd'){
                withHVCredential([
                [
                    vaultCredentialPath : "configserver-pwd",
                    vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_SECRET_TEXT,
                    secret              : "pwd" 
                ]
                ]){                    
                this.script.echo """
                    **********************************************************************************************
                    Obtencion Config Server Vault                
                    **********************************************************************************************
                    """
                    this.script.sh "sed -i -e 's/PASSWORD_CONFIG_SERVER/${script.env.pwd}/' appsettings.json"
                }
            }

            script.sh 'dotnet publish -c Release' 

            script.executeCommands(commandsAfterBuild)
        }
    }

    def buildNetFrameworkApp(){
        // esta compilacion se realizara sobre entorno windows para la compilacion sobre NET Framework Applicacion
        
        this.script.dir(this.sourceProject){
		    script.powershell 'rm Web.config'
		    script.powershell "mv Web.${environment}.config Web.config"	
        }
        
        script.powershell 'nuget restore'
        script.powershell "& \"${msbuildExec}\" /p:Platform=\"Any CPU\" /p:VisualStudioVersion=12.0 /p:VSToolsPath=c:\\MSBuild.Microsoft.VisualStudio.Web.targets.14.0.0.3\\tools\\VSToolsPath "

        script.executeCommands(commandsAfterBuild)

        this.script.dir(this.sourceProject){
            script.stash includes: '/bin/**/*, Global.asax, Web.config', name: 'builtSources'
        }                
    }

    private withHVCredential(ArrayList credentialList, Closure body){
        HashicorpVault vault = new HashicorpVault(this.script, "")
        vault.withHVCredential(credentialList, body)
    }
}
