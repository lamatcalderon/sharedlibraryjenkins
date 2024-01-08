package com.sreasons.tools

import com.sreasons.exceptions.InvalidArgumentException

class DockerTools implements Serializable{

  protected String environment
  def script

  protected String fullPathRegistry

  /*
  * Docker image name
  */
  String image
  String buildIdAsString
  String url
  String sourceProject
  /*
  * Variable usado para docker build sin https / http
  */
  String _url


  String registryCredentials = ""
  
  //String REGISTRY_CREDENTIALS = 'nexus-credentials'

  DockerTools(script, Map args = [:]){
    this.script = script
    this.environment = this.script.env.ENV 
    this.script.echo """
    **********************************************************************************************
    DockerTools
    environment : ${environment}
    **********************************************************************************************
    """

    if (args.containsKey('imageName')){
      this.image = args['imageName']
    }
    if (args.containsKey('buildIdAsString')){
      this.buildIdAsString = args['buildIdAsString']
    }
    if (args.containsKey('url')){
      this.url = args['url']
    }
    if (this.url) {
      if (!this.url.endsWith('/')) {
          this.url += '/'
      }
      this.url = this.url.toLowerCase()

      if (this.url.startsWith('https://')) {
          this._url = this.url.substring(8)
      } else if (this.url.startsWith('http://')) {
          this._url = this.url.substring(7)
      } else {
          this._url = this.url
      }
    }
    if (args.containsKey('sourceProject')){
      this.sourceProject = args['sourceProject'];
    }

    this.registryCredentials = "ecr:us-west-2:sr-registry-credential-${environment}" 
    
  }

  void buildDockerImage(){

    this.script.dir(this.sourceProject){
      String fullImageName = this.getFullImageName(script.currentBuild.id)
      script.sh "docker build -t ${fullImageName} -f Dockerfile ."

      String fullImageNameLatest = this.getFullImageName("latest")
      script.sh "docker tag ${fullImageName} ${fullImageNameLatest}"
    }
  }

  void publishDockerImage(){
    script.docker.withRegistry(this.url, "${registryCredentials}") {
      script.sh "docker push ${getFullImageName(script.currentBuild.id)}"
      script.sh "docker push ${getFullImageName("latest")}"
    }
  }
  
  String getFullImageName(String tag) throws InvalidArgumentException {
      // validate arguments
      if (!this.image) {
          throw new InvalidArgumentException('image')
      }
      if (!tag) {
          throw new InvalidArgumentException('tag')
      }
      return "${this._url}${this.image}:${tag}".toString()
  }
}
