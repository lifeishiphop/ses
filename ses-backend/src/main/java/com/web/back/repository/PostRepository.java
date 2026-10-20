package com.web.back.repository;

import com.web.back.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByBoardId(Long boardId);

    @Query(value = "SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<Post> findAllSearch(String keyword);

    @Query(value = "SELECT p FROM Post p WHERE p.board.id = :boardId and p.title LIKE %:keyword% OR p.board.id = :boardId and p.content LIKE %:keyword%")
    Page<Post> searchCategoryPost(Long boardId, String keyword, Pageable pageable);

//    @Query(value = "SELECT p FROM Post p WHERE p.board.id = :boardId and p.title LIKE %:keyword% OR p.board.id = :boardId and p.content LIKE %:keyword%")
//    List<Post> searchCategoryPost(Long boardId, String keyword);
}
