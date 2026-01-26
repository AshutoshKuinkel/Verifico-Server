package com.verifico.server.comment.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.verifico.server.comment.Comment;
import com.verifico.server.comment.CommentRepository;
import com.verifico.server.comment.CommentService;
import com.verifico.server.comment.dto.CommentRequest;
import com.verifico.server.comment.dto.CommentResponse;
import com.verifico.server.post.Category;
import com.verifico.server.post.Post;
import com.verifico.server.post.PostRepository;
import com.verifico.server.post.Stage;
import com.verifico.server.user.User;
import com.verifico.server.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
  @Mock
  CommentRepository commentRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  PostRepository postRepository;

  @Mock
  SecurityContext securityContext;

  @Mock
  Authentication authentication;

  @InjectMocks
  CommentService commentService;

  // returning mock securityContext object:
  @BeforeEach
  void setup() {
    SecurityContextHolder.setContext(securityContext);
  }

  private CommentRequest validCommentRequest() {
    CommentRequest commentRequest = new CommentRequest();

    commentRequest.setContent("Wohoo! Congratulations on your acheivement!");

    return commentRequest;
  };

  private User mockUser() {
    User user = new User();
    user.setId(1L);
    user.setUsername("JohnDoe123");
    user.setEmail("johndoe2@gmail.com");
    user.setPassword("hashedPass");
    return user;
  }

  private Post mockPost() {
    Post post = new Post();
    List<String> screenshots = new ArrayList<>(Arrays.asList("FIRST PIC", "SECOND PIC"));
    post.setId(1L);
    post.setAuthor(mockUser());
    post.setTitle("Test Post");
    post.setTagline("Test Tagline");
    post.setCategory(Category.AI);
    post.setStage(Stage.DEVELOPMENT);
    post.setProblemDescription("Test problem");
    post.setSolutionDescription("Test solution");
    post.setScreenshotUrls(screenshots);
    post.setLiveDemoUrl("FIND HERE ON WWW.GOOGLE.COM");
    post.setCreatedAt(Instant.now());
    post.setUpdatedAt(Instant.now());
    return post;
  }

  private Comment mockComment(User author, Post post, String content) {
    Comment comment = new Comment();
    comment.setId(1L);
    comment.setPost(post);
    comment.setAuthor(author);
    comment.setContent(content);
    comment.setCreatedAt(Instant.now());
    comment.setMarkedHelpful(false);
    return comment;
  }

  // create comment (authenticated user JWT but not in db,post id not found,
  // comment successfully made)
  @Test
  void userNotLoggedInDBTryingToComment() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    Post post = mockPost();

    when(postRepository.findById(1L)).thenReturn(Optional.of(post));
    when(userRepository.findByUsername("JohnDoe123")).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> commentService.postComment(validCommentRequest(), 1L));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    assertEquals("User data inconsistency detected", ex.getReason());
  }

  @Test
  void postIdNotFoundWhenAddingComment() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");
    when(postRepository.findById(4L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> commentService.postComment(validCommentRequest(), 4L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertEquals("Post not found", ex.getReason());

    verify(commentRepository, never()).save(any());
  }

  @Test
  void commentSuccessfullyMade() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    User user = mockUser();
    Post post = mockPost();
    CommentRequest request = validCommentRequest();

    when(userRepository.findByUsername("JohnDoe123")).thenReturn(Optional.of(user));
    when(postRepository.findById(1L)).thenReturn(Optional.of(post));

    Comment savedComment = mockComment(user, post, request.getContent());
    when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

    CommentResponse response = commentService.postComment(request, 1L);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals(request.getContent(), response.content());
    assertEquals(user.getUsername(), response.author().getUsername());
    assertEquals(post.getId(), response.postId());

    verify(commentRepository).save(any(Comment.class));
  }

  // get All comments for a posting (post id not found,successfully fetched all
  // comments)
  @Test
  void postIdNotFoundWhenFetchingAllComments() {
    when(postRepository.findById(4L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> commentService.getAllCommentsForPost(4L, 0, 15));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertEquals("Post not found", ex.getReason());

    verify(commentRepository, never()).save(any());
  }

  @Test
  void successfullyFetchedAllCommentsForPost() {
    Post post = mockPost();
    User user = mockUser();
    Comment comment = mockComment(user, post, "w post");

    when(postRepository.findById(1L)).thenReturn(Optional.of(post));

    List<Comment> commentList = List.of(comment);

    Page<Comment> commentPage = new PageImpl<>(commentList);

    when(commentRepository.findAllByPostIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class))).thenReturn(commentPage);

    Page<CommentResponse> response = commentService.getAllCommentsForPost(1L, 0, 15);

    assertNotNull(response);
    assertEquals(1, response.getContent().size());

    CommentResponse commentResponse = response.getContent().get(0);

    assertEquals(comment.getId(), commentResponse.id());
    assertEquals(comment.getContent(), commentResponse.content());
    assertEquals(user.getUsername(), commentResponse.author().getUsername());
    assertEquals(post.getId(), commentResponse.postId());
    assertEquals(comment.getCreatedAt(), commentResponse.createdAt());
  }

  // delete comment (unauthenticated user tries to delete comment, authenticated
  // user tries to delete someone elses comment, comment id not
  // found,successfully deleted comment )

  @Test
  void unauthenticatedUserTriesToDeleteComment() {
    when(securityContext.getAuthentication()).thenReturn(null);

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> commentService.deleteMyComment(1L));

    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    assertEquals("User not authenticated", ex.getReason());

    verify(commentRepository, never()).findById(any());
    verify(commentRepository, never()).delete(any());
  }

  @Test
  void commentIdNotFoundWhenDeleting() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    when(commentRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> commentService.deleteMyComment(99L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertEquals("Comment not found!", ex.getReason());
    verify(commentRepository, never()).delete(any());
  }

  @Test
  void authenticatedUserTriesToDeleteSomeoneElsesComment() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    User commentAuthor = new User();
    commentAuthor.setId(2L);
    commentAuthor.setUsername("JaneSmith456");
    commentAuthor.setEmail("janesmith@gmail.com");

    Post post = mockPost();
    Comment comment = mockComment(commentAuthor, post, "Someone else's comment");

    when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> commentService.deleteMyComment(1L));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    assertEquals("You are not authorised to make changes to this comment!", ex.getReason());
    verify(commentRepository, never()).delete(any());
  }

  @Test
  void commentSuccessfullyDeleted() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    User user = mockUser();
    Post post = mockPost();
    Comment comment = mockComment(user, post, "This is my comment");

    when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

    commentService.deleteMyComment(1L);

    verify(commentRepository).delete(comment);
  }

}
