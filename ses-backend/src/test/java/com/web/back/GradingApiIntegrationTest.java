package com.web.back;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.back.model.*;
import com.web.back.repository.*;
import com.web.back.security.JwtTokenProvider;
import com.web.back.utils.DockerCodeRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GradingApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MemberRepository members;
    @Autowired ProblemRepository problems;
    @Autowired LessonRepository lessons;
    @Autowired AssignmentRepository assignments;
    @Autowired TestcaseRepository testcases;
    @Autowired SubmitRepository submissions;
    @Autowired JwtTokenProvider tokens;
    @Autowired PasswordEncoder encoder;
    @MockitoBean DockerCodeRunner runner;

    private Member member;
    private String token;

    @BeforeEach
    void member() {
        member = new Member();
        member.setNumber("20260011");
        member.setEmail("judge@kumoh.ac.kr");
        member.setPassword(encoder.encode("SesTest123!"));
        member.setName("홍길동");
        member.setIdentity(1);
        members.saveAndFlush(member);
        token = tokens.generateToken(member);
    }

    @Test
    void problemRunUsesPathIdAndAuthenticatedMemberAndDoesNotPersistSubmission() throws Exception {
        Problem problem = problem(true);
        correct();
        JsonNode result = request("/problem/run/" + problem.getId(), body(), 200);
        assertThat(result.path("score").asInt()).isEqualTo(100);
        assertThat(result.path("problem_id").asLong()).isEqualTo(problem.getId());
        assertThat(result.path("member_id").asLong()).isEqualTo(member.getId());
        assertThat(submissions.count()).isZero();
    }

    @Test
    void problemSubmitWithoutBodyIdSavesAndUpdatesLatestLanguageSubmission() throws Exception {
        Problem problem = problem(true);
        correct();
        JsonNode first = request("/problem/submit/" + problem.getId(), body(), 200);
        assertThat(first.path("score").asInt()).isEqualTo(100);
        when(runner.run(anyInt(), anyString(), anyList())).thenReturn(new DockerCodeRunner.Result("COMPILE_ERROR", List.of()));
        JsonNode second = request("/problem/submit/" + problem.getId(), body(), 200);
        assertThat(second.path("id")).isEqualTo(first.path("id"));
        assertThat(second.path("score").asInt()).isZero();
        assertThat(second.path("result").asText()).isEqualTo("컴파일 에러입니다.");
        assertThat(submissions.count()).isEqualTo(1);
    }

    @Test
    void assignmentDetailPreservesSubmittedLanguageAndScoreForEditor() throws Exception {
        Assignment assignment = assignment(LocalDateTime.now().plusDays(1));
        assignment.setLanguage(0);
        assignments.saveAndFlush(assignment);
        correct();
        String code = "#include <iostream>\nint main() { std::cout << 3; }";
        request("/assignment/submit/" + assignment.getId(), Map.of("language", 2, "submit_contents", code), 200);
        mvc.perform(get("/assignment/" + assignment.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value(0))
                .andExpect(jsonPath("$.submit_language").value(2))
                .andExpect(jsonPath("$.score").value(100))
                .andExpect(jsonPath("$.submit_contents").value(code));
    }

    @Test
    void rejectsMismatchedBodyIdBeforeStartingGrader() throws Exception {
        Problem problem = problem(true);
        var body = new java.util.HashMap<>(body());
        body.put("problem_id", problem.getId() + 1);
        request("/problem/submit/" + problem.getId(), body, 400);
        verifyNoInteractions(runner);
        assertThat(submissions.count()).isZero();
    }

    @Test
    void infrastructureFailureDoesNotSaveAZeroScore() throws Exception {
        Problem problem = problem(true);
        when(runner.run(anyInt(), anyString(), anyList())).thenThrow(new IllegalStateException("Docker unavailable"));
        request("/problem/submit/" + problem.getId(), body(), 400);
        assertThat(submissions.count()).isZero();
    }

    @Test
    void problemWithoutTestcasesReturnsUngradedResult() throws Exception {
        Problem problem = problem(false);
        assertThat(request("/problem/run/" + problem.getId(), body(), 200).path("score").asInt()).isEqualTo(-1);
        verifyNoInteractions(runner);
    }

    @Test
    void assignmentRunAndSubmitShareGraderAndKeepSubmissionAcknowledgement() throws Exception {
        Assignment assignment = assignment(LocalDateTime.now().plusDays(1));
        correct();
        JsonNode run = request("/assignment/run/" + assignment.getId(), body(), 200);
        assertThat(run.path("assignment_id").asLong()).isEqualTo(assignment.getId());
        assertThat(run.path("result").asText()).isEqualTo("정답입니다.");
        JsonNode submit = request("/assignment/submit/" + assignment.getId(), body(), 200);
        assertThat(submit.path("score").asInt()).isEqualTo(100);
        assertThat(submit.path("result").asText()).isEqualTo("제출되었습니다.");
        assertThat(submissions.count()).isEqualTo(1);
    }

    @Test
    void pastDeadlineIsRejectedBeforeStartingGrader() throws Exception {
        Assignment assignment = assignment(LocalDateTime.now().minusDays(1));
        request("/assignment/submit/" + assignment.getId(), body(), 400);
        verifyNoInteractions(runner);
        assertThat(submissions.count()).isZero();
    }

    private Map<String, Object> body() {
        return Map.of("language", 1, "submit_contents", "public class Main { public static void main(String[] a) { System.out.println(3); } }");
    }

    private void correct() {
        when(runner.run(anyInt(), anyString(), anyList())).thenReturn(new DockerCodeRunner.Result("OK",
                List.of(new DockerCodeRunner.CaseResult("OK", "3\n", ""))));
    }

    private JsonNode request(String path, Object body, int expected) throws Exception {
        var response = mvc.perform(post(path).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body)))
                .andExpect(status().is(expected)).andReturn().getResponse();
        return mapper.readTree(response.getContentAsByteArray());
    }

    private Problem problem(boolean withTestcase) {
        Problem problem = new Problem();
        problem.setTitle("덧셈");
        problem.setDescription("합을 출력하세요.");
        problem.setLanguage(0);
        problem.setMember(member);
        problems.saveAndFlush(problem);
        if (withTestcase) {
            Testcase testcase = testcase();
            testcase.setProblem(problem);
            testcases.saveAndFlush(testcase);
        }
        return problem;
    }

    private Assignment assignment(LocalDateTime deadline) {
        Lesson lesson = new Lesson();
        lesson.setName("강의실");
        lesson.setYear("2026");
        lesson.setSemester("1");
        lesson.setMember(member);
        lessons.saveAndFlush(lesson);
        Assignment assignment = new Assignment();
        assignment.setTitle("덧셈 과제");
        assignment.setDescription("합을 출력하세요.");
        assignment.setLanguage(1);
        assignment.setLesson(lesson);
        assignment.setEnd_time(deadline);
        assignments.saveAndFlush(assignment);
        Testcase testcase = testcase();
        testcase.setAssignment(assignment);
        testcases.saveAndFlush(testcase);
        return assignment;
    }

    private Testcase testcase() {
        Testcase testcase = new Testcase();
        testcase.setInput("1 2\n");
        testcase.setOutput("3");
        return testcase;
    }
}
