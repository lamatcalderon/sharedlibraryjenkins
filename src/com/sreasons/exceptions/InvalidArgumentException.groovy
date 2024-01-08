package com.sreasons.exceptions

class InvalidArgumentException extends Exception {

  final String argument

  InvalidArgumentException(String argument, String message = ''){
    super(message ? message : "Argument \"${argument}\" is not provided or invalid")
    this.argument = argument
  }
}