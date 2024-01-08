package com.sreasons

class ConfigParser {

  private static String LATEST = 'latest';
  private static Integer DEFAULT_TIMEOUT = 600;   // 600 seconds

  static ProjectConfiguration parse(def yaml, def env, def projectID) {
      ProjectConfiguration projectConfiguration = new ProjectConfiguration();

      projectConfiguration.buildNumber = env.BUILD_ID;

      // obtenemos el proyecto actual para CI 
      def currentProject = yaml.projects[projectID]


      // parse the environment variables and jenkins environment variables to be passed
      projectConfiguration.environment = parseEnvironment(yaml.environment, yaml.jenkinsEnvironment, env);

      // add Build Number environment variables
      projectConfiguration.environment.add("BUILD_ID=${env.BUILD_ID}");

      // add SCM environment variables
      //projectConfiguration.environment.add("BRANCH_NAME=${env.BRANCH_NAME.replace('origin/','')}");
      projectConfiguration.environment.add("CHANGE_ID=${env.CHANGE_ID}");

      if (env.CHANGE_ID) {
          projectConfiguration.environment.add("CHANGE_BRANCH=${env.CHANGE_BRANCH}");
          projectConfiguration.environment.add("CHANGE_TARGET=${env.CHANGE_TARGET}");
      }

      // parse the execution steps
//      projectConfiguration.steps = parseSteps(yaml.steps);

      // parse the necessary services
//      projectConfiguration.services = parseServices(yaml.services);

      // load the dockefile
//      projectConfiguration.dockerfile = parseDockerfile(yaml.config);

      // load the project name
      projectConfiguration.projectName = parseProjectName(currentProject);
      projectConfiguration.sourceProject = currentProject.sourceProject;
      projectConfiguration.lambdaName = (currentProject.lambdaName) ? currentProject.lambdaName : "";
      projectConfiguration.withDeploy = currentProject.withDeploy;

      projectConfiguration.env = env;

      //projectConfiguration.dockerConfiguration = new DockerConfiguration(projectConfiguration: projectConfiguration);

      projectConfiguration.timeout = currentProject.timeout ?: DEFAULT_TIMEOUT;

      projectConfiguration.dockerConfiguration = (currentProject.dockerConfiguration)? currentProject.dockerConfiguration : new DockerConfiguration(imageName : "", containerLocalPort : 0);

      projectConfiguration.artifactConfiguration = currentProject.artifactConfiguration;
      
      projectConfiguration.folderDeploy = currentProject.folderDeploy;

      projectConfiguration.cleanOnlyFilesFolderDeploy = currentProject.cleanOnlyFilesFolderDeploy;

      projectConfiguration.commandsAtInit = (currentProject.commandsAtInit) ? currentProject.commandsAtInit : [];
      
      projectConfiguration.commandsAfterBuild = (currentProject.commandsAfterBuild) ? currentProject.commandsAfterBuild : [];

      projectConfiguration.deployConfiguration = (currentProject.deployConfiguration) ? currentProject.deployConfiguration : new DeployConfiguration();
      
      projectConfiguration.buildConfiguration = (currentProject.buildConfiguration) ? currentProject.buildConfiguration : new BuildConfiguration();

      return projectConfiguration;
  }

  static def parseEnvironment(def environment, def jenkinsEnvironment, def env) {
    def config = [];

    if (environment) {
        config += environment.collect { k, v -> "${k}=${v}"};
    }

    if (jenkinsEnvironment) {
        config += jenkinsEnvironment.collect { k -> "${k}=${env.getProperty(k)}"};
    }

    return config;
  }

  static def parseProjectName(def config) {
    if (!config || !config.projectName) {
        return "sinnombre-project";
    }

    return config.projectName;
  }
}