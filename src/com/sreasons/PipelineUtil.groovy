package com.sreasons
import com.sreasons.tools.DotnetTools;
import com.sreasons.tools.DockerTools;
import com.sreasons.tools.NodeJSTools;
import com.sreasons.tools.JavaTools;
import com.sreasons.tools.GradleTools;
import com.sreasons.security.HashicorpVault;
import com.sreasons.exceptions.InvalidArgumentException;


import groovy.json.*

class PipelineUtil implements Serializable {

    protected boolean sonnarCubeActivated = true
    
    protected script;
    
    protected String projectName 
    protected String sourceProject
    protected String environment
    protected String branchName
    protected String nameImageDocker
    protected String publishFolder
    protected String containerLocalPort

    protected ProjectConfiguration projectConfig;

    def steps

    String apisContainerCredentials = ""
    String hostingLinuxCredentials = ""
    String apisWindowsNetFrameworkCredentials = ""
    String registryCredentials = ""
    String awsCredentials = ""

    private DotnetTools dotnetTools
    private DockerTools dockerTools
    private NodeJSTools nodeJSTools
    private JavaTools javaTools
    private GradleTools gradleTools

    PipelineUtil(steps, script){        
        this.script = script
        this.steps = steps
        this.environment = this.script.env.ENV
    }

    public void initialize(Map parameters, boolean isExecuteCmdAtInit = true) throws InvalidArgumentException{

        println("entro a la funcion XDXDXD")
        /*steps.wrap([$class: 'ParentFolderBuildWrapper']) {
            this.script.env.ENV = this.script.env.ENV_PROJECT
        }
        if( this.script.env.ENV == null || this.script.env.ENV.isEmpty() ){
            throw new InvalidArgumentException('ENV_PROJECT')
        }

        this.script.cleanWs()
        //script.checkout script.scm
        this.script.checkout([
            $class: 'GitSCM',
            branches: this.script.scm.branches,
            extensions: this.script.scm.extensions + [[$class: 'LocalBranch', localBranch: '**']] ,
            userRemoteConfigs: this.script.scm.userRemoteConfigs
        ])

        this.setBranchName()

        def yaml = this.script.readYaml file: "devops/config/config-${this.script.env.ENV}.yml";
    
        projectConfig = ConfigParser.parse(yaml, this.script.env, parameters.projectID);

        projectName     = projectConfig.projectName ? projectConfig.projectName : "" 
        sourceProject   = projectConfig.sourceProject ? projectConfig.sourceProject : "" 
        nameImageDocker = projectConfig.dockerConfiguration.imageName ? projectConfig.dockerConfiguration.imageName : ""        
        containerLocalPort = projectConfig.dockerConfiguration.containerLocalPort ? projectConfig.dockerConfiguration.containerLocalPort : ""
        environment = this.script.env.ENV

        def aa = yaml.projects[parameters.projectID]
        steps.echo """
        **********************************************************************************************
        Project : ${projectName}        
        environment : ${this.script.env.ENV}        
        Project2: ${aa}
        ProjectConfig: ${projectConfig.toString()}
        BranchName: ${branchName}
        
        **********************************************************************************************
        """
        
        this.apisContainerCredentials = "sr-apiscontainer-credential-${environment}"
        this.hostingLinuxCredentials = "sr-hosting-credential-${environment}"
        this.apisWindowsNetFrameworkCredentials = "sr-apiswindows-credential-${environment}"
        this.registryCredentials = "sr-registry-credential-${environment}"
        this.awsCredentials = "sr-aws-credential-${environment}"

        if(isExecuteCmdAtInit){
            this.script.executeCommands(projectConfig.commandsAtInit)
        }*/
    }

    public void sendNotificacionRest(String message)
    {
        String url = """https://erpperuapi.smartclic.pe/Externos/api/v1/notificaciones/jenkins?namejob=${this.script.env.JOB_BASE_NAME}&version=${this.script.env.BUILD_NUMBER}&rama=${branchName}&ambiente=${this.script.env.ENV}"""
        if (message.length()>0)
        {
            String messageEncode = java.net.URLEncoder.encode(message, "UTF-8")
            url = """https://erpperuapi.smartclic.pe/Externos/api/v1/notificaciones/jenkins?namejob=${this.script.env.JOB_BASE_NAME}&version=${this.script.env.BUILD_NUMBER}&rama=${branchName}&ambiente=${this.script.env.ENV}&mensaje=${messageEncode}"""        

        }
        
        
        
        steps.echo """        
        **********************************************************************************************          
        Project : ${this.script.env.JOB_BASE_NAME}
        Version : ${this.script.env.BUILD_NUMBER}
        Ambiente : ${this.script.env.ENV}
        Rama: ${branchName}
        Enlace: ${url}          
        **********************************************************************************************
        """
        def getConnection = url.toURL().openConnection()      
        getConnection.setRequestMethod("GET")
        assert getConnection.responseCode == 200

    }
    
    def executeQA(){
        steps.echo "Execution of QA step environment: ${environment}"
    }

    def executeSonnarForCSharp(){
        steps.echo "Execution of Sonnar for CSharp environment: ${environment}"
    }

    def executeSonnarForNetCore(){
        steps.echo "Execution of Sonnar for NetCore environment: ${environment}"
    }
    
    def executeSonnarForNode(){
        steps.echo "Execution of Sonnar for Node environment: ${environment}"

        this.scannerSonarQube()
    }

    def executeBuildNetCoreApp(){
        DotnetTools dotnetTools = this.getInstanceDotnetTools()
        dotnetTools.buildNetCoreApp()
    }
    
    def executeBuildNetFramewrokApp(){
        DotnetTools dotnetTools = this.getInstanceDotnetTools()
        dotnetTools.buildNetFrameworkApp()
    }

    def executeBuildDockerImage(){
        DockerTools dockerTools = this.getInstanceDockerTools()
        dockerTools.buildDockerImage()
    }

    def executeBuildNodeJSApp(){
        NodeJSTools nodeJSTools = this.getInstanceNodeJSTools()
        nodeJSTools.buildNodeJSApp()
    }

    def executeBuildJavaApp(){
        JavaTools javaTools = this.getInstanceJavaTools()
        javaTools.buildJavaApp()
    }

    def executeBuildGradleApp(){
        GradleTools gradleTools = this.getInstanceGradleTools()
        gradleTools.buildGradleApp()
    }
    
    def executePushDockerImage(){
        DockerTools dockerTools = this.getInstanceDockerTools()
        dockerTools.publishDockerImage()
    }

    def executePrepareDeployWebApplicationStage2(){
        this.prepareDeployWebApplicationStage2()
    }

    def executeDeployLinuxContainer(){
        this.deployLinuxContainer()
    }

    def executeDeployLinuxHostingFront(){
        this.deployLinuxHostingFront()
    }

    def executeDeployWindowsAPINetFramework(){
        this.deployWindowsAPINetFramework()
    }

    def executeDeployWebApplication(){
        this.deployWebApplication()
    }
    
    def executeDeployWebApplicationStage2(){
        this.deployWebApplicationStage2()
    }

    def executeDeployLambdaFunctionAWS(){
        this.deployLambdaFunctionAWS()
    }

    def executeUploadArtifact(){
        this.uploadArtifact()
    }

    def executeUploadNetFrameworkArtifact(){ 
        this.uploadNetFrameworkArtifact()
    }

    def executeUploadArtifactGradleApp(){
        GradleTools gradleTools = this.getInstanceGradleTools()
        gradleTools.uploadArtifactGradleApp()
    }

    private DotnetTools getInstanceDotnetTools(){
        if (this.dotnetTools == null){
            this.dotnetTools = new DotnetTools(
                this.script,
                this.sourceProject,
                this.projectName,
                projectConfig.commandsAfterBuild 
                )
        }
        return this.dotnetTools
    }

    private NodeJSTools getInstanceNodeJSTools(){
        if(this.nodeJSTools == null){
            this.nodeJSTools = new NodeJSTools(
                this.script,
                this.sourceProject,
                projectConfig.commandsAfterBuild
            )
        }
        return this.nodeJSTools
    }

    private DockerTools getInstanceDockerTools(){
        if (this.dockerTools == null){
            withHVCredential([
                [
                    vaultCredentialPath : "registry",
                    vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_SECRET_TEXT,
                    secret              : "registry" 
                ]
            ]){
                this.dockerTools = new DockerTools(
                    this.script,
                    [
                        'url'               : script.env.registry,
                        'imageName'         : nameImageDocker,
                        'buildIdAsString'   : getBuildIdAsString(),
                        'sourceProject'     : sourceProject,
                    ])
            }
        }
        return this.dockerTools
    }

    private JavaTools getInstanceJavaTools(){
        if(this.javaTools == null){
            this.javaTools = new JavaTools(
                this.script,
                this.sourceProject,
                this.projectName,
                projectConfig.commandsAfterBuild
            )
        }
        return this.javaTools
    }

    private GradleTools getInstanceGradleTools(){
        if(this.gradleTools == null){
            this.gradleTools = new GradleTools(
                this.script,
                this.sourceProject,
                this.projectName,
                this.projectConfig
            )
        }
        return this.gradleTools
    }

    private String getBuildIdAsString(){
        return script.currentBuild.id.padLeft(5, "0")
    }

    


    private withHVCredential(ArrayList credentialList, Closure body){
        HashicorpVault vault = new HashicorpVault(this.script, "")
        vault.withHVCredential(credentialList, body)
    }

    private deployLinuxContainer() throws InvalidArgumentException{
        if(projectConfig.withDeploy){
            
            steps.echo """
            **********************************************************************************************
            Deploy on LINUX 
            Environment: ${environment}

            **********************************************************************************************
            """

            if(containerLocalPort =="" || containerLocalPort.isEmpty() ){
                throw new InvalidArgumentException('imagecontainerLocalPort')
            }

            withHVCredential([
                [
                    vaultCredentialPath : "registry-public",
                    vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_SECRET_TEXT,
                    secret              : "registrypublic" 
                ],
                [
                    vaultCredentialPath : "registry-credentials",
                    vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
                    username            : "registryusername",
                    password            : "registrypassword"            
                ]
            ]){
                this.script.sshPublisher(
                    continueOnError: false, failOnError: true,
                    publishers: [
                    this.script.sshPublisherDesc(
                    configName: "${apisContainerCredentials}",//"containers-server-dev",//sshIdCred,
                    verbose: true,
                    transfers: [
                        this.script.sshTransfer(execCommand: "docker login ${script.env.registrypublic} --username ${script.env.registryusername} --password ${script.env.registrypassword} "),
                        this.script.sshTransfer(execCommand: "docker pull ${removeProtocolURL(script.env.registrypublic)}${nameImageDocker}:latest"),
                        this.script.sshTransfer(execCommand: "docker container rm -f -v ${nameImageDocker}"),
                        this.script.sshTransfer(execCommand: "docker run -d --restart=always --name ${nameImageDocker} -p ${containerLocalPort}:80 ${removeProtocolURL(script.env.registrypublic)}${nameImageDocker}:latest"),  
                        this.script.sshTransfer(execCommand: 'docker image prune -f')
                    ])
                    ]
                )                   
            }
        }
    }

    private deployLinuxHostingFront() throws InvalidArgumentException{
        if(projectConfig.withDeploy){
            
            if(projectConfig.folderDeploy == null || projectConfig.folderDeploy.isEmpty() ){
                throw new InvalidArgumentException('projectConfig.folderDeploy')
            }

            steps.echo """
            **********************************************************************************************
            Deploy on Hosting LINUX 
            Environment: ${environment}

            **********************************************************************************************
            """
            withHVCredential([
                [
                    vaultCredentialPath : "nexus-credentials",
                    vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
                    username            : "nexususername",
                    password            : "nexuspassword"            
                ]
            ]){
                this.script.sshPublisher(
                    continueOnError: false, failOnError: true,
                    publishers: [
                    this.script.sshPublisherDesc(
                    configName: "${hostingLinuxCredentials}",//"containers-server-dev",//sshIdCred,
                    verbose: true,
                    transfers: [
                        this.script.sshTransfer( execCommand: "curl -o distRepo.7z -L -X GET \"https://nexus.sreasons.com/repository/${projectConfig.artifactConfiguration.repositoryName}/${projectConfig.artifactConfiguration.repositoryName}-${getBuildIdAsString()}.7z\" -H \"accept: application/json\" --user ${script.env.nexususername}:${script.env.nexuspassword} -s" ),
                        this.script.sshTransfer( execCommand: (projectConfig.cleanOnlyFilesFolderDeploy ) ? "find /home/jenkins/${projectConfig.folderDeploy} -maxdepth 1 -type f -delete " : "rm -r /home/jenkins/${projectConfig.folderDeploy}/* "),
                        this.script.sshTransfer( execCommand: "7z x distRepo.7z -o\"/home/jenkins/${projectConfig.folderDeploy}\""),
                        this.script.sshTransfer( execCommand: "chmod 777 -R /home/jenkins/${projectConfig.folderDeploy}/*"),
                        this.script.sshTransfer( execCommand: 'rm distRepo.7z ')
                    ])
                ])
            }
        }
    }

    private deployWindowsAPINetFramework() throws InvalidArgumentException{
        if(projectConfig.withDeploy && !projectConfig.deployConfiguration.forceRefreshInstance){
            
            if(projectConfig.folderDeploy == null || projectConfig.folderDeploy.isEmpty() ){
                throw new InvalidArgumentException('projectConfig.folderDeploy')
            }

            steps.echo """
            **********************************************************************************************
            Deploy on Windows API Net Framework
            Environment: ${environment}

            **********************************************************************************************
            """
            withHVCredential([
                [
                    vaultCredentialPath : "nexus-credentials",
                    vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
                    username            : "nexususername",
                    password            : "nexuspassword"            
                ]
            ]){
                this.script.sshPublisher(
                    continueOnError: false, failOnError: true,
                    publishers: [
                    this.script.sshPublisherDesc(
                    configName: "${apisWindowsNetFrameworkCredentials}",
                    verbose: true,
                    transfers: [
                        this.script.sshTransfer( execCommand: "curl -o distRepo.7z -L -X GET \"https://nexus.sreasons.com/repository/${projectConfig.artifactConfiguration.repositoryName}/${projectConfig.artifactConfiguration.repositoryName}-${getBuildIdAsString()}.7z\" -H \"accept: application/json\" --user ${script.env.nexususername}:${script.env.nexuspassword} -s" ),
                        this.script.sshTransfer( execCommand: "mkdir ${projectConfig.folderDeploy}Temp" ),
                        this.script.sshTransfer( execCommand: "move distRepo.7z ${projectConfig.folderDeploy}Temp" ),
                        this.script.sshTransfer( execCommand: "7z x ${projectConfig.folderDeploy}Temp\\distRepo.7z -o\"${projectConfig.folderDeploy}Temp\" " ),
                        this.script.sshTransfer( execCommand: "powershell Remove-Item -Force ${projectConfig.folderDeploy}Temp\\distRepo.7z" ),
                        this.script.sshTransfer( execCommand: "if exist ${projectConfig.folderDeploy} powershell Remove-Item -Recurse -Force ${projectConfig.folderDeploy} " ),
                        this.script.sshTransfer( execCommand: "powershell Rename-Item ${projectConfig.folderDeploy}Temp ${projectConfig.folderDeploy} " )
                    ])
                ])
            }
        }
        if(projectConfig.withDeploy && projectConfig.deployConfiguration.forceRefreshInstance){

            steps.echo """
            **********************************************************************************************
            Deploy Refresh Instance EC2
            Environment: ${environment}

            **********************************************************************************************
            """
            this.script.withAWS(credentials: "${registryCredentials}", region: 'us-west-2') {
                this.script.sh("aws autoscaling start-instance-refresh --auto-scaling-group-name ${projectConfig.deployConfiguration.scalingGroupName}")		
            }
        }
    }

    private deployWebApplication() throws InvalidArgumentException{
        if(projectConfig.withDeploy && projectConfig.deployConfiguration.typeDeploy == "IIS" ){
            
            if(projectConfig.folderDeploy == null || projectConfig.folderDeploy.isEmpty() ){
                throw new InvalidArgumentException('projectConfig.folderDeploy')
            }

            steps.echo """
            **********************************************************************************************
            Deploy Web Application on Windows IIS
            Environment: ${environment}

            **********************************************************************************************
            """
            withHVCredential([
                [
                    vaultCredentialPath : "nexus-credentials",
                    vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
                    username            : "nexususername",
                    password            : "nexuspassword"            
                ]
            ]){
                this.script.sshPublisher(
                    continueOnError: false, failOnError: true,
                    publishers: [
                    this.script.sshPublisherDesc(
                    configName: "${apisWindowsNetFrameworkCredentials}",
                    verbose: true,
                    transfers: [
                        this.script.sshTransfer( execCommand: "curl -o distRepo.7z -L -X GET \"https://nexus.sreasons.com/repository/${projectConfig.artifactConfiguration.repositoryName}/${projectConfig.artifactConfiguration.repositoryName}-${getBuildIdAsString()}.7z\" -H \"accept: application/json\" --user ${script.env.nexususername}:${script.env.nexuspassword} -s" ),
                        this.script.sshTransfer( execCommand: "mkdir ${projectConfig.folderDeploy}Temp" ),
                        this.script.sshTransfer( execCommand: "move distRepo.7z ${projectConfig.folderDeploy}Temp" ),
                        this.script.sshTransfer( execCommand: "7z x ${projectConfig.folderDeploy}Temp\\distRepo.7z -o\"${projectConfig.folderDeploy}Temp\" " ),
                        this.script.sshTransfer( execCommand: "powershell Remove-Item -Force ${projectConfig.folderDeploy}Temp\\distRepo.7z" ),
                        this.script.sshTransfer( execCommand: "if exist ${projectConfig.folderDeploy} powershell Remove-Item -Recurse -Force ${projectConfig.folderDeploy} " ),
                        this.script.sshTransfer( execCommand: "powershell Rename-Item ${projectConfig.folderDeploy}Temp ${projectConfig.folderDeploy} " )
                    ])
                ])
            }
        }
        if(projectConfig.withDeploy && projectConfig.deployConfiguration.typeDeploy == "CLOUDFRONT" ){

            steps.echo """
            **********************************************************************************************
            Deploy Web Application on Cloudfront
            Environment: ${environment}

            **********************************************************************************************
            """
            this.script.dir(this.projectConfig.artifactConfiguration.folderDist){
                this.script.withAWS(credentials: "${registryCredentials}", region: 'us-west-2') {
                    this.script.sh("aws s3 rm s3://${this.projectConfig.deployConfiguration.cloudfrontParameters.bucketName} --recursive")
                    this.script.sh("aws s3 sync . s3://${this.projectConfig.deployConfiguration.cloudfrontParameters.bucketName}")		
                    this.script.sh("aws cloudfront create-invalidation --distribution-id ${this.projectConfig.deployConfiguration.cloudfrontParameters.distributionId} --paths \"/*\"")
                }            
            }
        }
        if(projectConfig.withDeploy && projectConfig.deployConfiguration.typeDeploy == "LINUX" ){
            
            if(projectConfig.folderDeploy == null || projectConfig.folderDeploy.isEmpty() ){
                throw new InvalidArgumentException('projectConfig.folderDeploy')
            }

            steps.echo """
            **********************************************************************************************
            Deploy on Hosting LINUX 
            Environment: ${environment}

            **********************************************************************************************
            """
            withHVCredential([
                [
                    vaultCredentialPath : "nexus-credentials",
                    vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
                    username            : "nexususername",
                    password            : "nexuspassword"            
                ]
            ]){
                this.script.sshPublisher(
                    continueOnError: false, failOnError: true,
                    publishers: [
                    this.script.sshPublisherDesc(
                    configName: "${hostingLinuxCredentials}",
                    verbose: true,
                    transfers: [
                        this.script.sshTransfer( execCommand: "curl -o distRepo.7z -L -X GET \"https://nexus.sreasons.com/repository/${projectConfig.artifactConfiguration.repositoryName}/${projectConfig.artifactConfiguration.repositoryName}-${getBuildIdAsString()}.7z\" -H \"accept: application/json\" --user ${script.env.nexususername}:${script.env.nexuspassword} -s" ),
                        this.script.sshTransfer( execCommand: (projectConfig.cleanOnlyFilesFolderDeploy ) ? "find /home/jenkins/${projectConfig.folderDeploy} -maxdepth 1 -type f -delete " : "rm -r /home/jenkins/${projectConfig.folderDeploy}/* "),
                        this.script.sshTransfer( execCommand: "7z x distRepo.7z -o\"/home/jenkins/${projectConfig.folderDeploy}\""),
                        this.script.sshTransfer( execCommand: "chmod 777 -R /home/jenkins/${projectConfig.folderDeploy}/*"),
                        this.script.sshTransfer( execCommand: 'rm distRepo.7z ')
                    ])
                ])
            }
        }
    }

    private prepareDeployWebApplicationStage2() throws InvalidArgumentException{
        
        withHVCredential([
            [
                vaultCredentialPath : "nexus-credentials",
                vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
                username            : "nexususername",
                password            : "nexuspassword"            
            ]
        ]){
            this.script.sh("mkdir ${this.projectConfig.artifactConfiguration.folderDist}")
            this.script.dir(this.projectConfig.artifactConfiguration.folderDist){
                //this.script.sh("curl -o distRepo.7z -L -X GET \"https://nexus.sreasons.com/repository/${projectConfig.artifactConfiguration.repositoryName}/${projectConfig.artifactConfiguration.repositoryName}-${getBuildIdAsString()}.7z\" -H \"accept: application/json\" --user ${script.env.nexususername}:${script.env.nexuspassword} -s")
                this.script.sh("curl -o distRepo.7z -L -X GET \"https://nexus.sreasons.com/service/rest/v1/search/assets/download?sort=name&direction=desc&repository=${projectConfig.artifactConfiguration.repositoryName}\" -H \"accept: application/json\" --user ${script.env.nexususername}:${script.env.nexuspassword} -s" )
                this.script.sh("7z x distRepo.7z")
            }
        }
    }

    private deployWebApplicationStage2() throws InvalidArgumentException{
        if(projectConfig.withDeploy && projectConfig.deployConfiguration.typeDeploy == "CLOUDFRONT" ){

            steps.echo """
            **********************************************************************************************
            Deploy Web Application on Cloudfront
            Environment: ${environment}

            **********************************************************************************************
            """
            this.script.dir(this.projectConfig.artifactConfiguration.folderDist){
                this.script.withAWS(credentials: "${registryCredentials}", region: 'us-west-2') {
                    this.script.sh("aws s3 rm s3://${this.projectConfig.deployConfiguration.cloudfrontParametersStage2.bucketName} --recursive")
                    this.script.sh("aws s3 sync . s3://${this.projectConfig.deployConfiguration.cloudfrontParametersStage2.bucketName}")		
                    this.script.sh("aws cloudfront create-invalidation --distribution-id ${this.projectConfig.deployConfiguration.cloudfrontParametersStage2.distributionId} --paths \"/*\"")
                }            
            }
        }
    }

    private deployLambdaFunctionAWS() throws InvalidArgumentException{
        if(projectConfig.withDeploy){
            
            if(projectConfig.lambdaName == null || projectConfig.lambdaName.isEmpty() ){
                throw new InvalidArgumentException('projectConfig.lambdaName')
            }

            steps.echo """
            **********************************************************************************************
            Deploy Lambda Function on AWS
            Environment: ${environment}

            **********************************************************************************************
            """
            this.script.dir(this.sourceProject){
                this.script.withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "${awsCredentials}" ]]) {
                    this.script.sh 'rm appsettings.json'
                    this.script.sh "mv appsettings.${environment}.json appsettings.json"
                    this.script.sh "dotnet lambda deploy-function -fn ${projectConfig.lambdaName} -cfg aws-lambda-tools-defaults.json --region us-west-2 -frole lambda-role"
                }
            }
        }
    }

    private String removeProtocolURL(String url){
        String _url = ""        
        if (!url.endsWith('/')) {
            url += '/'
        }
        url = url.toLowerCase()

        if (url.startsWith('https://')) {
            _url = url.substring(8)
        } else if (url.startsWith('http://')) {
            _url = url.substring(7)
        } else {
            _url = url
        }
        return _url    
    }

    private void setBranchName(){
        if(this.script.isUnix()){
            this.branchName = this.script.steps.sh(
                script: 'git name-rev --name-only HEAD | sed "s?.*remotes/origin/??"',
                returnStdout: true
            ).trim()
        } else {
            this.branchName = this.script.steps.powershell(
                script: '(git name-rev --name-only HEAD) -replace \".*remotes/origin/\",\"\" ',
                returnStdout: true
            ).trim()
        }
    }

    private def executeCommandsAtInit(List<String> commandsForRun){
        this.script.stage('commands at init') {
            if(this.script.isUnix()){
                commandsForRun.each { val ->        
                    this.script.sh val
                }
            } else {
                commandsForRun.each { val ->        
                    this.script.powershell val
                }
            }
        }
    }

    private scannerSonarQube() throws InvalidArgumentException{
        def scannerHome = this.script.tool 'SonarQubeScanner'
        
        this.script.withSonarQubeEnv('sonarqube') {
            this.script.bat "${scannerHome}\\bin\\sonar-scanner -D\"sonar.projectKey=${projectName}-${environment}\" -D\"sonar.sources=${this.script.env.WORKSPACE}\" -D\"sonar.host.url=https://sonarqube.sreasons.com\" -D\"sonar.login=13921043a6c23ad381e174baeffd7589fdd616ce\""
        }
        this.script.timeout(time: 1, unit: 'HOURS') {
            def qg = this.script.waitForQualityGate()
            if (qg.status != 'OK') {
                //error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
        }
    }

    private uploadArtifact(){

        withHVCredential([
            [
                vaultCredentialPath : "nexus-credentials",
                vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
                username            : "nexususername",
                password            : "nexuspassword"            
            ]
        ]){
            if(!this.script.isUnix()){
                this.script.cleanWs()
                this.script.unstash 'builtSources'
            }
            this.script.dir(this.projectConfig.artifactConfiguration.folderDist){
                this.script.sh "7z a -t7z dist.7z *"
                this.script.sh ("curl -X \"POST\" https://nexus.sreasons.com/service/rest/v1/repositories/raw/hosted   -H \"accept: application/json\"   -H \"Content-Type: application/json\"   -d \"{\\\"name\\\": \\\"${projectConfig.artifactConfiguration.repositoryName}\\\",\\\"online\\\": true,\\\"storage\\\": {\\\"blobStoreName\\\": \\\"default\\\",\\\"strictContentTypeValidation\\\": true,\\\"writePolicy\\\": \\\"ALLOW\\\"},\\\"cleanup\\\": {\\\"policyNames\\\": [\\\"string\\\"]},\\\"component\\\": {\\\"proprietaryComponents\\\": false},\\\"raw\\\": {\\\"contentDisposition\\\": \\\"ATTACHMENT\\\"}}\" --user ${script.env.nexususername}:${script.env.nexuspassword}")
                this.script.sh ("curl --user ${script.env.nexususername}:${script.env.nexuspassword} --upload-file dist.7z https://nexus.sreasons.com/repository/${projectConfig.artifactConfiguration.repositoryName}/${projectConfig.artifactConfiguration.repositoryName}-${getBuildIdAsString()}.7z")
            }
        }
    }
    
    private uploadNetFrameworkArtifact(){

        withHVCredential([
            [
                vaultCredentialPath : "nexus-credentials",
                vaultCredentialType : HashicorpVault.VAULT_SECRET_TYPE_USERNAME_PASSWORD,
                username            : "nexususername",
                password            : "nexuspassword"            
            ]
        ]){
            this.script.cleanWs()
            this.script.unstash 'builtSources'
            this.script.dir(this.projectConfig.artifactConfiguration.folderDist){
                this.script.sh "7z a -t7z dist.7z * "
                this.script.sh ("curl -X \"POST\" https://nexus.sreasons.com/service/rest/v1/repositories/raw/hosted   -H \"accept: application/json\"   -H \"Content-Type: application/json\"   -d \"{\\\"name\\\": \\\"${projectConfig.artifactConfiguration.repositoryName}\\\",\\\"online\\\": true,\\\"storage\\\": {\\\"blobStoreName\\\": \\\"default\\\",\\\"strictContentTypeValidation\\\": true,\\\"writePolicy\\\": \\\"ALLOW\\\"},\\\"cleanup\\\": {\\\"policyNames\\\": [\\\"string\\\"]},\\\"component\\\": {\\\"proprietaryComponents\\\": false},\\\"raw\\\": {\\\"contentDisposition\\\": \\\"ATTACHMENT\\\"}}\" --user ${script.env.nexususername}:${script.env.nexuspassword}")
                this.script.sh ("curl --user ${script.env.nexususername}:${script.env.nexuspassword} --upload-file dist.7z https://nexus.sreasons.com/repository/${projectConfig.artifactConfiguration.repositoryName}/${projectConfig.artifactConfiguration.repositoryName}-${getBuildIdAsString()}.7z")
            }
        }
    }
}
