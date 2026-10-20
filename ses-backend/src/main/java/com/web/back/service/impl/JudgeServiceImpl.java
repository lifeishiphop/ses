package com.web.back.service.impl;

import com.web.back.dto.SubmitDto;
import com.web.back.model.Testcase;
import com.web.back.service.JudgeService;
import com.web.back.utils.DockerCodeRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {
    private final DockerCodeRunner runner;

    @Override
    public SubmitDto grade(Integer allowedLanguage, SubmitDto submission, List<Testcase> testcases, String noTestsMessage) {
        Integer language = submission.getLanguage();
        if (language == null || (language != 1 && language != 2)
                || allowedLanguage == null || (allowedLanguage != 0 && !allowedLanguage.equals(language))) {
            return result(submission, 0, "지원되지 않는 언어입니다.");
        }
        if (testcases.isEmpty()) return result(submission, -1, noTestsMessage);
        var execution = runner.run(language, submission.getSubmit_contents(), testcases.stream().map(Testcase::getInput).toList());
        if (!"OK".equals(execution.status())) return result(submission, 0, message(execution.status()));
        if (execution.cases().isEmpty() || execution.cases().size() > testcases.size()) {
            throw new IllegalStateException("채점기의 테스트 결과 개수가 일치하지 않습니다.");
        }
        for (int i = 0; i < execution.cases().size(); i++) {
            var actual = execution.cases().get(i);
            if (!"OK".equals(actual.status())) return result(submission, 0, message(actual.status()));
            String expected = testcases.get(i).getOutput();
            if (expected == null || actual.output() == null) throw new IllegalStateException("채점 입출력 형식이 올바르지 않습니다.");
            if (!normalize(actual.output()).equals(normalize(expected))) return result(submission, 0, "오답입니다.");
        }
        if (execution.cases().size() != testcases.size()) {
            throw new IllegalStateException("채점기의 테스트 결과 개수가 일치하지 않습니다.");
        }
        return result(submission, 100, "정답입니다.");
    }

    private String message(String status) {
        return switch (status) {
            case "COMPILE_ERROR" -> "컴파일 에러입니다.";
            case "RUNTIME_ERROR" -> "실행 오류입니다.";
            case "TIME_LIMIT" -> "시간 초과입니다.";
            case "OUTPUT_LIMIT" -> "출력 제한을 초과했습니다.";
            case "MEMORY_LIMIT" -> "메모리 제한을 초과했습니다.";
            default -> throw new IllegalStateException("알 수 없는 채점 결과입니다.");
        };
    }

    private String normalize(String output) {
        return output.replace("\r\n", "\n").trim();
    }

    private SubmitDto result(SubmitDto submission, int score, String message) {
        submission.setSubmit_time(LocalDateTime.now());
        submission.setScore(score);
        submission.setResult(message);
        return submission;
    }
}
