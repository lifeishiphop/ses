package com.web.back.service.impl;

import com.web.back.dto.*;
import com.web.back.model.*;
import com.web.back.repository.*;
import com.web.back.service.AccountService;
import com.web.back.service.AssignmentService;
import com.web.back.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Autowired
    private JudgeService judgeService;

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private SubmitRepository submitRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TestcaseRepository testcaseRepository;

    @Autowired
    private AccountService accountService;

    // 과제 목록 조회
    @Override
    public List<AssignmentDto> getAssignmentList(Long lessonId) throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();

        if (authenticatedMember.getIdentity() == 1) {
            List<Assignment> assignments = assignmentRepository.findAllByLessonId(lessonId);

            List<AssignmentDto> assignmentDtos = new ArrayList<>();
            assignments.forEach(s ->{
                Submit submits = submitRepository.findByAssignmentIdAndMemberId(s.getId(), authenticatedMember.getId());
                String submitState = "미제출";
                if (submits != null) {
                    submitState = "제출완료";
                }
                assignmentDtos.add(AssignmentDto.setFormat(s, assignments.indexOf(s) + 1, submitState));
            });

            return assignmentDtos;
        }
        else {
            int numOfStudent = countStudent(studentRepository.findMyStudent(lessonId));
            List<Assignment> assignments = assignmentRepository.findAllByLessonId(lessonId);
            List<AssignmentDto> assignmentDtos = new ArrayList<>();
            assignments.forEach(s -> {
                int numOfSubmitStudent = countStudent(submitRepository.findAllByAssignmentId(s.getId()));
                assignmentDtos.add(AssignmentDto.setFormat(s, assignments.indexOf(s) + 1, numOfSubmitStudent, numOfStudent));
            });

            return assignmentDtos;
        }
    }

    public int countStudent(List students) {
        return students.size();
    }

    // 과제 상세 조회
    @Override
    public AssignmentDto getAssignmentDetail(Long assignmentId) throws Exception {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 강의실을 찾을 수 없습니다.");
        });

        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        Submit submit = submitRepository.findByAssignmentIdAndMemberId(assignmentId, memberId);

        String submitContents = null;
        AssignmentDto assignmentDto = AssignmentDto.setFormat(assignment);

        if (submit != null) {
            assignmentDto.setSubmit_contents(submit.getSubmit_contents());
            assignmentDto.setSubmit_language(submit.getLanguage());
            assignmentDto.setScore(submit.getScore());
        }

        return assignmentDto;
    }

    // 과제 등록
    @Override
    public AssignmentDto addAssignment(AssignmentDto assignmentDto) throws Exception {
        Assignment assignment = new Assignment();
        assignment.setTitle(assignmentDto.getTitle());
        assignment.setDescription(assignmentDto.getDescription());
        assignment.setStart_time(LocalDateTime.now());
        // assignment.setStart_time(assignmentDto.getStart_time());
        assignment.setEnd_time(assignmentDto.getEnd_time());
        assignment.setLanguage(assignmentDto.getLanguage());

        assignment.setLesson(lessonRepository.findById(assignmentDto.getLessonId()).orElseThrow(() -> {
            return new IllegalArgumentException("해당 강의실을 찾을 수 없습니다.");
        }));

        assignmentRepository.save(assignment);

        return AssignmentDto.setFormat(assignment);
    }

    // 과제 수정
    @Override
    public AssignmentDto editAssignment(Long assId, AssignmentDto assignmentDto) throws Exception {
        Assignment assignment = assignmentRepository.findById(assId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 문제를 찾을 수 없습니다.");
        });
        assignment.setTitle(assignmentDto.getTitle());
        assignment.setDescription(assignmentDto.getDescription());
//        assignment.setStart_time(assignmentDto.getStart_time());
        assignment.setEnd_time(assignmentDto.getEnd_time());
        assignment.setLanguage(assignmentDto.getLanguage());
        // 나중에 수정
//        assignment.setLesson(lessonRepository.findById(assignmentDto.getLessonId()).orElseThrow(() -> {
//            return new IllegalArgumentException("해당 강의실을 찾을 수 없습니다.");
//        }));

        assignmentRepository.save(assignment);

        return AssignmentDto.setFormat(assignment);
    }

    // 과제 삭제
    @Override
    public void deleteAssignment(Long assId) throws Exception {
        Assignment assignment = assignmentRepository.findById(assId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 문제를 찾을 수 없습니다.");
        });
        assignmentRepository.delete(assignment);
    }

    // 과제 코드 실행
    @Override
    public SubmitDto runAssignmentCode(Long assignmentId, SubmitDto submitDto) throws Exception {
        if (submitDto.getAssignment_id() != null && !assignmentId.equals(submitDto.getAssignment_id())) {
            throw new IllegalArgumentException("경로와 본문의 과제 ID가 일치하지 않습니다.");
        }
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() ->
                new IllegalArgumentException("해당 과제를 찾을 수 없습니다."));
        submitDto.setAssignment_id(assignmentId);
        submitDto.setProblem_id(null);
        submitDto.setMember_id(accountService.getAuthenticatedMember().getId());
        return judgeService.grade(assignment.getLanguage(), submitDto,
                testcaseRepository.findByAssignmentId(assignmentId), "테스트 할 수 없는 과제입니다.");
    }

    // 과제 코드 제출
    @Override
    public SubmitDto submitAssignmentCode(Long assignmentId, SubmitDto submitDto) throws Exception {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 문제를 찾을 수 없습니다.");
        });

        // 지금 날짜랑 마감일 비교해서 마감일 지났으면 제출 못하게 예외 throw
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime endTime = assignment.getEnd_time();

        if (currentDate.isAfter(endTime)) {
            throw new IllegalStateException("과제 마감일이 지났습니다. 더이상 제출할 수 없습니다.");
        }

        SubmitDto newSubmit = runAssignmentCode(assignmentId, submitDto);
        Member member = accountService.getAuthenticatedMember();
        Submit submit;
        // 이미 제출 정보가 존재하면 덮어쓰기
        submit = submitRepository.findByAssignmentIdAndMemberId(assignmentId, member.getId());
        if (submit == null) {
            submit = new Submit();
        }

        submit.setMember(member);
        submit.setLanguage(newSubmit.getLanguage());
        // 과제의 제출 결과는 제출 여부를 표시
        submit.setResult("제출되었습니다.");
        // 실제 과제 점수
        submit.setScore(newSubmit.getScore());
        submit.setAssignment(assignment);
        submit.setSubmit_contents(newSubmit.getSubmit_contents());
        submit.setSubmit_time(newSubmit.getSubmit_time());

        submitRepository.save(submit);

        return SubmitDto.setFormat(submit);
    }

    // 멤버별 과제 채점 결과 조회
    @Override
    public List<StudentDataDto> getMyAssignmentScoreList(Long assignmentId, Long memberId) throws Exception {
        List<Submit> submits = submitRepository.findAllByAssignmentIdAndMemberId(memberId, assignmentId);
        List<SubmitDto> submitDtos = new ArrayList<>();
        List<StudentDataDto> result = new ArrayList<>();
        submits.forEach(s -> {
            StudentDataDto resultData = new StudentDataDto();
            Member member = memberRepository.findByNumber(s.getMember().getNumber()).orElseThrow();
            resultData.setId(s.getId());
            resultData.setName(member.getName());
            resultData.setNumber(member.getNumber());
            resultData.setCode(s.getSubmit_contents());
            resultData.setScore(s.getScore());
            result.add(resultData);
        });

        return result;
    }

    // 과제별 채점 결과 조회
    @Override
    public List<StudentDataDto> getAssignmentScoreList(Long assId) throws Exception {
        List<Submit> submits = submitRepository.findAllByAssignmentId(assId);
        List<StudentDataDto> result = new ArrayList<>();
        submits.forEach(s -> {
            StudentDataDto resultData = new StudentDataDto();
            Member member = memberRepository.findByNumber(s.getMember().getNumber()).orElseThrow();
            resultData.setId(s.getId());
            resultData.setName(member.getName());
            resultData.setNumber(member.getNumber());
            resultData.setCode(s.getSubmit_contents());
            resultData.setScore(s.getScore());
            result.add(resultData);
        });

        return result;
    }

    // 과제 채점 결과 상세 조회
    @Override
    public SubmitDto getAssignmentScoreDetail(Long submitId) throws Exception {
        Submit submit = submitRepository.findById(submitId).orElseThrow();

        return SubmitDto.setFormat(submit);
    }

    // 과제 채점 결과 수정
    @Override
    public SubmitDto editAssignmentScoreDetail(Long submitId, SubmitDto submitDto) throws Exception {
        Submit submit = submitRepository.findById(submitId).orElseThrow();
        submit.setScore(submitDto.getScore());
        submitRepository.save(submit);

        return SubmitDto.setFormat(submit);
    }

    // 과제 테스트케이스 등록
    @Override
    public TestcaseDto addAssignmentTestcase(TestcaseDto testcaseDto) throws  Exception {
        Assignment assignment = assignmentRepository.findById(testcaseDto.getAssignment_id()).orElseThrow(() -> {
            return new IllegalArgumentException("해당 과제를 찾을 수 없습니다.");
        });

        Testcase testcase = new Testcase();
        testcase.setInput(testcaseDto.getInput());
        testcase.setOutput(testcaseDto.getOutput());
        testcase.setAssignment(assignment);
        testcaseRepository.save(testcase);
        return TestcaseDto.setFormat(testcase);
    }

    // 과제 테스트케이스 수정
    @Override
    public TestcaseDto editAssignmentTestcase(Long testcaseId, TestcaseDto testcaseDto) throws  Exception {
        Testcase testcase = testcaseRepository.findById(testcaseId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 테스트 케이스를 찾을 수 없습니다.");
        });
        testcase.setInput(testcaseDto.getInput());
        testcase.setOutput(testcaseDto.getOutput());
        testcaseRepository.save(testcase);
        return TestcaseDto.setFormat(testcase);
    }

    // 과제 테스트케이스 삭제
    @Override
    public void deleteAssignmentTestcase(Long testcaseId) throws Exception {
        Testcase testcase = testcaseRepository.findById(testcaseId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 테스트 케이스를 찾을 수 없습니다.");
        });
        testcaseRepository.delete(testcase);
    }

    // 과제 테스트케이스 조회
    @Override
    public List<TestcaseDto> getAssignmentTestcaseList(Long assignmentId) throws  Exception {
        List<Testcase> testcases = testcaseRepository.findByAssignmentId(assignmentId);
        List<TestcaseDto> testcaseDtos = new ArrayList<>();
        testcases.forEach(s -> testcaseDtos.add(TestcaseDto.setFormat(s)));

        return testcaseDtos;
    }
}
