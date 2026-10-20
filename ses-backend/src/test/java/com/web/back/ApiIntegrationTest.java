package com.web.back;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.back.model.Board;
import com.web.back.repository.BoardRepository;
import com.web.back.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {
    private static final String PASSWORD = "SesTest123!";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MemberRepository members;
    @Autowired BoardRepository boards;
    @Value("${jwt.signing.key.secret}") String signingKey;

    @Test
    void signsUpAndLogsInWithStudentNumber() throws Exception {
        String token = signUpAndLogin("20260001");
        var member = members.findByNumber("20260001").orElseThrow();
        assertThat(member.getPassword()).isNotEqualTo(PASSWORD).startsWith("$2");
        mvc.perform(get("/problem").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsDuplicateSignup() throws Exception {
        signUpAndLogin("20260001");
        send(post("/signup"), registration("20260001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error - ']").isString());
        assertThat(members.count()).isEqualTo(1);
    }

    @Test
    void rejectsWeakPasswordWithoutSavingMember() throws Exception {
        var registration = registration("20260001");
        registration.put("password", "weak");
        registration.put("passwordCheck", "weak");
        send(post("/signup"), registration).andExpect(status().isBadRequest());
        assertThat(members.count()).isZero();
    }

    @Test
    void rejectsIncorrectLoginPassword() throws Exception {
        signUpAndLogin("20260001");
        send(post("/login"), Map.of("id", "20260001", "password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$['error - ']").isString());
    }

    @Test
    void requiresAuthenticationForBusinessApis() throws Exception {
        for (String path : new String[]{"/problem", "/lesson", "/board/1", "/assignment/1", "/manage?page=1"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void rejectsMalformedJwtWithoutCallingController() throws Exception {
        mvc.perform(get("/problem").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsExpiredJwt() throws Exception {
        signUpAndLogin("20260001");
        String token = Jwts.builder().setSubject("20260001")
                .setExpiration(new Date(0)).signWith(SignatureAlgorithm.HS512, signingKey).compact();
        mvc.perform(get("/problem").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsJwtSignedWithDifferentKey() throws Exception {
        signUpAndLogin("20260001");
        String otherKey = java.util.Base64.getEncoder().encodeToString(new byte[64]);
        String token = Jwts.builder().setSubject("20260001")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(SignatureAlgorithm.HS512, otherKey).compact();
        mvc.perform(get("/problem").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTokenOfDeletedMember() throws Exception {
        String token = signUpAndLogin("20260001");
        members.delete(members.findByNumber("20260001").orElseThrow());
        members.flush();
        mvc.perform(get("/problem").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changesPasswordAndAuthenticatesWithNewPassword() throws Exception {
        String token = signUpAndLogin("20260001");
        send(authorized(put("/changePassword"), token), Map.of(
                "password", PASSWORD, "newPassword", "Changed123!", "newPasswordCheck", "Changed123!"))
                .andExpect(status().isOk());
        send(post("/login"), Map.of("id", "20260001", "password", PASSWORD))
                .andExpect(status().isUnauthorized());
        send(post("/login"), Map.of("id", "20260001", "password", "Changed123!"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
    }

    @Test
    void createsReadsSearchesUpdatesAndDeletesProblem() throws Exception {
        String token = signUpAndLogin("20260001");
        var body = Map.of("title", "반복문", "description", "숫자를 출력하세요.", "language", 1);
        long id = read(send(authorized(post("/problem/write"), token), body)
                .andExpect(status().isOk())).path("id").asLong();
        mvc.perform(authorized(get("/problemDetail/{id}", id), token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.problem.title").value("반복문"));
        mvc.perform(authorized(get("/problem/search").param("keyword", "반복문").param("page", "1"), token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.problemDtos[0].id").value(id));
        send(authorized(put("/problem/edit/{id}", id), token),
                Map.of("title", "수정한 문제", "description", "수정한 설명", "language", 2))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("수정한 문제"));
        mvc.perform(authorized(delete("/problem/delete/{id}", id), token)).andExpect(status().isOk());
        mvc.perform(authorized(get("/problemDetail/{id}", id), token))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$['error - ']").isString());
    }

    @Test
    void createsAndReadsProblemTestcases() throws Exception {
        String token = signUpAndLogin("20260001");
        long id = read(send(authorized(post("/problem/write"), token),
                Map.of("title", "덧셈", "description", "합을 출력하세요.", "language", 1))
                .andExpect(status().isOk())).path("id").asLong();
        send(authorized(post("/problem/{id}/testcase/add", id), token),
                Map.of("input", "1 2", "output", "3", "problem_id", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.output").value("3"));
        mvc.perform(authorized(get("/problem/{id}/testcase", id), token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].input").value("1 2"));
    }

    @Test
    void createsLessonAndAssignmentAndReadsThemBack() throws Exception {
        String token = signUpAndLogin("20260001");
        long lessonId = read(send(authorized(post("/lesson/add"), token),
                Map.of("name", "소프트웨어공학", "year", "2026", "semester", "1"))
                .andExpect(status().isOk())).path("id").asLong();
        long assignmentId = read(send(authorized(post("/assignment/add"), token),
                Map.of("title", "첫 과제", "description", "과제 설명", "language", 1,
                        "lessonId", lessonId, "end_time", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())).path("id").asLong();
        mvc.perform(authorized(get("/lesson/{id}/assignment", lessonId), token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(assignmentId));
        mvc.perform(authorized(get("/assignment/{id}", assignmentId), token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lessonId").value(lessonId));
    }

    @Test
    void createsPostAndCommentAndPreventsEditingAnotherMembersPost() throws Exception {
        String token = signUpAndLogin("20260001");
        Board board = new Board();
        board.setCategory("자유게시판");
        long boardId = boards.saveAndFlush(board).getId();
        long postId = read(send(authorized(post("/board/{id}/write", boardId), token),
                Map.of("title", "질문", "content", "질문 내용"))
                .andExpect(status().isOk())).path("id").asLong();
        send(authorized(post("/board/{board}/{post}/comment", boardId, postId), token),
                Map.of("content", "답변"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").value("답변"));
        mvc.perform(authorized(get("/board/{board}/{post}", boardId, postId), token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.comments[0].content").value("답변"));
        String otherToken = signUpAndLogin("20260002");
        send(authorized(put("/board/{board}/edit/{post}", boardId, postId), otherToken),
                Map.of("title", "다른 회원의 수정", "content", "내용"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$['error - ']").value("작성자가 아닙니다."));
    }

    @Test
    void rejectsMemberManagementForStudent() throws Exception {
        String token = signUpAndLogin("20260001");
        mvc.perform(authorized(get("/manage").param("page", "1"), token))
                .andExpect(status().isBadRequest());
    }

    private String signUpAndLogin(String number) throws Exception {
        send(post("/signup"), registration(number)).andExpect(status().isOk());
        return read(send(post("/login"), Map.of("id", number, "password", PASSWORD))
                .andExpect(status().isOk())).path("token").asText();
    }

    private Map<String, Object> registration(String number) {
        return new java.util.HashMap<>(Map.of("number", number, "password", PASSWORD,
                "passwordCheck", PASSWORD, "email", "s" + number.substring(6) + "@kumoh.ac.kr",
                "name", "홍길동", "identity", 1));
    }

    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private ResultActions send(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body)));
    }

    private JsonNode read(ResultActions result) throws Exception {
        return mapper.readTree(result.andReturn().getResponse().getContentAsByteArray());
    }
}
