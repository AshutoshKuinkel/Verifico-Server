package com.verifico.server.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.verifico.server.model.User;
import com.verifico.server.repository.UserRepository;

@RestController
@RequestMapping("api/users")
public class UserController {

  @Autowired
  private UserRepository userRepository;
  @GetMapping
  public List<User> getAllProfiles() {
    return userRepository.findAll();
  }

  @PostMapping
  public User createProfile(@RequestBody User user){
    return userRepository.save(user);
  }
}
