package com.verifico.server.post.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.verifico.server.post.Post;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

// handle filtering logic here, and inject directly into post
// service. So both post repo and post search dao are in service.
// In getAllPosts(), check if search param exists:
// If yes → call postSearchDao.searchPosts(...)
// If no → call postRepository.findAll...() 
@Repository
public class PostSearchDao {
  private final EntityManager em;

  public PostSearchDao(EntityManager em) {
    this.em = em;
  }

  // all these fields are required when making post, so adding the if (field
  // !=null) check is redundant.
  public Page<Post> searchPosts(String search, Pageable pageable) {

    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaQuery<Post> criteriaQuery = criteriaBuilder.createQuery(Post.class);

    // SELECT * FROM post
    Root<Post> root = criteriaQuery.from(Post.class);

    // Build predicates list
    List<Predicate> predicates = new ArrayList<>();

    if (search != null && !search.isEmpty()) {
      String searchPattern = "%" + search.toLowerCase() + "%";

      // preparing WHERE clause with predicates
      // WHERE title like '%verifiko%'
      Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
          searchPattern);
      Predicate taglinePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("tagline")),
          searchPattern);
      Predicate categoryPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("category")),
          searchPattern);
      Predicate problemDescriptionPredicate = criteriaBuilder.like(
          criteriaBuilder.lower(root.get("problemDescription")),
          searchPattern);
      Predicate solutionDescriptionPredicate = criteriaBuilder.like(
          criteriaBuilder.lower(root.get("solutionDescription")),
          searchPattern);
      Predicate userNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("author").get("username")),
          searchPattern);

      Predicate searchPredicates = criteriaBuilder.or(titlePredicate, taglinePredicate, categoryPredicate,
          problemDescriptionPredicate, solutionDescriptionPredicate, userNamePredicate);
      predicates.add(searchPredicates);

      // final query ==> SELECT * FROM post WHERE title like '%verifiko'
      // or tagline like '%verifiko%' etc...
      criteriaQuery.where(predicates.toArray(new Predicate[0]));
    }

    // get result
    TypedQuery<Post> query = em.createQuery(criteriaQuery);

    // {ALL PAGINATION LOGIC AFTER THIS}
    query.setFirstResult((int) pageable.getOffset()); // make sure we are skipping to right page
    query.setMaxResults(pageable.getPageSize()); // limit results per page
    List<Post> results = query.getResultList(); // execute + get results

    // count query for pagination {total counts for this page, all pages etc..}
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<Post> countRoot = countQuery.from(Post.class);
    countQuery.select(criteriaBuilder.count(countRoot));

    if (search != null && !search.isEmpty()) {
      String searchPattern = "%" + search.toLowerCase() + "%";
      Predicate searchPredicate = criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(countRoot.get("title")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(countRoot.get("tagline")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(countRoot.get("category")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(countRoot.get("problemDescription")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(countRoot.get("solutionDescription")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(countRoot.get("author").get("username")), searchPattern));

      countQuery.where(searchPredicate);
    }

    long total = em.createQuery(countQuery).getSingleResult();

    return new PageImpl<>(results, pageable, total);
  }
}
