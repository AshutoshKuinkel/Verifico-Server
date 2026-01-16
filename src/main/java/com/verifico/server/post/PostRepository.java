package com.verifico.server.post;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
  Optional<Post> findById(Long id);

  Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<Post> findByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);

  // this is our filtering logic for the search bar, where users can search for
  // posts. I am thinking of allowing for matching keyword/phrase user searches to
  // look into title,tagline,problem/solution description. Also, this is a task
  // for later but idk why companies aren't stored when making posts... e.g so
  // people are able to filter by/search for companies aswell. Anyways I read
  // about
  // this and realised there are 2 good ways to approach this, one being JPA
  // specification and the other being criteria queries. Criteria queries is lower
  // level and requires more boiler plate, but the logic was easier for me to pick
  // up. On the other hand, our JPA specification removes the need for boiler
  // plate, and uses the criteria API which simplifies our process. Since I don't
  // really have complex sql joins across multiple entitires etc as of now, and I
  // don't need very specific control over query structure I'll go with JPA
  // specification. But I may need to come back to this later and change it up to
  // criteria queries.
  default void postSpecification() {

  }

}
