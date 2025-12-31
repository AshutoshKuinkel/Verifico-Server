package com.verifico.server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
  
  @PostMapping("/register")
  public String Register(){
    return "Register endpoint";
  }

  @PostMapping("login")
  public String Login(){
    return "Login endpoint";
  }
}
