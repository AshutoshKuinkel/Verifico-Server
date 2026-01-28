package com.verifico.server.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.verifico.server.user.dto.PublicUserResponse;
import com.verifico.server.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public UserResponse meEndpoint() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to find authenticated user");
    }
    String username = auth.getName();

    User user = userRepository.findByUsername(username).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
            "Unable to find user associated with that username."));

    return toUserResponse(user);
  }

  public PublicUserResponse viewSomebodiesProfile(Long id) {
    // no need authentication or anything to view some body elses profile
    // just find by id and return the public user response..

    User user = userRepository.findById(id).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "A user with that associated id couldn't be found"));

    return toPublicUserResponse(user);
  }

  public UserResponse toUserResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getBio(),
        user.getAvatarUrl(),
        user.getJoinedDate());
  }

  public PublicUserResponse toPublicUserResponse(User user) {
    return new PublicUserResponse(
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getBio(),
        user.getAvatarUrl(),
        user.getJoinedDate());
  }
}
