package com.web.back.service.impl;

import com.web.back.dto.SubmitDto;
import com.web.back.model.Testcase;
import com.web.back.utils.DockerCodeRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JudgeServiceImplTest {
    @Mock DockerCodeRunner runner;
    @InjectMocks JudgeServiceImpl judge;

    @Test
    void awards100OnlyWhenEveryCaseMatchesAndNormalizesLineEndings() {
        execution("OK", List.of(ok(" a\nb \n"), ok("3\n")));
        var result = judge.grade(0, submission(), List.of(testcase("a\r\nb"), testcase("3")), "없음");
        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getResult()).isEqualTo("정답입니다.");
        assertThat(result.getSubmit_time()).isNotNull();
    }

    @Test
    void laterSuccessCannotOverwriteEarlierWrongAnswer() {
        execution("OK", List.of(ok("wrong"), ok("3")));
        var result = judge.grade(1, submission(), List.of(testcase("3"), testcase("3")), "없음");
        assertThat(result.getScore()).isZero();
        assertThat(result.getResult()).isEqualTo("오답입니다.");
    }

    @Test
    void laterSuccessCannotOverwriteEarlierTimeout() {
        execution("OK", List.of(new DockerCodeRunner.CaseResult("TIME_LIMIT", "", ""), ok("3")));
        var result = judge.grade(1, submission(), List.of(testcase("3"), testcase("3")), "없음");
        assertThat(result.getScore()).isZero();
        assertThat(result.getResult()).isEqualTo("시간 초과입니다.");
    }

    @ParameterizedTest
    @CsvSource({"COMPILE_ERROR,컴파일 에러입니다.", "RUNTIME_ERROR,실행 오류입니다.",
            "TIME_LIMIT,시간 초과입니다.", "OUTPUT_LIMIT,출력 제한을 초과했습니다.", "MEMORY_LIMIT,메모리 제한을 초과했습니다."})
    void mapsExecutionFailuresToExistingResultFields(String status, String message) {
        execution(status, List.of());
        var result = judge.grade(1, submission(), List.of(testcase("3")), "없음");
        assertThat(result.getScore()).isZero();
        assertThat(result.getResult()).isEqualTo(message);
    }

    @Test
    void noTestcasesNeverReceivesPassingScoreOrStartsDocker() {
        var result = judge.grade(1, submission(), List.of(), "테스트 할 수 없는 문제입니다.");
        assertThat(result.getScore()).isEqualTo(-1);
        verifyNoInteractions(runner);
    }

    @Test
    void rejectsLanguageThatDoesNotMatchTheProblemBeforeExecution() {
        var result = judge.grade(2, submission(), List.of(testcase("3")), "없음");
        assertThat(result.getScore()).isZero();
        assertThat(result.getResult()).isEqualTo("지원되지 않는 언어입니다.");
        verifyNoInteractions(runner);
    }

    @Test
    void incompleteResultsAndUnknownStatusesAreInfrastructureFailures() {
        execution("OK", List.of(ok("3")));
        assertThatThrownBy(() -> judge.grade(1, submission(), List.of(testcase("3"), testcase("3")), "없음"))
                .isInstanceOf(IllegalStateException.class);
        execution("INVALID", List.of());
        assertThatThrownBy(() -> judge.grade(1, submission(), List.of(testcase("3")), "없음"))
                .isInstanceOf(IllegalStateException.class);
    }

    private void execution(String status, List<DockerCodeRunner.CaseResult> cases) {
        when(runner.run(anyInt(), anyString(), anyList())).thenReturn(new DockerCodeRunner.Result(status, cases));
    }

    private DockerCodeRunner.CaseResult ok(String output) {
        return new DockerCodeRunner.CaseResult("OK", output, "debug messages are not answers");
    }

    private SubmitDto submission() {
        SubmitDto dto = new SubmitDto();
        dto.setLanguage(1);
        dto.setSubmit_contents("source");
        return dto;
    }

    private Testcase testcase(String expected) {
        Testcase testcase = new Testcase();
        testcase.setInput("1 2");
        testcase.setOutput(expected);
        return testcase;
    }
}
