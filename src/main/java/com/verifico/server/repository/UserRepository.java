package com.verifico.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.verifico.server.model.User;

public interface UserRepository extends JpaRepository<User,Long> {
  
}
