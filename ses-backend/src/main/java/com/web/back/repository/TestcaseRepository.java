package com.web.back.repository;

import java.util.List;

import com.web.back.model.Problem;
import com.web.back.model.Testcase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestcaseRepository extends JpaRepository<Testcase, Long> {
	List<Testcase> findByProblemId(Long problemId);

	List<Testcase> findByAssignmentId(Long assignmentId);
}