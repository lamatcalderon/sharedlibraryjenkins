#!/usr/bin/env groovy

def call(List<String> commandsForRun){
  //stage('commands at init') {
      if(isUnix()){
          echo 'run commands on linux/unix'
          commandsForRun.each { val ->        
              sh val
          }
      } else {
          echo 'run commands on windows'
          commandsForRun.each { val ->        
                powershell val
          }
      }
  ///}
}
