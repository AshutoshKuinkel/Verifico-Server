package com.verifico.server.comment;

import org.springframework.stereotype.Service;

import com.verifico.server.comment.dto.CommentRequest;
import com.verifico.server.post.PostRepository;

// Create, read, delete operations only for now.
// We won't feature edit/update comments initially.
@Service
public class CommentService {

  private CommentRepository commentRepository;

  private PostRepository postRespository;

  public CommentService(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  public void postComment(CommentRequest request) {
    
  }

  public void getAllCommentsForPost() {

  }

  public void deleteMyComment() {

  }

}
