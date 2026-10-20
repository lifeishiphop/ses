package com.web.back.service;

import com.web.back.dto.AssignmentDto;
import com.web.back.dto.StudentDataDto;
import com.web.back.dto.SubmitDto;
import com.web.back.dto.TestcaseDto;

import java.util.List;

public interface AssignmentService {

    List<AssignmentDto> getAssignmentList(Long lessonId) throws Exception;

//    List<AssignmentDto> getMyAssignmentList(Long lessonId) throws Exception;

    AssignmentDto getAssignmentDetail(Long assignmentId) throws Exception;

    AssignmentDto addAssignment(AssignmentDto assignmentDto) throws Exception;

    AssignmentDto editAssignment(Long assignmentId, AssignmentDto assignmentDto) throws Exception;

    void deleteAssignment(Long assignmentId) throws Exception;

    SubmitDto runAssignmentCode(Long assignmentId, SubmitDto submitDto) throws Exception;

    SubmitDto submitAssignmentCode(Long assignmentId, SubmitDto submitDto) throws Exception;

    List<StudentDataDto> getMyAssignmentScoreList(Long assignmentId, Long memberId) throws Exception;

    List<StudentDataDto> getAssignmentScoreList(Long assignmentId) throws Exception;

    SubmitDto getAssignmentScoreDetail(Long submitId) throws Exception;

    SubmitDto editAssignmentScoreDetail(Long submitId, SubmitDto submitDto) throws Exception;

    TestcaseDto addAssignmentTestcase(TestcaseDto testcaseDto) throws  Exception;

    TestcaseDto editAssignmentTestcase(Long testcaseId, TestcaseDto testcaseDto) throws  Exception;

    void deleteAssignmentTestcase(Long testcaseId) throws Exception;

    List<TestcaseDto> getAssignmentTestcaseList(Long assignmentId) throws  Exception;

}
