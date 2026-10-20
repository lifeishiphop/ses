package com.web.back.service.impl;

import com.web.back.dto.*;
import com.web.back.model.*;
import com.web.back.repository.MemberRepository;
import com.web.back.repository.ProblemRepository;
import com.web.back.repository.SubmitRepository;
import com.web.back.repository.TestcaseRepository;
import com.web.back.service.AccountService;
import com.web.back.service.ProblemService;
import com.web.back.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private JudgeService judgeService;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestcaseRepository testcaseRepository;

    @Autowired
    private SubmitRepository submitRepository;

    @Autowired
    private AccountService accountService;

    // 문제 목록 조회
    @Override
    public List<ProblemDto> getProblemList() throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        List<Problem> problems = problemRepository.findAll();
        List<ProblemDto> problemDtos = new ArrayList<>();

        for (Problem problem : problems) {

            List<Submit> submits = submitRepository.findAllByProblemIdAndMemberId(problem.getId(), memberId);

            String solveStatus = "-";
            if (submits != null && !submits.isEmpty()) {
                submits.sort(Comparator.comparing(Submit::getSubmit_time).reversed());

                Submit submit = submits.get(0);
                solveStatus = "정답입니다.".equals(submit.getResult()) ? "o" : "x";
            }
            problemDtos.add(ProblemDto.setFormat(problem, problems.indexOf(problem) + 1, solveStatus));
        }
        return problemDtos;
    }

    // 출제 문제 목록 조회
    @Override
    public List<ProblemDto> getMyProblemList() throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        List<Problem> problems = problemRepository.findAllByMemberId(memberId);
        List<ProblemDto> problemDtos = new ArrayList<>();

        for (Problem problem : problems) {
            problemDtos.add(ProblemDto.setFormat(problem, problems.indexOf(problem) + 1));
        }
        return problemDtos;
    }

    // 문제 상세 정보 조회
    @Override
    public ProblemDataDto getProblemDetail(Long problemId) throws Exception {
        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 문제를 찾을 수 없습니다.");
        });

        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        // 언어별 풀이 보내기
        ProblemDto problemDto = ProblemDto.setFormat(problem);
        ProblemDataDto sendData = new ProblemDataDto();
        sendData.setProblem(problemDto);


        for(int i = 1; i <= 2; i++) {
            Submit submit = submitRepository.findByProblemIdAndMemberIdAndLanguage(problemId, memberId, i);
            if(submit != null) {
                SubmitDto submitDto = new SubmitDto();
                submitDto.setSubmit_contents(submit.getSubmit_contents());
                submitDto.setLanguage(submit.getLanguage());
                sendData.getSubmits().add(submitDto);
            }
        }

        return sendData;
    }

    // 문제 등록
    @Override
    public ProblemDto registerProblem(ProblemDto problemDto) throws Exception {
        Problem problem = new Problem();
        problem.setTitle(problemDto.getTitle());
        problem.setDescription(problemDto.getDescription());
        problem.setUpload_time(LocalDateTime.now());
        problem.setLanguage(problemDto.getLanguage());

        Member member = accountService.getAuthenticatedMember();
        problem.setMember(member);

        problemRepository.save(problem);

        return ProblemDto.setFormat(problem);
    }

    // 문제 수정
    @Override
    public ProblemDto editProblem(Long problemId, ProblemDto problemDto) throws Exception {

        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 문제를 찾을 수 없습니다.");
        });

        // 문제 등록자와 동일한지 검증
        if (!memberId.equals(problem.getMember().getId())) {
            throw new IllegalAccessException("작성자가 아닙니다.");
        }

        problem.setTitle(problemDto.getTitle());
        problem.setDescription(problemDto.getDescription());
        problem.setUpload_time(LocalDateTime.now());
        problem.setLanguage(problemDto.getLanguage());

        problemRepository.save(problem);

        return ProblemDto.setFormat(problem);
    }

    // 문제 삭제
    @Override
    public void deleteProblem(Long problemId) throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 문제를 찾을 수 없습니다.");
        });

        // 문제 등록자와 동일한지 검증
        if (!memberId.equals(problem.getMember().getId())) {
            throw new IllegalAccessException("작성자가 아닙니다.");
        }

        problemRepository.deleteById(problemId);
    }

    // 문제 검색 (제목 / 내용)
    @Override
    public SearchResultDto searchProblem(String keyword, int page) throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        // 한 페이지에 표시할 contents 수 5로 정의
        int pageSize = 5;
        int pageNum = page - 1;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Problem> problemPage = (Page<Problem>) problemRepository.findAllSearch(keyword, pageable);
        List<Problem> problems = problemPage.getContent();
        int totalPage = problemPage.getTotalPages();

        List<ProblemDto> problemDtos = new ArrayList<>();

        for (Problem problem : problems) {
            List<Submit> submits = submitRepository.findAllByProblemIdAndMemberId(problem.getId(), memberId);

            String solveStatus = "-";
            if (submits != null && !submits.isEmpty()) {
                submits.sort(Comparator.comparing(Submit::getSubmit_time).reversed());
                // 가장 최근에 풀이한 언어 기준
                Submit submit = submits.get(0);
                solveStatus = "정답입니다.".equals(submit.getResult()) ? "o" : "x";
            }
            problemDtos.add(ProblemDto.setFormat(problem, problems.indexOf(problem)+1*(pageNum * pageSize)+1, solveStatus));
        }

        return new SearchResultDto(problemDtos, totalPage);
    }

    // 코드 실행
    @Override
    public SubmitDto runProblemCode(Long problemId, SubmitDto submitDto) throws Exception {
        if (submitDto.getProblem_id() != null && !problemId.equals(submitDto.getProblem_id())) {
            throw new IllegalArgumentException("경로와 본문의 문제 ID가 일치하지 않습니다.");
        }
        Problem problem = problemRepository.findById(problemId).orElseThrow(() ->
                new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));
        submitDto.setProblem_id(problemId);
        submitDto.setAssignment_id(null);
        submitDto.setMember_id(accountService.getAuthenticatedMember().getId());
        return judgeService.grade(problem.getLanguage(), submitDto,
                testcaseRepository.findByProblemId(problemId), "테스트 할 수 없는 문제입니다.");
    }

    // 문제 풀이 제출
    @Override
    public SubmitDto submitProblemCode(Long problemId, SubmitDto submitDto) throws Exception {
        SubmitDto newSubmit = runProblemCode(problemId, submitDto);
        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 문제를 찾을 수 없습니다.");
        });

        Member member = accountService.getAuthenticatedMember();

        Submit submit;
        // 해당 문제의 제출 언어별로 구분하기 위해
        submit = submitRepository.findByProblemIdAndMemberIdAndLanguage(problemId, member.getId(), newSubmit.getLanguage());
        if (submit == null) {
            submit = new Submit();
        }

        submit.setMember(member);
        submit.setLanguage(newSubmit.getLanguage());
        submit.setResult(newSubmit.getResult());
        submit.setScore(newSubmit.getScore());
        submit.setProblem(problem);

        submit.setSubmit_contents(newSubmit.getSubmit_contents());
        submit.setSubmit_time(newSubmit.getSubmit_time());

        submitRepository.save(submit);

        return SubmitDto.setFormat(submit);
    }

    // 문제 테스트케이스 등록
    @Override
    public TestcaseDto addProblemTestcase(TestcaseDto testcaseDto) throws  Exception {
        Problem problem = problemRepository.findById(testcaseDto.getProblem_id()).orElseThrow(() -> {
            return new IllegalArgumentException("해당 문제를 찾을 수 없습니다.");
        });

        Testcase testcase = new Testcase();
        testcase.setInput(testcaseDto.getInput());
        testcase.setOutput(testcaseDto.getOutput());
        testcase.setProblem(problem);
        testcaseRepository.save(testcase);
        return TestcaseDto.setFormat(testcase);
    }

    // 문제 테스트케이스 수정
    @Override
    public TestcaseDto editProblemTestcase(Long testcaseId, TestcaseDto testcaseDto) throws  Exception {
        Testcase testcase = testcaseRepository.findById(testcaseId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 테스트 케이스를 찾을 수 없습니다.");
        });
        testcase.setInput(testcaseDto.getInput());
        testcase.setOutput(testcaseDto.getOutput());
        testcaseRepository.save(testcase);
        return TestcaseDto.setFormat(testcase);
    }

    // 문제 테스트케이스 삭제
    @Override
    public void deleteProblemTestcase(Long testcaseId) throws Exception {
        Testcase testcase = testcaseRepository.findById(testcaseId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 테스트 케이스를 찾을 수 없습니다.");
        });
        testcaseRepository.delete(testcase);
    }

    // 문제 테스트케이스 조회
    @Override
    public List<TestcaseDto> getProblemTestcaseList(Long problemId) throws  Exception {
        List<Testcase> testcases = testcaseRepository.findByProblemId(problemId);
        List<TestcaseDto> testcaseDtos = new ArrayList<>();
        testcases.forEach(s -> testcaseDtos.add(TestcaseDto.setFormat(s)));

        return testcaseDtos;
    }
}
