package com.verifico.server.auth.token;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
  Optional<RefreshToken> findByToken(String token);
  List<RefreshToken> findByUser_Username(String username);
}
