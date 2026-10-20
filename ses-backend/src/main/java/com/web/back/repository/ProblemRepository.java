package com.web.back.repository;

import com.web.back.model.Testcase;
import com.web.back.model.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    @Query("SELECT p FROM Problem p WHERE LOWER(p.title) LIKE %:keyword% OR LOWER(p.description) LIKE %:keyword%")
    Page<Problem> findAllSearch(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT t FROM Testcase t WHERE t.problem.id = :problemId")
    List<Testcase> findHaveTestcase(Long problemId);

    List<Problem> findAllByMemberId(Long memberId);
}
