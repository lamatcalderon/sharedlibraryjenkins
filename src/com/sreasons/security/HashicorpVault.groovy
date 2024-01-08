package com.sreasons.security

class HashicorpVault implements Serializable {

  protected String environment
  private Script script
  private def urlHV = [dev: "http://vaultdev.sreasons.com:8200", crt: "https://vault.sreasons-crt.internal:8200", prd: "http://vault.sreasons.internal:8200"]

  
  public final static String VAULT_SECRET_TYPE_USERNAME_PASSWORD = "username-password"
  public final static String VAULT_SECRET_TYPE_SECRET_TEXT = "secret-text"

  public final static String VAULT_SECRET_KEY_SECRET = "secret"
  public final static String VAULT_SECRET_KEY_USERNAME = "username"
  public final static String VAULT_SECRET_KEY_PASSWORD = "password"



  public HashicorpVault(script, String credentialUseParam){

    this.script = script
    this.environment = this.script.env.ENV 
  }

  public void withHVCredential(ArrayList credentialList, Closure body){
    
    this.script.steps.echo "Inside withHVCredential"

    ArrayList envContextList = new ArrayList()
    ArrayList keyPairList = new ArrayList()

    def path = "kv-v2/data/${this.script.env.ENV}"

    getDinamicToken([[tokenValue: "dynamicToken"]]){

      for(Map credentialsArray: credentialList){
        String vaultCredentialPath = credentialsArray.get('vaultCredentialPath')
        String vaultCredentialType = credentialsArray.get('vaultCredentialType')

        this.script.steps.echo "Hashicorp Vault Credencial que se utilizara: ${vaultCredentialPath}"

        def finalPath = "${urlHV[environment]}/v1/${path}/${vaultCredentialPath}"

        String apiSecretCall = """curl -s --header 'X-Vault-Token: ${script.env.dynamicToken}' ${finalPath} """

        String vaultResponse = callHashicorpVaultApi(apiSecretCall, "", "Error al obtener la credencial: ${vaultCredentialPath}")
        
        switch(vaultCredentialType) {
          case VAULT_SECRET_TYPE_SECRET_TEXT:
            String secretVariable = credentialsArray.get(VAULT_SECRET_KEY_SECRET)
            addValue(vaultResponse, envContextList, keyPairList, VAULT_SECRET_KEY_SECRET, secretVariable )             
            break
          case VAULT_SECRET_TYPE_USERNAME_PASSWORD:
            String userNameVariable = credentialsArray.get(VAULT_SECRET_KEY_USERNAME)
            String passwordVariable = credentialsArray.get(VAULT_SECRET_KEY_PASSWORD)
            addValue(vaultResponse, envContextList, keyPairList, VAULT_SECRET_KEY_USERNAME, userNameVariable)             
            addValue(vaultResponse, envContextList, keyPairList, VAULT_SECRET_KEY_PASSWORD, passwordVariable)             
        }

      }
    }

    this.script.steps.wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: keyPairList]){
      this.script.steps.withEnv(envContextList){
        body.call()
      }
    }
  }

  @NonCPS
  private AppRol getAppRol(){
    
    AppRol resp

    def jenkinsCredentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
      com.cloudbees.plugins.credentials.Credentials.class,
      Jenkins.instance,
      hudson.security.ACL.SYSTEM,
      null
    )

    String target_id_approle = "sr-idapprole-hashicorp-${environment}"
    for (def creds in jenkinsCredentials){
      if(creds.id == target_id_approle){
        resp = new AppRol()
        resp.roleId = creds.roleId
        resp.secretId = creds.secretId
      }
    }

    if (resp == null || resp.roleId.isEmpty() || resp.secretId.isEmpty()){
      throw new Exception("Error al obtener el AppRole del Vault de Jenkins")
    }

    return resp    
  }
  
  private void getDinamicToken(ArrayList credentialsList, Closure body){

    AppRol appRol = getAppRol()
    String roleId = appRol.roleId
    String secretId = appRol.secretId

    String dynamicToken = null
    String loginToken = ""

    this.script.steps.wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: secretId, var: 'secretIdVariable'], [password: roleId, var: 'roleIdVariable']]]){

      String apiCall = """curl -s --request POST --data '{"role_id": "${roleId}", "secret_id": "${secretId}"}' ${urlHV[environment]}/v1/auth/approle/login"""
      loginToken = callHashicorpVaultApi(apiCall, ".auth.client_token", "Error al obtener el token desde hashicorp") 

    }

    String policy = "jenkins"

    this.script.steps.wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: loginToken, var: 'loginTokenVariable']]]){

      String apiDynamicTokenCall = """curl -s --header "X-Vault-Token: ${loginToken}" --request POST --data '{"policies": ["${policy}"], "type": "batch", "ttl": "2m"}' ${urlHV[environment]}/v1/auth/token/create"""
      dynamicToken = callHashicorpVaultApi(apiDynamicTokenCall, ".auth.client_token", "Error al obtner el token dinamico desde hashicorp")

    }

    HashMap tempCredentials = new HashMap()

    for(Map credentialsArray : credentialsList){
      String tokenValue = credentialsArray.get('tokenValue')
      tempCredentials.put("${tokenValue}", "${dynamicToken}")
    }

    ArrayList enviromentContextList = new ArrayList()
    ArrayList passwordPairList = new ArrayList()

    tempCredentials.each {
      key, value ->
        enviromentContextList.add("$key=$value")
        HashMap passwordPair = new HashMap()
        passwordPair.put("password", "$value")
        passwordPair.put("var", "${key}Var")
        passwordPairList.add(passwordPair)
    }

    this.script.steps.wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: passwordPairList]){
      this.script.steps.withEnv(enviromentContextList){
        body.call()
      }
    }
  }


  private String callHashicorpVaultApi(String callApi, String valueResponse, String msgError){
    
    String vaultResponse = ""

    vaultResponse = this.script.steps.sh(script: """${callApi}""", returnStdout: true).trim()

    String error = this.script.steps.sh(script: """jq '.errors' <<< '${vaultResponse}' --raw-output | tr '\n' ' '""", returnStdout: true).trim()

    if(error != "null" && !error.isEmpty()){
      throw new Exception("ERROR: ${msgError}, Error: ${error} ")
    }

    if (!valueResponse.isEmpty()){
      vaultResponse = this.script.steps.sh(script:"""jq '${valueResponse}' --raw-output <<< '${vaultResponse}'""", returnStdout:true).trim() 
    }

    return vaultResponse
  }

  private void addValue(String vaultResponse, ArrayList environmentContextList, ArrayList keyPairList, String key, String environmentValue){
    String secretValue = this.script.steps.sh(script: """jq '.data.data.${key}' --raw-output <<< '${vaultResponse}'""", returnStdout: true).trim()
    HashMap keyPair = new HashMap()
    keyPair.put("password", "${secretValue}")
    keyPair.put("var", "${environmentValue}Var")
    keyPairList.add(keyPair)
    environmentContextList.add("${environmentValue}=${secretValue}")
  }
}
