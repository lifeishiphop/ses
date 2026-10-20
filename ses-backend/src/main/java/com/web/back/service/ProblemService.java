package com.web.back.service;


import com.web.back.dto.*;

import java.util.List;

public interface ProblemService {
    List<ProblemDto> getProblemList() throws Exception;

    List<ProblemDto> getMyProblemList() throws Exception;

    ProblemDataDto getProblemDetail(Long problemId) throws Exception;

    ProblemDto registerProblem(ProblemDto problemDto) throws Exception;

    ProblemDto editProblem(Long problemId, ProblemDto problemDto) throws Exception;

    void deleteProblem(Long problemId) throws Exception;

    //List<ProblemDto> searchProblem(String keyword,int page) throws Exception;
    SearchResultDto searchProblem(String keyword, int page) throws Exception;

    SubmitDto runProblemCode(Long problemId, SubmitDto submitDto) throws Exception;

    SubmitDto submitProblemCode(Long problemId, SubmitDto submitDto) throws Exception;

    TestcaseDto addProblemTestcase(TestcaseDto testcaseDto) throws  Exception;

    TestcaseDto editProblemTestcase(Long testcaseId, TestcaseDto testcaseDto) throws  Exception;

    void deleteProblemTestcase(Long testcaseId) throws Exception;

    List<TestcaseDto> getProblemTestcaseList(Long problemId) throws  Exception;
}
