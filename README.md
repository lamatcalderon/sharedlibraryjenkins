### Projecto de librerias compartidas para Jenkins Pipelines



Listado de librerias 

- Haskell Dockerfile Linter   https://github.com/hadolint/hadolint

Integracion con Servidores 
- Hashicorp Vault


Plugins Jenkinsfile

- Mask Passwords Plugin



Listado de secretos en Jenkins

- sr-registry-credential-dev : credencial en vault de jenkins para la conexion con el registry de nexus
- sr-registry-credential-crt : credencial de vault de jenkins para la conexion con el registry de Amazon Container registry
- sr-registry-credential-prd : credencial de vault de jenkins para la conexion con el registry de Amazon Container registry

- sr-apiscontainer-credential-dev : credencial para usuario ssh para el servidor linux contenedores ambiente desarrollo

- sr-apiswindows-credential-dev : credencial para usuario ssh para servidor windows aplicaciones net framework ambiente desarrollo
- sr-apiswindows-credential-crt : credencial para usuario ssh para servidor windows aplicaciones net framework ambiente certificacion
- sr-apiswindows-credential-prd : credencial para usuario ssh para servidor windows aplicaciones net framework ambiente produccion



Estructura de archivos en relacion a los jenkins a ejecutar
* OPCION 1 - un archivo Jenkinsfile por cada entorno
folder
-> jenkins
   -> Jenkinsfile-dev.groovy
   -> Jenkinsfile-crt.groovy
   -> Jenkinsfile-prd.groovy

* OPCION 2 - un archivo unico en cuyo interior esta parametrizado los entornos
folder
-> Jenkinsfile