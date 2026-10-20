package com.web.back.repository;

import com.web.back.model.Submit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmitRepository extends JpaRepository<Submit, Long> {
    List<Submit> findAllByMemberId(Long memberId);
    List<Submit> findAllByAssignmentId(Long assignmentId);
    Submit findByAssignmentIdAndMemberId(Long assignmentId, Long memberId);
    List<Submit> findAllByAssignmentIdAndMemberId(Long assignmentId, Long memberId);
    List<Submit> findAllByProblemIdAndMemberId(Long problemId, Long memberId);
    Submit findByProblemIdAndMemberId(Long problemId, Long memberId);
    Submit findByProblemIdAndMemberIdAndLanguage(Long problemId, Long memberId, int language);
}
