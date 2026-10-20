package com.web.back.repository;

import com.web.back.model.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository  extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByMemberId(Long ownerId);

    @Query(value = "SELECT l FROM Lesson l WHERE l.name LIKE %:keyword%")
    Page<Lesson> findAllSearch(@Param("keyword") String keyword, Pageable pageable);

//    @Query(value = "SELECT l FROM Lesson l WHERE l.name LIKE %:keyword%")
//    List<Lesson> findAllSearch(String keyword);

}
