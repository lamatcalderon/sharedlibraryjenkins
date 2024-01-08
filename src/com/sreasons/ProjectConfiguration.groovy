package com.sreasons

class ProjectConfiguration {

  def environment;
  def env; 
  def projectName;
  def sourceProject;
  def lambdaName;
  def nexusRepositoryName;
  def buildNumber;
  def timeout;
  def withDeploy = false;
  def folderDeploy;
  DockerConfiguration dockerConfiguration;
  List<String> commandsAtInit;
  List<String> commandsAfterBuild;
  ArtifactConfiguration artifactConfiguration;
  def cleanOnlyFilesFolderDeploy = false;
  DeployConfiguration deployConfiguration = new DeployConfiguration()
  BuildConfiguration buildConfiguration = new BuildConfiguration()
}

class DockerConfiguration{
  def imageName;
  def containerLocalPort;
}

class ArtifactConfiguration{
  def repositoryName;
  def folderDist;
}

class DeployConfiguration{
  boolean forceRefreshInstance = false;
  def scalingGroupName;  
  String typeDeploy; // IIS, CLOUDFRONT
  CloudfrontParameters cloudfrontParameters = null;
  CloudfrontParameters cloudfrontParametersStage2 = null;
}

class CloudfrontParameters{
  def bucketName;
  def distributionId;
}

class BuildConfiguration{
  def gradleTask = "assembleRelease";  
}