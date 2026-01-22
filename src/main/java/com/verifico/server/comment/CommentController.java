package com.verifico.server.comment;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.verifico.server.comment.dto.CommentRequest;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

  @PostMapping("/create/{id}")
  public void createComment(@PathVariable("id") Long id, CommentRequest request) {

  }

  @GetMapping("/{id}")
  public void fetchAllCommentsForPost(@PathVariable("id") Long id) {

  }

  @DeleteMapping("/{id}")
  public void deleteComment(@PathVariable("id") Long id) {

  }
}
