package com.verifico.server.post.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.verifico.server.post.Category;
import com.verifico.server.post.Post;
import com.verifico.server.post.PostRepository;
import com.verifico.server.post.PostService;
import com.verifico.server.post.Stage;
import com.verifico.server.post.dao.PostSearchDao;
import com.verifico.server.post.dto.PostRequest;
import com.verifico.server.post.dto.PostResponse;
import com.verifico.server.user.User;
import com.verifico.server.user.UserRepository;

// since we don't have any service layer validation for post api
// the unit tests do not feature the missing input/validation checks
// instead we will do those DTO/annotation validation (@NotNull, @Size, etc.) → Test in integration/controller tests.
@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
  @Mock
  PostRepository postRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  PostSearchDao postSearchDao;

  @Mock
  SecurityContext securityContext;

  @Mock
  Authentication authentication;

  @InjectMocks
  PostService postService;

  // returning our mock securityContext object:
  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  private PostRequest validPostRequest() {
    PostRequest postRequest = new PostRequest();
    List<String> screenshots = new ArrayList<>(Arrays.asList("FIRST PIC", "SECOND PIC"));
    postRequest.setTitle("TEST POST");
    postRequest.setTagline("TEST TAGLINE");
    postRequest.setCategory(Category.AI);
    postRequest.setStage(Stage.DEVELOPMENT);
    postRequest.setProblemDescription("FACING PROBLEM");
    postRequest.setSolutionDescription("VERY GOOD SOLUTION");
    postRequest.setScreenshotUrls(screenshots);
    postRequest.setLiveDemoUrl("FIND HERE ON WWW.GOOGLE.COM");

    return postRequest;
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

  // create post test endpoints:
  // user not found/ not authenticated and tries to make post
  // successfull post creation with only required fields, optionals left blank
  // successfull post creation with all fields including optionals
  @Test
  void userNotFoundWhenMakingPost() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");
    when(userRepository.findByUsername("JohnDoe123")).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> postService.createPost(validPostRequest()));

    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    assertEquals("Unable to get username", ex.getReason());

    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  void successfullPostCreationWithOnlyRequiredFields() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    User user = mockUser();
    Post savedPost = mockPost();

    when(userRepository.findByUsername("JohnDoe123")).thenReturn(Optional.of(user));
    when(postRepository.save(any(Post.class))).thenReturn(savedPost);

    PostRequest minimalRequest = new PostRequest();
    minimalRequest.setTitle("TEST POST");
    minimalRequest.setTagline("TEST TAGLINE");
    minimalRequest.setCategory(Category.AI);
    minimalRequest.setStage(Stage.DEVELOPMENT);
    minimalRequest.setProblemDescription("FACING PROBLEM");
    minimalRequest.setSolutionDescription("VERY GOOD SOLUTION");

    PostResponse response = postService.createPost(minimalRequest);
    assertEquals(savedPost.getId(), response.id());
    assertEquals(savedPost.getTitle(), response.title());
    assertEquals(savedPost.getTagline(), response.tagline());
    assertEquals(savedPost.getCategory(), response.category());
    assertEquals(savedPost.getStage(), response.stage());
    assertEquals(savedPost.getProblemDescription(), response.problemDescription());
    assertEquals(savedPost.getSolutionDescription(), response.solutionDescription());

    verify(postRepository, times(1)).save(any(Post.class));
    verify(userRepository, times(1)).save(user);
  }

  @Test
  void successfullPostCreationWithAllFields() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    User user = mockUser();
    Post savedPost = mockPost();

    when(userRepository.findByUsername("JohnDoe123")).thenReturn(Optional.of(user));
    when(postRepository.save(any(Post.class))).thenReturn(savedPost);

    PostResponse response = postService.createPost(validPostRequest());

    assertNotNull(response);
    assertEquals(savedPost.getId(), response.id());
    assertEquals(savedPost.getTitle(), response.title());
    assertEquals(savedPost.getTagline(), response.tagline());
    assertEquals(savedPost.getCategory(), response.category());
    assertEquals(savedPost.getStage(), response.stage());
    assertEquals(savedPost.getProblemDescription(), response.problemDescription());
    assertEquals(savedPost.getSolutionDescription(), response.solutionDescription());
    assertEquals(savedPost.getAuthor().getId(), response.author().getId());
    assertEquals(savedPost.getAuthor().getUsername(), response.author().getUsername());
    assertEquals(savedPost.getScreenshotUrls(), response.screenshotUrls());
    assertEquals(savedPost.getLiveDemoUrl(), response.liveDemoUrl());
    assertEquals(savedPost.isBoosted(), false);

    verify(postRepository, times(1)).save(any(Post.class));
    verify(userRepository, times(1)).save(user);
  }

  // get post by id test endpoints:
  // post not found endpoint
  // successfull post fetch
  @Test
  void getPostByIdNotFound() {
    when(postRepository.findById(4L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> postService.getPostById(4L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertEquals("Post not found", ex.getReason());
  }

  @Test
  void successfullGetPostById() {
    Post savedPost = mockPost();
    when(postRepository.findById(1L)).thenReturn(Optional.of(savedPost));

    PostResponse response = postService.getPostById(1L);

    assertNotNull(response);
    assertEquals(savedPost.getId(), response.id());
    assertEquals(savedPost.getTitle(), response.title());
    assertEquals(savedPost.getTagline(), response.tagline());
    assertEquals(savedPost.getCategory(), response.category());
    assertEquals(savedPost.getStage(), response.stage());
    assertEquals(savedPost.getProblemDescription(), response.problemDescription());
    assertEquals(savedPost.getSolutionDescription(), response.solutionDescription());
    assertEquals(savedPost.getAuthor().getId(), response.author().getId());
    assertEquals(savedPost.getAuthor().getUsername(), response.author().getUsername());
    assertEquals(savedPost.getScreenshotUrls(), response.screenshotUrls());
    assertEquals(savedPost.getLiveDemoUrl(), response.liveDemoUrl());
    assertEquals(savedPost.isBoosted(), false);
  }

  // get all posts test endpoints:
  // get posts without any filters
  // get all posts filtered by category
  // get all posts with search query
  @Test
  void getAllPostsWithoutFilters() {
    Post savedPost = mockPost();
    List<Post> postList = List.of(savedPost);

    Page<Post> postPage = new PageImpl<>(postList);

    when(postRepository.findAllByOrderByCreatedAtDesc(any())).thenReturn(postPage);

    Page<PostResponse> response = postService.getAllPosts(0, 10, null, null);

    assertNotNull(response);
    assertEquals(1, response.getTotalElements());
    assertEquals(savedPost.getId(), response.getContent().get(0).id());
    assertEquals(savedPost.getTitle(), response.getContent().get(0).title());
    assertEquals(savedPost.getTagline(), response.getContent().get(0).tagline());
    assertEquals(savedPost.getCategory(), response.getContent().get(0).category());
    assertEquals(savedPost.getStage(), response.getContent().get(0).stage());
    assertEquals(savedPost.getProblemDescription(), response.getContent().get(0).problemDescription());
    assertEquals(savedPost.getSolutionDescription(), response.getContent().get(0).solutionDescription());
    assertEquals(savedPost.getAuthor().getId(), response.getContent().get(0).author().getId());
    assertEquals(savedPost.getAuthor().getUsername(), response.getContent().get(0).author().getUsername());
    assertEquals(savedPost.getScreenshotUrls(), response.getContent().get(0).screenshotUrls());
    assertEquals(savedPost.getLiveDemoUrl(), response.getContent().get(0).liveDemoUrl());
    assertEquals(savedPost.isBoosted(), false);

  }

  @Test
  void getAllPostsWithCategoryFilter() {
    Post savedPost = mockPost();
    List<Post> postList = List.of(savedPost);

    Page<Post> postPage = new PageImpl<>(postList);

    when(postRepository.findByCategoryOrderByCreatedAtDesc(eq(Category.AI), any())).thenReturn(postPage);

    Page<PostResponse> response = postService.getAllPosts(0, 10, Category.AI, null);

    assertNotNull(response);
    assertEquals(1, response.getTotalElements());
    assertEquals(savedPost.getId(), response.getContent().get(0).id());
    assertEquals(savedPost.getTitle(), response.getContent().get(0).title());
    assertEquals(savedPost.getTagline(), response.getContent().get(0).tagline());
    assertEquals(savedPost.getCategory(), response.getContent().get(0).category());
    assertEquals(savedPost.getStage(), response.getContent().get(0).stage());
    assertEquals(savedPost.getProblemDescription(), response.getContent().get(0).problemDescription());
    assertEquals(savedPost.getSolutionDescription(), response.getContent().get(0).solutionDescription());
    assertEquals(savedPost.getAuthor().getId(), response.getContent().get(0).author().getId());
    assertEquals(savedPost.getAuthor().getUsername(), response.getContent().get(0).author().getUsername());
    assertEquals(savedPost.getScreenshotUrls(), response.getContent().get(0).screenshotUrls());
    assertEquals(savedPost.getLiveDemoUrl(), response.getContent().get(0).liveDemoUrl());
    assertEquals(savedPost.isBoosted(), false);

  }

  @Test
  void getAllPostsWithSearchQuery() {
    Post savedPost = mockPost();
    List<Post> postList = List.of(savedPost);

    Page<Post> postPage = new PageImpl<>(postList);

    when(postSearchDao.searchPosts(eq("JohnDoe123"), any())).thenReturn(postPage);

    Page<PostResponse> response = postService.getAllPosts(0, 10, null, "JohnDoe123");

    assertNotNull(response);
    assertEquals(1, response.getTotalElements());
    assertEquals(savedPost.getId(), response.getContent().get(0).id());
    assertEquals(savedPost.getTitle(), response.getContent().get(0).title());
    assertEquals(savedPost.getTagline(), response.getContent().get(0).tagline());
    assertEquals(savedPost.getCategory(), response.getContent().get(0).category());
    assertEquals(savedPost.getStage(), response.getContent().get(0).stage());
    assertEquals(savedPost.getProblemDescription(), response.getContent().get(0).problemDescription());
    assertEquals(savedPost.getSolutionDescription(), response.getContent().get(0).solutionDescription());
    assertEquals(savedPost.getAuthor().getId(), response.getContent().get(0).author().getId());
    assertEquals(savedPost.getAuthor().getUsername(), response.getContent().get(0).author().getUsername());
    assertEquals(savedPost.getScreenshotUrls(), response.getContent().get(0).screenshotUrls());
    assertEquals(savedPost.getLiveDemoUrl(), response.getContent().get(0).liveDemoUrl());
    assertEquals(savedPost.isBoosted(), false);

    verify(postSearchDao, times(1)).searchPosts(eq("JohnDoe123"), any());
  }

  // update put test endpoints:
  // partial update (only some fields provided)
  // unauthorised user trying to update
  // post not found exception
  // successfull full update
  @Test
  void partialPostUpdate() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    Post savedPost = mockPost();
    when(postRepository.findById(1L)).thenReturn(Optional.of(savedPost));
    when(postRepository.save(any())).thenReturn(savedPost);

    PostRequest partialRequest = new PostRequest();
    partialRequest.setTitle("UPDATED TITLE");

    PostResponse response = postService.updatePostServiceById(1L, partialRequest);

    assertNotNull(response);
    verify(postRepository, times(1)).save(savedPost);
  }

  @Test
  void unAuthorisedUserTryingToUpdatePost() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("GIGGANIGGA");

    Post savedPost = mockPost();

    when(postRepository.findById(1L)).thenReturn(Optional.of(savedPost));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> postService.updatePostServiceById(1L, validPostRequest()));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    assertEquals("You are not authorised to make changes to this post", ex.getReason());

    verify(postRepository, never()).save(any());
  }

  @Test
  void postNotFoundOnPut() {
    when(postRepository.findById(4L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> postService.updatePostServiceById(4L, validPostRequest()));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertEquals("Post not found", ex.getReason());
  }

  @Test
  void successfullUpdatePost() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    Post savedPost = mockPost();

    when(postRepository.findById(1L)).thenReturn(Optional.of(savedPost));
    when(postRepository.save(any(Post.class))).thenReturn(savedPost);

    PostResponse response = postService.updatePostServiceById(1L, validPostRequest());

    assertNotNull(response);
    assertEquals(savedPost.getId(), response.id());
    assertEquals(savedPost.getTitle(), response.title());
    assertEquals(savedPost.getTagline(), response.tagline());
    assertEquals(savedPost.getCategory(), response.category());
    assertEquals(savedPost.getStage(), response.stage());
    assertEquals(savedPost.getProblemDescription(), response.problemDescription());
    assertEquals(savedPost.getSolutionDescription(), response.solutionDescription());
    assertEquals(savedPost.getAuthor().getId(), response.author().getId());
    assertEquals(savedPost.getAuthor().getUsername(), response.author().getUsername());
    assertEquals(savedPost.getScreenshotUrls(), response.screenshotUrls());
    assertEquals(savedPost.getLiveDemoUrl(), response.liveDemoUrl());
    assertEquals(savedPost.isBoosted(), false);

    verify(postRepository, times(1)).save(savedPost);

  }

  // delete post test endpoints:
  // unauthorised user trying to delete post
  // post not found exception
  // successfull post deletion
  @Test
  void unAuthorisedUserTryingToDeletePost() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("GIGGANIGA");

    Post savedPost = mockPost();
    when(postRepository.findById(1L)).thenReturn(Optional.of(savedPost));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> postService.deletePostbyId(1L));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    assertEquals("You are not authorised to make changes to this post", ex.getReason());

    verify(postRepository, never()).deleteById(any());
  }

  @Test
  void postNotFoundOnDelete() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");
    when(postRepository.findById(4L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> postService.deletePostbyId(4L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertEquals("Post not found", ex.getReason());

  }

  @Test
  void successfullDeletePostById() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("JohnDoe123");

    Post savedPost = mockPost();

    when(postRepository.findById(1L)).thenReturn(Optional.of(savedPost));

    postService.deletePostbyId(1L);

    verify(postRepository, times(1)).deleteById(1L);
  }

}
